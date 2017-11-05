package yaccoin.utils

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

/** Utility for serialisation and de-serialisation of generic Scala objects. */
object SerialisationUtils {

  /** Serialise the given object.
    *
    * @param value Object to serialise.
    * @tparam A Type of the object to serialise.
    * @return The byte array containing the serialised representation.
    */
  def serialise[A <: Any with Serializable](value: A): Array[Byte] = {
    val stream = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }

  /** De-serialise the given object.
    *
    * @param bytes The byte array containing serialised representation.
    * @tparam A The type of the object to infer.
    * @return The de-serialised object.
    */
  def deserialise[A <: Any with Serializable](bytes: Array[Byte]): A = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value.asInstanceOf[A]
  }

}
