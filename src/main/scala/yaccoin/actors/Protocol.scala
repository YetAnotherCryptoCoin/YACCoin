package yaccoin.actors

import akka.actor.{ActorRef, Address}
import scorex.crypto.hash.Digest
import scorex.crypto.signatures.PublicKey
import yaccoin.block.{Block, SignedTransaction}

import scala.collection.immutable.TreeSet

/** Define the messages for the actors to communicate through. */
object Protocol {

  /** A trait for messages in this protocol. */
  trait Message extends Serializable

  /** Discovery message sent by a new transactor to
    * make everyone aware of it's existence. */
  @SerialVersionUID(201L)
  case class DiscoverTransactor(actor: ActorRef) extends Message

  /** Discovery message for miner. */
  @SerialVersionUID(202L)
  case class DiscoverMiner(actor: ActorRef) extends Message

  /** A new transaction sent by a Transactor.
    *
    * @param signedTxn A signed transaction.
    */
  @SerialVersionUID(203L)
  case class NewTransaction(signedTxn: SignedTransaction) extends Message

  /** New block in the block chain mined.
    *
    * @param block The block which was mined.
    */
  @SerialVersionUID(204L)
  case class NewBlock(block: Block) extends Message

  /** Perform a transaction.
    *
    * @param to Public key to transfer to.
    * @param amount Amount to transfer.
    */
  @SerialVersionUID(205L)
  case class DoTransaction(to: PublicKey, amount: Long) extends Message

  /** Start mining the un-mined transactions. */
  @SerialVersionUID(206L)
  object BeginMining extends Message

  /** Stop the current mining process. */
  @SerialVersionUID(207L)
  object StopMining extends Message

  /** Send miner a list of confirmed transactions to remove from
    * MemPool.
    *
    * @param txns Set of transactions.
    */
  @SerialVersionUID(208L)
  case class ConfirmedTransactions(txns: TreeSet[SignedTransaction]) extends Message

  /** Send me hash of latest block. */
  @SerialVersionUID(209L)
  object GetHash extends Message

  /** Here is hash of latest block. */
  @SerialVersionUID(210L)
  case class LatestBlockHash(hash: Digest) extends Message

  /** Tell them you're up!
    *
    * @param remote A list of remote communicators.
    */
  @SerialVersionUID(211L)
  case class BootStrap(remote: List[Address]) extends Message

  /** Tell them you're up! */
  @SerialVersionUID(212L)
  object BootStrapped extends Message

  /** Send me your public key. */
  @SerialVersionUID(213L)
  object GetPublicKey extends Message

  /** Here is my public key. */
  @SerialVersionUID(214L)
  case class MyPublicKey(publicKey: PublicKey) extends Message

  /** Print current MemPool. */
  @SerialVersionUID(215L)
  object ShowMemPool extends Message

  /** Print current BlockChain. */
  @SerialVersionUID(215L)
  object ShowBlockChain extends Message

}
