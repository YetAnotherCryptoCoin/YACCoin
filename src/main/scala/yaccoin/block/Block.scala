package yaccoin.block

import scorex.crypto.authds.LeafData
import scorex.crypto.authds.merkle.MerkleTree
import scorex.crypto.hash.Digest
import yaccoin.utils.{MiningUtils, SerialisationUtils}

import scala.collection.immutable.TreeSet
import scala.util.Random

/** A concrete implementation of Block trait.
  *
  * @param transactions A list of transactions.
  * @param prevBlockHash Hash of previous block's header.
  * @param nonce The nonce for this block.
  */
case class Block(
                  blockId: Long,
                  transactions: TreeSet[SignedTransaction],
                  prevBlockHash: Digest,
                  truncatedHeader: Array[Byte],
                  nonce: Long
                ) {

  def header: Array[Byte] = truncatedHeader ++
    BigInt(nonce).toByteArray

  override def toString: String =
    s"Block[${MiningUtils.hashFunction(header)}]($blockId, ${transactions.size}, $prevBlockHash, $nonce) ==> "

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
  def apply(transactions: TreeSet[SignedTransaction], prevBlockHash: Digest): Block = {
    require(
      transactions.par.map(_.verify).reduceOption(_ && _).getOrElse(true),
      "Some transactions are have invalid signature."
    )

    val blockId = System.currentTimeMillis
    val merkleTree = MerkleTree(transactions.map({
        x => LeafData @@ SerialisationUtils.serialise(x)
      }).toSeq)(MiningUtils.hashFunction)

    val truncatedHeader = BigInt(blockId).toByteArray ++ merkleTree.rootHash ++ prevBlockHash

    Block(blockId, transactions, prevBlockHash, truncatedHeader, Random.nextLong)
  }

  def verifyIntegrity(block: Block): Boolean = {
    val merkleTree = MerkleTree(block.transactions.map({
      x => LeafData @@ SerialisationUtils.serialise(x)
    }).toSeq)(MiningUtils.hashFunction)

    val truncatedHeader = BigInt(block.blockId).toByteArray ++ merkleTree.rootHash ++ block.prevBlockHash

    block.transactions.par.map(_.verify).reduceOption(_ && _).getOrElse(true) && // Transactions are signed
      MiningUtils.difficultyFunction(block.header) &&                            // Block passes proof-of-work
      truncatedHeader.sameElements(block.truncatedHeader)                        // Header contains the right
                                                                                 // transactions' hash
  }

  /** The genesis block. */
  val genesisBlock: Block =
    new Block(0, TreeSet(), MiningUtils.hashFunction(Array.emptyByteArray), Array.emptyByteArray, 0) {

      override def toString: String = "GENESIS_BLOCK"

    }

}
