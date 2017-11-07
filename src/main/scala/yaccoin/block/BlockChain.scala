package yaccoin.block

import yaccoin.utils.MiscUtils

import scala.util.Try

/** Implementation of a BlockChain.
  *
  * Basically a wrapper around a List[Block] for additional
  * security logic.
  *
  * @param blocks The starting list of blocks.
  */
case class BlockChain(blocks: List[Block]) {

  /** Add a block to the block chain.
    *
    * @param newBlock Block to add.
    * @return A new BlockChain.
    */
  def addBlock(newBlock: Block): Try[BlockChain] = {

    /* block Id of new block > latest block I. */
    val validateId = MiscUtils.condToTry(
      newBlock.blockId > blocks.head.blockId,
      "New block is older than current block."
    )

    /* Previous block's header contains current block's hash. */
    val validateHistory = MiscUtils.condToTry(
      newBlock.header.indexOfSlice(blocks.head.header) != -1,
      "New block does not succeed the current block."
    )

    /* Check whether block has proof of work and is not tampered with. */
    val validateIntegrity = MiscUtils.condToTry(
      Block.verifyIntegrity(newBlock),
      "New block does not pass the validation test."
    )

    /* Perform validation and return the new BlockChain. */
    validateId.map(_ => validateHistory).map(_ => validateIntegrity).map({_ =>
      BlockChain(List(newBlock) ++ blocks)
    })

  }

  override def toString: String = s"BlockChain: ${blocks.map(_.toString).reduce(_ + _)}!"

}

object BlockChain {

  /** Get the block chain with only Genesis block. */
  val initBlockChain: BlockChain = BlockChain(List(Block.genesisBlock))

}
