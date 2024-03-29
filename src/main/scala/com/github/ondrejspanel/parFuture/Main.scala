package com.github.ondrejspanel.parFuture

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.parallel.CollectionConverters._

object Main {


  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  def main(args: Array[String]): Unit = {
    val stateCount = 50
    val state = (0 until stateCount).par

    val triggerIssue = true

    (0 until 1000).foreach { i =>
      hotSleep(25)
      val innerScopeBeg = System.currentTimeMillis()
      if (!triggerIssue) {
        Future {
          hotSleep(205)
        }
      }
      state.foreach { x =>
        if (triggerIssue && x == 0) {
          Future {
            hotSleep(205)
          }
        }
        hotSleep(2)
      }
      val innerScopeEnd = System.currentTimeMillis()
      val duration = innerScopeEnd - innerScopeBeg
      if (duration >= 200) {
        println(s"Suspicious background duration $duration")
      }
    }
  }

}
