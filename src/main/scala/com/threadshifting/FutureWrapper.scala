package com.threadshifting

import com.typesafe.scalalogging.StrictLogging
import io.vertx.core.Future

trait FutureWrapper[A] {
  def onComplete(continuation: Either[Throwable, A] => Unit): Unit

  def map[B](f: A => B): FutureWrapper[B] = {
    new Continuation[A, B](this, f)
  }
}


class RemoteCall[T](underlying: Future[T]) extends FutureWrapper[T] with StrictLogging {
  def onComplete(continuation: Either[Throwable, T] => Unit): Unit = {
    logger.info("RemoteCall.onComplete")
    underlying.onComplete { result =>
      logger.info("RemoteCall.onComplete.underlying")
      if (result.succeeded()) {
        continuation(Right(result.result()))
      } else {
        continuation(Left(result.cause()))
      }
    }
  }
}

case class Continuation[T, X](underlying: FutureWrapper[T], f: T => X) extends FutureWrapper[X] with StrictLogging {
  def onComplete(continuation: Either[Throwable, X] => Unit): Unit = {
    logger.info("Continuation.onComplete")
    underlying.onComplete {
      case Left(value) =>
        logger.info("Continuation.onComplete.underlying.Failed")
        continuation(Left(value))
      case Right(value) =>
        logger.info("Continuation.onComplete.underlying.Success")
        continuation(Right(f(value)))
    }
  }
}

case class Now[T](value: T) extends FutureWrapper[T] {
  def onComplete(continuation: Either[Throwable, T] => Unit): Unit = {
    continuation(Right(value))
  }
}

object FutureWrapper {
  def apply[A](f: Future[A]): FutureWrapper[A] = fromVertxFuture(f)

  def fromVertxFuture[T](future: Future[T]): FutureWrapper[T] = new RemoteCall(future)

  def now[T](value: T): FutureWrapper[T] = Now(value)
}

