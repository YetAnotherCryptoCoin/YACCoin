package yaccoin.utils.future

import scala.concurrent.{ExecutionContext, Future}

/** A trait for a cancellable future. */
trait CancellableFuture[T] {

  /** Return the Scala future. */
  def future: Future[T]

  /** Cancel the future. */
  def cancel(): Unit

}

object CancellableFuture {

  /** Get a CancellableFuture from function.
    *
    * @param todo Task to execute.
    * @param executionContext Ctx to execute in.
    * @tparam T Type of return of the future.
    * @return A CancellableFuture.
    */
  def apply[T](todo: => T)(implicit executionContext: ExecutionContext): CancellableFuture[T] =
    new JavaCancellableFuture[T](executionContext, todo)

  /** Get an already successful CancellableFuture from value.
    *
    * @param t Value to return.
    * @tparam T Type of return of the future.
    * @return A CancellableFuture.
    */
  def successful[T](t: T): CancellableFuture[T] = new CancellableFuture[T] {
    /* Do nothing. */
    override def cancel(): Unit = Unit

    /* Return the successful future. */
    override def future: Future[T] = Future.successful(t)
  }

  /** Get an already failed CancellableFuture from function.
    *
    * @param ex Reason/Cause.
    * @tparam T Type of return of the future.
    * @return A CancellableFuture.
    */
  def failed[T](ex: Throwable): CancellableFuture[T] = new CancellableFuture[T] {
    /* Do nothing. */
    override def cancel(): Unit = Unit

    /* Return the failed future. */
    override def future: Future[T] = Future.failed[T](ex)
  }

}
