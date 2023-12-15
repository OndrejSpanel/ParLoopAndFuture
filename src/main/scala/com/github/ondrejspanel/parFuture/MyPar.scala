package com.github.ondrejspanel.parFuture

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

object MyPar {
  val numberOfProcessors: Int = Runtime.getRuntime.availableProcessors()

  def parallelMap[T, S](coll: Iterable[T])(f: T => S)(implicit ec: ExecutionContext): Iterable[S] = {
    // use Any so that we can use Array without a ClassTag
    // note: we could use the same array for source / result
    val source = Array.ofDim[Any](coll.size)
    val result = Array.ofDim[Any](coll.size)
    val taskCount = coll.size
    val taskIndex = new AtomicInteger(0)
    val tasksDone = new AtomicInteger(0)
    coll.copyToArray(source)

    @tailrec
    def worker(): Boolean = {
      val index = taskIndex.getAndIncrement()
      if (index < taskCount) {
        result(index) = f(source(index).asInstanceOf[T])
        tasksDone.getAndIncrement()
        worker()
      } else false
    }

    val maxConcurrency = numberOfProcessors - 1 // calling thread is doing work as well
    val numberOfWorkers = taskCount - 1 min maxConcurrency
    var i = 0
    while (i < numberOfWorkers) {
      Future(worker())
      i += 1
    }
    // perform tasks on the main thread while waiting for futures to complete
    worker()
    // busy-wait for the rest of the futures
    while (tasksDone.get() < taskCount) {
      Thread.onSpinWait()
    }
    result.asInstanceOf[Array[S]]
  }

  final class ParMapWrapper[K, V](coll: Map[K, V]) {
    def flatMap[S](f: ((K, V)) => Iterable[S])(implicit ec: ExecutionContext): Iterable[S] = parallelMap(coll)(f).flatten

    def map[T](f: ((K, V)) => (K, T))(implicit ec: ExecutionContext): Map[K, T] = parallelMap(coll)(f).toMap

    def foreach(f: ((K, V)) => Unit)(implicit ec: ExecutionContext): Unit = parallelMap(coll)(f)
  }

  final class ParIterableWrapper[S](coll: Iterable[S]) {
    def flatMap[T](f: S => IterableOnce[T])(implicit ec: ExecutionContext): Iterable[T] = parallelMap(coll)(f).flatten

    def map[T](f: S => T)(implicit ec: ExecutionContext): Iterable[T] = parallelMap(coll)(f)

    def foreach(f: S => Unit)(implicit ec: ExecutionContext): Unit = parallelMap(coll)(f)
  }

  implicit class ParIterable[S](private val coll: Iterable[S]) extends AnyVal {
    def par: ParIterableWrapper[S] = new ParIterableWrapper(coll)
  }

  implicit class ParMap[K, V](private val coll: Map[K, V]) extends AnyVal {
    def par: ParMapWrapper[K, V] = new ParMapWrapper(coll)
  }
}
