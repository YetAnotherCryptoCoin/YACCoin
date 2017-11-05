package yaccoin.block

import scorex.crypto.signatures.{PrivateKey, PublicKey}
import yaccoin.utils.{MiningUtils, SerialisationUtils}

/** A transaction, which has not yet been signed.
  *
  * @param txnId Transaction Id.
  * @param from Public key of sender.
  * @param to Public key of receiver.
  * @param amount Amount to send.
  */
@SerialVersionUID(101L)
case class UnsignedTransaction(
                                txnId: Long,
                                from: PublicKey,
                                to: PublicKey,
                                amount: Double) extends Ordered[UnsignedTransaction] {

  /** Sign the transaction with the sender's private key.
    *
    * @param privateKey Private key to sign with.
    * @return An instance of the SignedTransaction.
    */
  def sign(privateKey: PrivateKey): SignedTransaction = SignedTransaction(
    this,
    MiningUtils.signatureCurve.sign(privateKey, SerialisationUtils.serialise(this))
  )

  /** Function to compare two transactions so they can be ordered,
    * based on their transaction Id.
    *
    * @param that Transaction to compare to.
    * @return 0 is equal.
    */
  override def compare(that: UnsignedTransaction): Int = txnId.compare(that.txnId)

}

object UnsignedTransaction {

  /** Set the TxnId from current timestamp.
    *
    * @param from Public key of sender.
    * @param to Public key of receiver.
    * @param amount Amount to send.
    * @return An instance of UnsignedTransaction.
    */
  def apply(from: PublicKey, to: PublicKey, amount: Double): UnsignedTransaction =
    UnsignedTransaction(System.currentTimeMillis, from, to, amount)

}


