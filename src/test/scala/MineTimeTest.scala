import scorex.crypto.signatures.{PrivateKey, PublicKey}
import yaccoin.block.{Block, UnsignedTransaction}
import yaccoin.utils.MiningUtils

import scala.collection.immutable.TreeSet
import scala.language.postfixOps

object MineTimeTest extends App {

  val (privateKey: PrivateKey, publicKey: PublicKey) = MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)

  val transactions = TreeSet(
    UnsignedTransaction(publicKey, MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)._2, 250).sign(privateKey),
    UnsignedTransaction(publicKey, MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)._2, 250).sign(privateKey),
    UnsignedTransaction(publicKey, MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)._2, 250).sign(privateKey)
  )

  val start = System.currentTimeMillis

  val block = MiningUtils.mineBlock(Block(transactions, MiningUtils.hashFunction(Array.emptyByteArray)))

  println(Block.verifyIntegrity(block), (System.currentTimeMillis - start) / 1000)

}
