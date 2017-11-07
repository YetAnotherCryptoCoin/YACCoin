package yaccoin.block

import yaccoin.utils.MiscUtils

import scala.collection.immutable.TreeSet
import scala.util.Try

/** A class for MemPool.
  *
  * @param transactions List of transactions.
  */
case class MemPool(transactions: TreeSet[SignedTransaction]) {

  /** Add a transaction to the MemPool after verifying it.
    *
    * @param signedTxn Transaction to add.
    * @return Success(MemPool) if successful.
    */
  def addTransaction(signedTxn: SignedTransaction): Try[MemPool] = {

    val verifyTransaction = MiscUtils.condToTry(
      signedTxn.verify,
      "Transaction cannot be verified."
    )

    verifyTransaction.map(_ => MemPool(transactions ++ TreeSet(signedTxn)))

  }

  /** Remove (confirmed) transactions from MemPool.
    *
    * @param txns Transactions to be removed.
    * @return Set difference of current and confirmed transactions.
    */
  def removeTransactions(txns: TreeSet[SignedTransaction]): MemPool =
    MemPool(transactions &~ txns)

}

object MemPool {

  /** An empty memPool. */
  val emptyMemPool: MemPool = MemPool(TreeSet())

}
