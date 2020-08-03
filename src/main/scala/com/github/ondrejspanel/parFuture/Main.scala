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
      if (Quick.rng.nextInt(500) < 1) {
        val sleep = slowDuration + Quick.rng.nextInt(10)
        Future {
          hotSleep(sleep)
        }
      }
      this
    }
  }

  val slowDuration = 200

  class Background(index: Int) extends Thread(s"Background$index") {
    private val rng = new Random()

    private var backgroundState = List.fill[Quick](200)(Quick()).par

    @tailrec
    final override def run(): Unit = {
      hotSleep(rng.nextInt(slowDuration + 10) + slowDuration)

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

      hotSleep(5 + rng.nextInt(5))
      run()
    }

    setDaemon(true)
  }

  val nBackground = 1
  val backgrounds = List.tabulate(nBackground)(new Background(_))

  object Foreground {
    val rng = new Random()

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
        hotSleep(2 + rng.nextInt(2))
        result
      }

    }
  }

  backgrounds.foreach(_.start())
  Foreground.run()

}
