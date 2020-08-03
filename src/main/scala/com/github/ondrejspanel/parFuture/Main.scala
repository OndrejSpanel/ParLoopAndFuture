package com.github.ondrejspanel.parFuture

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  object Background extends Thread("Background") {

    private val stateCount = 200
    private val state = (0 until stateCount).par

    @tailrec
    final override def run(): Unit = {

      (0 until 10000).foreach { i =>
        hotSleep(100)
        val innerScopeBeg = System.currentTimeMillis()

        Future {
          state.seq.foreach { x =>
            hotSleep(1)
            if ((x + i * stateCount) % 500 == 0) {
              Future {
                hotSleep(205)
              }
            }
          }
        }

        state.foreach { x =>
          hotSleep(2)
        }


        val innerScopeEnd = System.currentTimeMillis()
        val duration = innerScopeEnd - innerScopeBeg
        if (duration >= 200) {
          println(s"Suspicious background duration $duration")
        }
      }

      run()
    }

    setDaemon(true)
  }


  object Foreground {

    def run(): Unit = {
      Thread.sleep(60000)
    }
  }

  Background.start()
  Foreground.run()

}
