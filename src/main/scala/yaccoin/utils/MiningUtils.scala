package yaccoin.utils

import scorex.crypto.hash.{Blake2b512, CryptographicHash64}
import scorex.crypto.signatures.{Curve25519, EllipticCurveSignatureScheme}
import yaccoin.block.Block

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.Random

/** Utility for various mining problems. */
object MiningUtils {

  /** Curve to generate and verify signatures. */
  val signatureCurve: EllipticCurveSignatureScheme = Curve25519

  /** Cryptographic hash function to use. */
  val hashFunction: CryptographicHash64 = Blake2b512

  /** How often to mine blocks. */
  val mineInterval: FiniteDuration = 5.minutes

  /** Number of leading bits of the hash that need to be zero. */
  val difficulty: Int = 20

  private val initialZeroBytes: Array[Byte] = Array.ofDim[Byte](difficulty / 8)
  private val remainingZeroBits: Byte = (255 >> (difficulty % 8)).toByte

  /** Test if the mined block is difficult enough.
    *
    * @param message Header of block mined.
    * @return Whether the block is difficult enough.
    */
  def difficultyFunction(message: Array[Byte]): Boolean = {
    val hash = hashFunction(message)

    (hash.length * 8 > difficulty) &&                                   // Length of hash is larger than difficulty
      hash.startsWith(initialZeroBytes) &&                              // The first (d - (d % 8)) bytes are 0
      ((hash(difficulty / 8) | remainingZeroBits) == remainingZeroBits) // The remaining bytes are also zero
  }

  /** Mining the block.
    *
    * If block is a valid one (passes the difficultyFunction test),
    * return it, otherwise, generate a random nonce and repeat.
    *
    * @param block The block to be mined.
    * @return The valid mined block.
    */
  @tailrec
  def mineBlock(block: Block): Block =
    if (difficultyFunction(block.header)) {
      block
    } else {
      mineBlock(block.copy(blockId = System.currentTimeMillis, nonce = Random.nextLong))
    }

}
