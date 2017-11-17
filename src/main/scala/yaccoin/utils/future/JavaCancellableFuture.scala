package yaccoin.utils.future

import java.util.concurrent.FutureTask

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/** A container class for a Scala-ish cancellable future.
  *
  * @param ctx ExecutionContext to execute in.
  * @param todo Function to execute.
  * @tparam T Type of return of future.
  */
class JavaCancellableFuture[T](ctx: ExecutionContext, todo: => T) extends CancellableFuture[T] {

  /* Promise to encapsulate the blocking function. */
  private val promise = Promise[T]()

  /* Java Future for the job. */
  private val javaFuture: FutureTask[T] = new FutureTask[T](() => todo) {
    override def done(): Unit = promise.complete(Try(get()))
  }

  /** Return a Scala future.
    *
    * @return A Scala future for the job.
    */
  override def future: Future[T] = promise.future

  /** Cancel the future. */
  override def cancel(): Unit = if (!javaFuture.isDone) {
    javaFuture.cancel(true)
  }

  /* Execute the javaFuture. */
  ctx.execute(javaFuture)
}
