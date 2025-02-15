package com.redpup.racingregisters.companion.timer

import com.google.common.collect.ArrayListMultimap
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
  var timer: JavaTimer? = null; private set
  var numResumes = 0; private set

  private val tickTime = 1000L / ticksPerSecond
  private val subscribers = ArrayListMultimap.create<Event, () -> Unit>()

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
    if (timer == null) {
      start()
    } else {
      pause()
    }
  }

  /** Resets this timer. Does nothing if not yet started. */
  fun reset() {
    if (ticks != 0) {
      deactivate()
      ticks = 0
      handleSubscribers(Event.RESET)
    }
  }

  /** Activates this timer. Does nothing if already active or if this timer is already elapsed. */
  private fun activate() {
    if (timer == null && remainingSeconds() > 0) {
      timer = timer("Timer", true, tickTime, tickTime) { tick() }
      numResumes++
      handleSubscribers(Event.ACTIVATE)
    }
  }

  /** Deactivates this timer. Does nothing if currently active. */
  private fun deactivate() {
    if (timer != null) {
      timer?.cancel()
      timer = null
      handleSubscribers(Event.DEACTIVATE)
    }
  }

  /** Timer tick that is invoked once a second. Invokes subscriber, if any. */
  @Synchronized
  private fun tick() {
    ticks++
    handleSubscribers(Event.TICK)

    if (ticks % ticksPerSecond == 0) {
      handleSubscribers(Event.SECOND)

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
    subscribers.put(event, sub)
  }

  /** Clears any existing subscribers. */
  fun clearSubscribers() {
    subscribers.clear()
  }

  /** Handles all subscribers registered for the given Event. */
  private fun handleSubscribers(event: Event) {
    subscribers.get(event).forEach { it.invoke() }
  }
}