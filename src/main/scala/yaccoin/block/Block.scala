package yaccoin.block

import scorex.crypto.authds.LeafData
import scorex.crypto.authds.merkle.MerkleTree
import scorex.crypto.hash.Digest
import yaccoin.utils.{MiningUtils, SerialisationUtils}

import scala.collection.immutable.SortedSet
import scala.util.Random

/** A concrete implementation of Block trait.
  *
  * @param transactions A list of transactions.
  * @param prevBlockHash Hash of previous block's header.
  * @param nonce The nonce for this block.
  */
case class Block(
                  blockId: Long,
                  transactions: SortedSet[SignedTransaction],
                  prevBlockHash: Digest,
                  truncatedHeader: Array[Byte],
                  nonce: Long
                ) {

  def header: Array[Byte] = truncatedHeader ++
    BigInt(blockId).toByteArray ++
    BigInt(nonce).toByteArray

}

object Block {

  /** Get an instance of ConcreteBlock from arguments.
    *
    * Set blockId as current timestamp and generate truncated header
    * from transactions.
    *
    * @param transactions A list of transactions.
    * @param prevBlockHash Hash of previous block's header.
    * @return Generated block.
    */
  def apply(transactions: SortedSet[SignedTransaction], prevBlockHash: Digest): Block = {
    require(
      transactions.par.map(_.verify).reduceOption(_ && _).getOrElse(true),
      "Some transactions are have invalid signature."
    )

    val blockId = System.currentTimeMillis
    val truncatedHeader =
      MerkleTree(transactions.map(x => LeafData @@ SerialisationUtils.serialise(x)).toSeq)(MiningUtils.hashFunction).rootHash ++
        prevBlockHash

    Block(blockId, transactions, prevBlockHash, truncatedHeader, Random.nextLong)
  }

  def verifyIntegrity(block: Block): Boolean = {
    val truncatedHeader =
      MerkleTree(block.transactions.map(x => LeafData @@ SerialisationUtils.serialise(x)).toSeq)(MiningUtils.hashFunction).rootHash ++
        block.prevBlockHash

     block.transactions.par.map(_.verify).reduceOption(_ && _).getOrElse(true) && // Transactions are signed
       MiningUtils.difficultyFunction(block.header) &&      // Block passes proof-of-work
       truncatedHeader.sameElements(block.truncatedHeader)  // Header contains the provided transactions' hash
  }

  /** The genesis block. */
  val genesisBlock: Block =
    Block(0, SortedSet(), MiningUtils.hashFunction(Array.emptyByteArray), Array.emptyByteArray, 0)

}
