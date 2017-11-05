package yaccoin.actors

import scorex.crypto.hash.Digest
import scorex.crypto.signatures.PublicKey
import yaccoin.block.{Block, SignedTransaction}

import scala.collection.immutable.SortedSet

/** Define the messages for the actors to communicate through. */
object Protocol {

  /** A trait for messages in this protocol. */
  trait Message extends Serializable

  /** Discovery message sent by a new transactor to
    * make everyone aware of it's existence. */
  @SerialVersionUID(201L)
  object DiscoverTransactor extends Message

  /** Discovery message for miner. */
  @SerialVersionUID(202L)
  object DiscoverMiner extends Message

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

  /** Start mining the un-mined transactions.
    *
    * @param prevBlockHash Hash of previous block.
    */
  @SerialVersionUID(206L)
  case class BeginMining(prevBlockHash: Digest) extends Message

  /** Stop the current mining process. */
  @SerialVersionUID(207L)
  object StopMining extends Message

  /** Send miner a list of confirmed transactions to remove from
    * MemPool.
    *
    * @param txns Set of transactions.
    */
  @SerialVersionUID(208L)
  case class ConfirmedTransactions(txns: SortedSet[SignedTransaction]) extends Message

}
