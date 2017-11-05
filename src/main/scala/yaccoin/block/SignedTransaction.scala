package yaccoin.block

import scorex.crypto.signatures.Signature
import yaccoin.utils.{MiningUtils, SerialisationUtils}

/** A transaction which has been signed.
  *
  * @param txn The transaction object.
  * @param sign The signature.
  */
@SerialVersionUID(102L)
case class SignedTransaction(txn: UnsignedTransaction, sign: Signature) extends Ordered[SignedTransaction] {

  /** Validate the transaction contained through signature. */
  def verify: Boolean = MiningUtils.signatureCurve.verify(sign, SerialisationUtils.serialise(txn), txn.from)

  /** Compare two signed transactions based on their Ids. */
  override def compare(that: SignedTransaction): Int = txn.compare(that.txn)

}
