package com.github.ondrejspanel.parFuture

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, LinkedBlockingDeque, LinkedBlockingQueue, Semaphore, ThreadFactory}
import scala.annotation.{tailrec, unused}
import scala.concurrent._
import scala.util.chaining._

object CustomEC {
  // many new Intel CPUs report plenty of cores, as they include "economical" cores as well
  // for our purposes, we do not want to run on them. Extra high concurrency levels make no sense anyway
  val maxCPUs = 8

  val daemonThreadFactory: ThreadFactory = new ThreadFactory {
    val defaultFactory = Executors.defaultThreadFactory()

    def newThread(r: Runnable): Thread = {
      val thread = defaultFactory.newThread(r)
      thread.setDaemon(true)
      thread
    }
  }
  val threadCount = scala.collection.parallel.availableProcessors min maxCPUs

  final implicit object custom extends ExecutionContextExecutor {
    private val taskQueue = new LinkedBlockingQueue[Runnable]()

    private object SingleThread extends Runnable {
      override def run(): Unit = {
        @tailrec
        def recurse(): Unit = {
          val task = taskQueue.take()
          task.run()
          recurse()
        }
        recurse()
      }
    }

    @unused // we only want the threads to be created, but having the variable around can be usefull for debugging
    private val threads = Array.fill(threadCount)(daemonThreadFactory.newThread(SingleThread).tap(_.start))

    override def reportFailure(cause: Throwable): Unit = ExecutionContext.defaultReporter(cause)
    override def execute(command: Runnable): Unit = {
      taskQueue.add(command)
    }
  }

}
