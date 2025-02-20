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
  RESET
}

/**
 * Timer class that counts down from a specified number of seconds.
 */
class Timer(
  internal val initialSeconds: Int,
  internal val ticksPerSecond: Int = 100,
) {
  init {
    require(ticksPerSecond > 0 && 1000 % ticksPerSecond == 0) {
      "ticksPerSecond must be positive and evenly divide 1000."
    }
  }

  var ticks = 0; internal set
  var numResumes = 0; private set

  private val timerLock = Object()

  @GuardedBy("timerLock")
  var timer: JavaTimer? = null; private set

  private val tickTime = 1000L / ticksPerSecond
  private val eventHandler = EventHandler<Event>()

  /** The amount of milliseconds that have passed. */
  fun elapsedMillis(): Long {
    return ticks * 1000L / ticksPerSecond
  }

  /** The whole number of elapsed seconds. */
  fun elapsedSeconds(): Int {
    return ticks / ticksPerSecond
  }

  /** The whole number of remaining seconds. */
  fun remainingSeconds(): Int {
    return max(0, initialSeconds - elapsedSeconds())
  }

  /** Returns whether this timer is currently active. */
  fun isActive() : Boolean {
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
      eventHandler.handleSubscribers(Event.RESET)
    }
  }

  /** Activates this timer. Does nothing if already active or if this timer is already elapsed. */
  private fun activate() {
    synchronized(timerLock) {
      if (timer == null && remainingSeconds() > 0) {
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

  /** Timer tick that is invoked once a second. Invokes subscriber, if any. */
  @Synchronized
  private fun tick() {
    ticks++
    eventHandler.handleSubscribers(Event.TICK)

    if (ticks % ticksPerSecond == 0) {
      eventHandler.handleSubscribers(Event.SECOND)

      if (remainingSeconds() == 0) {
        deactivate()
      }
    }
  }

  override fun toString(): String {
    val remaining = remainingSeconds()
    if (remaining == 0) {
      return "DONE"
    }

    val minutes = remaining / 60
    val seconds = remaining % 60
    if (seconds < 10) {
      return "${minutes}:0$seconds"
    } else {
      return "${minutes}:$seconds"
    }
  }

  /**
   * Adds a subscriber to this timer for the given event.
   */
  fun subscribe(event: Event, sub: () -> Unit) {
    eventHandler.subscribe(event, sub)
  }

  /** Clears any existing subscribers. */
  fun clearSubscribers() {
    eventHandler.clearSubscribers()
  }
}