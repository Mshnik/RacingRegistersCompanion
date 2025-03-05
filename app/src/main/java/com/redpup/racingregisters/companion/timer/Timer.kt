package com.redpup.racingregisters.companion.timer

import androidx.annotation.GuardedBy
import com.redpup.racingregisters.companion.event.EventHandler
import kotlin.concurrent.timer
import java.util.Timer as JavaTimer
import kotlin.math.max

/**
 * Events that can occur on a timer.
 * Any state change related to the event has already occurred before subscribers are called.
 */
enum class Event {
  TICK,
  SECOND,
  ACTIVATE,
  DEACTIVATE,
  COMPLETE,
  FINISH
}

/**
 * Timer class that counts down from a specified number of increments.
 *
 * Each increment is made up of some number of ticks, which is the smallest increment of time.
 * Events are fired both on tick and on increment.
 */
class Timer(
  internal val initialIncrements: Int,
  internal var millisPerIncrement: Long = 1000L,
  internal var ticksPerIncrement: Int = 100,
  internal val completeAtIncrements: Int = 0,
  internal val completionMessage: String = "DONE",
) {
  init {
    check(ticksPerIncrement > 0 && millisPerIncrement % ticksPerIncrement == 0L) {
      "ticksPerIncrement must be positive and evenly divide $millisPerIncrement."
    }
    require(completeAtIncrements >= 0)
  }

  var ticks = 0; internal set
  var numResumes = 0; private set

  private val timerLock = Object()

  @GuardedBy("timerLock")
  var timer: JavaTimer? = null; private set

  val eventHandler = EventHandler<Event>()
  val incrementHandler = EventHandler<Int>()

  /** The amount of milli-Increments that have passed. */
  fun elapsedMilliIncrements(): Long {
    return ticks * millisPerIncrement / ticksPerIncrement
  }

  /** The whole number of elapsed increments. */
  fun elapsedIncrements(): Int {
    return ticks / ticksPerIncrement
  }

  /** The whole number of remaining increments. */
  fun remainingIncrements(): Int {
    return max(0, initialIncrements - elapsedIncrements())
  }

  /** Returns whether this timer is currently active. */
  fun isActive(): Boolean {
    synchronized(timerLock) {
      return timer != null;
    }
  }

  /** Starts this timer. Does nothing if already started. */
  fun start() {
    activate()
  }

  /** Pauses this timer. Does nothing if not already started. */
  fun pause() {
    deactivate()
  }

  /** Either starts or pauses this timer, toggling between states. */
  fun toggle() {
    synchronized(timerLock) {
      if (timer == null) {
        activate()
      } else {
        deactivate()
      }
    }
  }

  /** Resets this timer. Does nothing if not yet started. */
  fun reset() {
    if (ticks != 0) {
      deactivate()
      ticks = 0
      numResumes = 0
    }
  }

  /**
   * Sets the speed of this timer. This can only be done when the timer is not running and
   * has been reset.*/
  fun setSpeed(millisPerIncrement: Long, ticksPerIncrement: Int) {
    check(!isActive() && ticks == 0)
    check(ticksPerIncrement > 0 && millisPerIncrement % ticksPerIncrement == 0L) {
      "ticksPerIncrement must be positive and evenly divide $millisPerIncrement."
    }
    this.millisPerIncrement = millisPerIncrement
    this.ticksPerIncrement = ticksPerIncrement
  }

  /** Activates this timer. Does nothing if already active or if this timer is already elapsed. */
  private fun activate() {
    synchronized(timerLock) {
      if (timer == null && remainingIncrements() > 0) {
        val tickTime = millisPerIncrement / ticksPerIncrement
        timer = timer("Timer", true, tickTime, tickTime) { tick() }
        numResumes++
        eventHandler.handleSubscribers(Event.ACTIVATE)
      }
    }
  }

  /** Deactivates this timer. Does nothing if currently active. */
  private fun deactivate() {
    synchronized(timerLock) {
      if (timer != null) {
        timer!!.cancel()
        timer = null
        eventHandler.handleSubscribers(Event.DEACTIVATE)
      }
    }
  }

  /** Timer tick that is invoked once a increment. Invokes subscriber, if any. */
  @Synchronized
  private fun tick() {
    ticks++
    eventHandler.handleSubscribers(Event.TICK)

    if (ticks % ticksPerIncrement == 0) {
      eventHandler.handleSubscribers(Event.SECOND)

      val remainingIncrements = remainingIncrements()
      incrementHandler.handleSubscribers(remainingIncrements)
      if (remainingIncrements == completeAtIncrements) {
        eventHandler.handleSubscribers(Event.COMPLETE)
      }

      if (remainingIncrements() == 0) {
        deactivate()
        eventHandler.handleSubscribers(Event.FINISH)
      }
    }
  }

  override fun toString(): String {
    val remaining = remainingIncrements() - completeAtIncrements
    if (remaining <= 0) {
      return completionMessage
    }

    val minutes = remaining / 60
    val increments = remaining % 60
    if (minutes == 0) {
      return "$increments"
    } else if (increments < 10) {
      return "${minutes}:0$increments"
    } else {
      return "${minutes}:$increments"
    }
  }
}