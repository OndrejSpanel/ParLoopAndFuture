package com.github.ondrejspanel.parFuture

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

object Main {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  def main(args: Array[String]): Unit = {
    val stateCount = 50
    val state = 0 until stateCount

    val shortTime = 2
    val longTime = 205

    val triggerIssue = true
    val start = System.currentTimeMillis()
    (0 until 1000).foreach { i =>
      hotSleep(25)
      val innerScopeBeg = System.currentTimeMillis()
      if (!triggerIssue) {
        Future {
          hotSleep(longTime)
        }
      }
      val futures = state.map { x =>
        if (triggerIssue && x == 0) {
          Future {
            hotSleep(longTime)
          }
        }
        Future {
          hotSleep(shortTime)
        }
      }
      Await.result(Future.sequence(futures), Duration.Inf)
      val innerScopeEnd = System.currentTimeMillis()
      val duration = innerScopeEnd - innerScopeBeg
      if (duration >= 100) {
        println(s"Suspicious background duration $duration")
      }
    }

    val end = System.currentTimeMillis()
    println(s"Duration ${end - start}")
  }


}
