package yaccoin.block

import yaccoin.utils.MiscUtils

import scala.collection.immutable.SortedSet
import scala.util.Try

/** A class for MemPool.
  *
  * @param transactions List of transactions.
  */
case class MemPool(transactions: SortedSet[SignedTransaction]) {

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

    verifyTransaction.map(_ => MemPool(transactions ++ SortedSet(signedTxn)))

  }

  /** Remove (confirmed) transactions from MemPool.
    *
    * @param txns Transactions to be removed.
    * @return Set difference of current and confirmed transactions.
    */
  def removeTransactions(txns: SortedSet[SignedTransaction]): MemPool =
    MemPool(transactions &~ txns)

}

object MemPool {

  /** An empty memPool. */
  val emptyMemPool: MemPool = MemPool(SortedSet())

}
