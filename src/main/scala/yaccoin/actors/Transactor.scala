package yaccoin.actors

import scorex.crypto.signatures.{PrivateKey, PublicKey}
import scorex.crypto.hash.Digest
import yaccoin.actors.Protocol._
import yaccoin.block._
import yaccoin.utils.MiningUtils

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Transactor(initState: TransactorState) extends AbstractActor[TransactorState](initState) {

  private val keyPair = MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)

  private val privateKey: PrivateKey = keyPair._1
  val publicKey: PublicKey = keyPair._2

  override def preStart(): Unit = {

    implicit val dispatcher: ExecutionContext = context.system.dispatcher

    /* Schedule messages to be sent to miner to begin mining blocks. */
    context.system.scheduler.schedule(
      0.milliseconds,
      MiningUtils.mineInterval,
      self,
      BeginMining(null)
    )

  }

  override def active(state: TransactorState): Receive = {

    case NewBlock(block) => state.blockChain.addBlock(block) match {
      case Success(x) =>
        log.info(s"Added new block ${x.blocks.head.blockId} to the Block Chain.")

        context.become(active(Tuple1(x)))
        localMiner ! StopMining
        localMiner ! ConfirmedTransactions(block.transactions)

      case Failure(ex) => log.error(ex.getMessage)
    }

    case BeginMining(null) =>
      localMiner ! BeginMining(MiningUtils.hashFunction(state.blockChain.blocks.head.header))

    case DoTransaction(to, amt) =>
      log.info(s"Sending $amt from $publicKey(me) to $to.")

      localCommunicator ! NewTransaction(UnsignedTransaction(publicKey, to, amt).sign(privateKey))

  }

}
