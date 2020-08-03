package com.github.ondrejspanel.parFuture

import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
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

  val slowDuration = 200

  class Background(index: Int) extends Thread(s"Background$index") {
    val rng = new Random()

    var backgroundState = List.fill[Quick](100)(Quick()).par

    @tailrec
    final override def run(): Unit = {
      val f = Future {
        hotSleep(slowDuration + rng.nextInt(10))
      }
      hotSleep(rng.nextInt(slowDuration + 10) + slowDuration / 4)

      backgroundState = (0 until 10000).foldLeft(backgroundState) { (state, i) =>
        val innerScopeBeg = System.currentTimeMillis()
        val result = state.map(_.simulate)
        val innerScopeEnd = System.currentTimeMillis()
        val duration = innerScopeEnd - innerScopeBeg
        if (duration >= slowDuration) {
          println(s"Suspicious background duration $duration")
        }
        result
      }

      Await.result(f, Duration.Inf)
      run()
    }

    setDaemon(true)
  }

  val nBackground = 6
  val backgrounds = List.tabulate(nBackground)(new Background(_))

  object Foreground {
    def run(): Unit = {
      val state = List.fill[Quick](10)(Quick())

      (0 until 5000).foldLeft(state) {(state,i) =>
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

  backgrounds.foreach(_.start())
  Foreground.run()

}
