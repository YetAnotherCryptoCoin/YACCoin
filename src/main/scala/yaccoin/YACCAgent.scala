package yaccoin

import akka.actor.{ActorRef, ActorSystem, Address, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import scorex.crypto.signatures.PublicKey
import yaccoin.actors.Protocol._
import yaccoin.actors.{Communicator, Miner, Transactor}
import yaccoin.block.{BlockChain, MemPool}

import scala.collection.immutable.TreeSet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object YACCAgent extends App {

  private val ipRegex = """^[ \t]*(.*):([0-9]+)[ \t]*$""".r
  private val validArgs = args.map {
    case ipRegex(_, _, _*) => true
    case _ => false
  }.reduce(_ && _) && args.length > 0

  if (!validArgs) {
    throw new RuntimeException("Args not in 'IP:Port' format or number of args < 1.")
  }

  private val system = ActorSystem(
    "YACCSystem",
    ConfigFactory
      .load()
      .withValue(
        "akka.remote.netty.tcp.hostname",
        ConfigValueFactory.fromAnyRef(args.head.split(':')(0))
      ).withValue(
        "akka.remote.netty.tcp.port",
        ConfigValueFactory.fromAnyRef(args.head.split(':')(1).toInt)
      )
  )

  private val remoteAddresses = args
    .tail
    .map(x => Address("akka.tcp", "YACCSystem", x.split(':')(0), x.split(':')(1).toInt))
    .toList

  val miner = system.actorOf(
    Props(classOf[Miner], MemPool.emptyMemPool),
    "localMiner"
  )

  val transactor = system.actorOf(
    Props(classOf[Transactor], Tuple1(BlockChain.initBlockChain)),
    "localTransactor"
  )

  system.actorOf(
    Props(classOf[Communicator], (TreeSet[ActorRef](), TreeSet[ActorRef]())),
    "localCommunicator"
  ) ! BootStrap(remoteAddresses)


  implicit val timeout: Timeout = Timeout(1.minute)
  implicit val ctx: ExecutionContext = ExecutionContext.global

  (transactor ? GetPublicKey).map {
    case MyPublicKey(publicKey) =>
      println(s"Local Transactor's Public Key: ${publicKey.mkString(":")}.")

      val transactionRegex = """^[ \t]*:t(ransaction)?[ \t]+([0-9]+)[ \t]+((\-?[0-9]+)(:\-?[0-9]+)*)[ \t]*$""".r
      val quitRegex = """^[ \t]*:q(uit)?[ \t]*$""".r
      val memPoolRegex = """^[ \t]*:m(empool)?[ \t]*$""".r
      val blockChainRegex = """^[ \t]*:b(lockchain)?[ \t]*$""".r

      Iterator.continually({print("CMD>> "); StdIn.readLine}).foreach {
        case transactionRegex(_, amt, to, _*) =>
          transactor ! DoTransaction(PublicKey @@ to.split(':').map(_.toInt.toByte), amt.toLong)

        case memPoolRegex(_*) =>
          miner ! ShowMemPool

        case blockChainRegex(_*) =>
          transactor ! ShowBlockChain

        case quitRegex(_*) =>
          system.terminate.wait()

        case _ => // No-Op
      }
  }

}
