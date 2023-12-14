package com.github.ondrejspanel.parFuture

import java.util.concurrent.{Executors, ThreadFactory}
import scala.concurrent._

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
  val threadPool = Executors.newFixedThreadPool(scala.collection.parallel.availableProcessors min maxCPUs, daemonThreadFactory)
  final lazy implicit val custom: ExecutionContextExecutor = ExecutionContext.fromExecutor(threadPool)

}
