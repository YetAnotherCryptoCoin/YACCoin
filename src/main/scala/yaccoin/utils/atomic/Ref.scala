package yaccoin.utils.atomic

/** A Scala interface for Java's AtomicReference.
  *
  * @tparam T Generic type for the reference.
  */
trait Ref[T] {

  /** Dereference. */
  def get: T

  /** Set the underlying reference.
    *
    * @param t Value to set.
    */
  def set(t: T): Unit

  /** Transform the underlying reference and return it.
    *
    * @param func Transformation function.
    * @return The object after transformation.
    */
  def transformAndGet(func: T => T): T

}

/** Companion object for Ref. */
object Ref {

  /** Return a Ref[T] for the given initial argument.
    *
    * @param initial The initial value of reference.
    * @tparam T Type of ref.
    * @return The Ref[T] corresponding to initial.
    */
  def apply[T <: AnyRef](initial: T): Ref[T] = new RefAny[T](initial)
  
}
