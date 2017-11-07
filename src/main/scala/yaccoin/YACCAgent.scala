package yaccoin

import akka.actor.{ActorRef, ActorSystem, Address, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import scorex.crypto.signatures.PublicKey
import yaccoin.actors.Protocol.{BootStrap, DoTransaction, GetPublicKey, MyPublicKey}
import yaccoin.actors.{Communicator, Miner, Transactor}
import yaccoin.block.{BlockChain, MemPool}

import scala.collection.immutable.TreeSet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object YACCAgent extends App {

  if (args.length < 1)
    throw new RuntimeException("Args-list too short. Provide at least the IP Address of host system.")

  private val system = ActorSystem(
    "YACCSystem",
    ConfigFactory
      .load()
      .withValue(
        "akka.remote.netty.tcp.hostname",
        ConfigValueFactory.fromAnyRef(args.head)
      )
  )

  private val remoteAddresses = args
    .tail
    .map(x => Address("akka.tcp", "YACCSystem", x, 2552))
    .toList

  system.actorOf(
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

      Iterator.continually({print("CMD>> "); StdIn.readLine}).foreach {
        case transactionRegex(_, amt, to, _*) => transactor ! DoTransaction(PublicKey @@ to.getBytes, amt.toLong)
        case quitRegex(_*) => system.terminate.wait()
      }
  }

}
