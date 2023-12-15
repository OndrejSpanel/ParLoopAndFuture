package com.github.ondrejspanel.parFuture

import scala.concurrent.{ExecutionContext, Future}

import MyPar._

object Main {
  val useCustomEC = false

  implicit val ec: ExecutionContext = if (useCustomEC) CustomEC.custom else ExecutionContext.global

  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  def main(args: Array[String]): Unit = {
    val stateCount = 50
    val state = (0 until stateCount).par

    val shortTime = 2
    val longTime = 200

    val triggerIssue = true
    val start = System.currentTimeMillis()
    (0 until 1000).foreach { i =>
      hotSleep(25)
      val innerScopeBeg = System.currentTimeMillis()
      if (!triggerIssue) {
        Future {
          hotSleep(longTime + 5)
        }
      }
      state.foreach { x =>
        if (triggerIssue && x == i % 10) {
          Future {
            hotSleep(longTime + 5)
          }
        }
        hotSleep(shortTime)
      }
      val innerScopeEnd = System.currentTimeMillis()
      val duration = innerScopeEnd - innerScopeBeg
      if (duration >= longTime) {
        println(s"Suspicious background duration $duration")
      }
    }

    val end = System.currentTimeMillis()
    println(s"Duration ${end - start}")
  }


}
