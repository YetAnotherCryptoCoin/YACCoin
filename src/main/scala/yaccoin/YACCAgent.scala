package yaccoin

import akka.actor.{ActorRef, ActorSystem, Address, Props}
import akka.pattern.ask
import akka.util.Timeout
import scorex.crypto.signatures.PublicKey
import yaccoin.actors.Protocol.{BootStrap, DoTransaction, GetPublicKey, MyPublicKey}
import yaccoin.actors.{Communicator, Miner, Transactor}
import yaccoin.block.{BlockChain, MemPool}

import scala.collection.immutable.TreeSet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

object YACCAgent extends App {

  private val system = ActorSystem("YACCSystem")

  private val remoteCommunicators = args
    .map(x => Address("akka.tcp", "YACCSystem", x, 2552).toString)
    .map(x => system.actorSelection(s"$x/user/localCommunicator"))
    .toList

  system.actorOf(
    Props(classOf[Communicator], (TreeSet[ActorRef](), TreeSet[ActorRef]())),
    "localCommunicator"
  )

  system.actorOf(
    Props(classOf[Miner], MemPool.emptyMemPool),
    "localMiner"
  ) ! BootStrap(remoteCommunicators)

  val transactor = system.actorOf(
    Props(classOf[Transactor], Tuple1(BlockChain.initBlockChain)),
    "localTransactor"
  )
  transactor ! BootStrap(remoteCommunicators)

  implicit val timeout: Timeout = Timeout(1.minute)
  implicit val ctx: ExecutionContext = ExecutionContext.global

  (transactor ? GetPublicKey).map {
    case MyPublicKey(publicKey) =>
      println(s"Local Transactor's Public Key: ${publicKey.mkString(":")}.")

      @inline def defined(line: String) = {
        line != null && !line.startsWith(":q")
      }

      val transactionRegex = """:txn ([0-9]+) ((\-?[0-9]+)(:\-?[0-9]+)*)"""".r

      Iterator.continually({print("CMD>> "); StdIn.readLine}).takeWhile(defined).foreach {
        case transactionRegex(amt, to, _*) => transactor ! DoTransaction(PublicKey @@ to.getBytes, amt.toLong)
      }
  }

}
