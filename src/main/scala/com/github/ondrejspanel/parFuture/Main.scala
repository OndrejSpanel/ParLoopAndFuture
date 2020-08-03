package com.github.ondrejspanel.parFuture

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object Main extends App {

  def hotSleep(ms: Int): Unit = {
    val now = System.currentTimeMillis()
    while (System.currentTimeMillis() < now + ms) {}

  }

  object Quick {
    val rng = new Random()
  }

  case class Quick() {
    def simulate: Quick = {
      hotSleep(1 + Quick.rng.nextInt(2))
      this
    }
  }

  val slowDuration = 100

  object Background extends Thread("Background") {
    val rng = new Random()

    @tailrec
    override def run(): Unit = {
      Future {
        hotSleep(slowDuration + rng.nextInt(10))
      }
      hotSleep(slowDuration + rng.nextInt(10))
      run()
    }

    setDaemon(true)
  }

  object Foreground {
    def run(): Unit = {
      val state = List.fill[Quick](100)(Quick()).par

      val finalState = (0 until 10000).foldLeft(state) {(state,i) =>
        val innerScopeBeg = System.currentTimeMillis()
        val result = state.map(_.simulate)
        val innerScopeEnd = System.currentTimeMillis()
        val duration = innerScopeEnd - innerScopeBeg
        if (duration >= slowDuration) {
          println(s"Suspicious duration $duration")
        }
        result
      }

    }
  }

  Background.start()
  Foreground.run()

}
