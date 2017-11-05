package yaccoin

import akka.actor.Address
import scorex.crypto.signatures.{PrivateKey, PublicKey}
import yaccoin.block.{Block, UnsignedTransaction}
import yaccoin.utils.MiningUtils

import scala.collection.immutable.SortedSet
import scala.language.postfixOps

object YACCServerApp extends App {

  val yaccServers: List[Address] = List(
    Address("akka", "YACCActorSystem", "192.168.1.1", 8890)
  )

  val (privateKey: PrivateKey, publicKey: PublicKey) = MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)

  val transactions = SortedSet(
    UnsignedTransaction(publicKey, MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)._2, 250).sign(privateKey),
    UnsignedTransaction(publicKey, MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)._2, 250).sign(privateKey),
    UnsignedTransaction(publicKey, MiningUtils.signatureCurve.createKeyPair(Array.emptyByteArray)._2, 250).sign(privateKey)
  )

  val start = System.currentTimeMillis

  val block = MiningUtils.mineBlock(Block(transactions, MiningUtils.hashFunction(Array.emptyByteArray)))

  println(Block.verifyIntegrity(block), (System.currentTimeMillis - start) / 1000)

}
