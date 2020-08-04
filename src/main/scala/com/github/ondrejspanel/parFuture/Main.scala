package com.github.ondrejspanel.parFuture

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {


  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  def run(): Unit = {
    val stateCount = 200
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

  run()

}
