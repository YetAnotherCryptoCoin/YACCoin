package yaccoin.utils.atomic

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec

/** Implementation of Ref[T] for a subclass AnyRef.
  *
  * @param initial initial value of reference.
  * @tparam T Generic type for the reference.
  */
class RefAny[T <: AnyRef](initial: T) extends Ref[T] {

  private val instance: AtomicReference[T] = new AtomicReference[T](initial)

  override def get: T = instance.get

  override def set(t: T): Unit = instance.set(t)

  /** Implementing transformAndGet using CAS.
    *
    * @param func Transformation function.
    * @return The object after transformation.
    */
  @tailrec
  final override def transformAndGet(func: T => T): T = {
    val oldValue = instance.get()
    val transformedValue = func(oldValue)

    if (instance.compareAndSet(oldValue, transformedValue)) {
      transformedValue
    } else {
      transformAndGet(func)
    }
  }

}
