package yaccoin.utils

import scala.util.{Failure, Success, Try}

/** Miscellaneous utility functions. */
object MiscUtils {

  /** Lift a boolean expression to a Try[Unit] with message.
    *
    * @param cond The condition.
    * @param message Message for the exception.
    * @return A Try[Unit]
    */
  def condToTry(cond: Boolean, message: String): Try[Unit] = if (cond) {
    Success(Unit)
  } else {
    Failure(new RuntimeException(message))
  }

}
