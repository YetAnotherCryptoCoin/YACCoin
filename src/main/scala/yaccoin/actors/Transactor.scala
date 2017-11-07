package yaccoin.actors

import scorex.crypto.signatures.{PrivateKey, PublicKey}
import yaccoin.actors.Protocol._
import yaccoin.block._
import yaccoin.utils.MiningUtils

import scala.util.{Failure, Success}

/** Transactor will perform transactions, and send them to Miners to mine.
  *
  * For their state, see [[TransactorState]].
  *
  * @param initState Initial state.
  */
class Transactor(initState: TransactorState) extends AbstractActor[TransactorState](initState) {

  /* Public/Private key pair. */
  private val (privateKey: PrivateKey, publicKey: PublicKey) = MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)

  override def active(state: TransactorState): Receive = {

    /* New block, see if we can add it to the block chain. */
    case NewBlock(block) => state.blockChain.addBlock(block) match {
      case Success(x) =>
        log.info(s"Added new block ${x.blocks.head.blockId} to the Block Chain.")

        println(x)

        context become active(Tuple1(x))
        localMiner ! StopMining
        localMiner ! ConfirmedTransactions(block.transactions)

      case Failure(ex) => log.error(ex.getMessage)
    }

    /* The local miner is asking for the latest block's hash. */
    case GetHash =>
      sender ! LatestBlockHash(MiningUtils.hashFunction(state.blockChain.blocks.head.header))

    /* The local program is asking for your public key. */
    case GetPublicKey =>
      sender ! MyPublicKey(publicKey)

    /* Someone on the outside says, send money. */
    case DoTransaction(to, amt) =>
      log.info(s"Sending $amt from $publicKey(me) to $to.")

      localCommunicator ! NewTransaction(UnsignedTransaction(publicKey, to, amt).sign(privateKey))

  }

}
