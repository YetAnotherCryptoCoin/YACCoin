import scorex.crypto.signatures.{Curve25519, PrivateKey, PublicKey}

object PublicKeyTest extends App {

  val (privateKey: PrivateKey, publicKey: PublicKey) = Curve25519.createKeyPair(Array.emptyByteArray)

  println(publicKey.mkString(":"))

  def toByteArray(str: String): Array[Byte] = {
    str.split(':').map(_.toByte)
  }

  val read = toByteArray(publicKey.mkString(":"))

  println(read.sameElements(publicKey))

}
