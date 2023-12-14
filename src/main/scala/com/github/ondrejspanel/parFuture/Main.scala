package com.github.ondrejspanel.parFuture

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ExecutionContextTaskSupport

object Main {
  val useCustomEC = true

  implicit val ec: ExecutionContext = if (useCustomEC) CustomEC.custom else ExecutionContext.global

  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  def main(args: Array[String]): Unit = {
    val stateCount = 50
    val state = (0 until stateCount).par

    state.tasksupport = new ExecutionContextTaskSupport(ec)

    val triggerIssue = true
    val start = System.currentTimeMillis()
    (0 until 100).foreach { i =>
      hotSleep(25)
      val innerScopeBeg = System.currentTimeMillis()
      if (!triggerIssue) {
        Future {
          hotSleep(105)
        }
      }
      state.foreach { x =>
        if (triggerIssue && x == 0) {
          Future {
            hotSleep(105)
          }
        }
        hotSleep(1)
      }
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
