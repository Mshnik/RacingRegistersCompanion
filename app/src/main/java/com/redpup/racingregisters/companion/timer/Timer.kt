package com.redpup.racingregisters.companion.timer

import kotlin.concurrent.timer
import java.util.Timer as JavaTimer
import kotlin.math.max

/**
 * Timer class that counts down from a specified number of seconds.
 */
class Timer(
  internal val initialSeconds: Int,
  internal val ticksPerSecond: Int = 100,
) {
  init {
    require(ticksPerSecond > 0 && 1000 % ticksPerSecond  == 0) {
      "ticksPerSecond must be positive and evenly divide 1000."
    }
  }

  var ticks = 0; internal set

  var timer: JavaTimer? = null; private set
  var numResumes = 0; private set

  private val tickTime = 1000L / ticksPerSecond
  private val subscribers = mutableListOf<() -> Unit>()
  private val subSecondSubscribers = mutableListOf<() -> Unit>()

  /** The amount of milliseconds that have passed. */
  fun elapsedMillis() : Long {
    return ticks * 1000L / ticksPerSecond
  }

  /** The whole number of elapsed seconds. */
  fun elapsedSeconds() : Int {
    return ticks / ticksPerSecond
  }

  /** The whole number of remaining seconds. */
  fun remainingSeconds() : Int {
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
    deactivate()
    ticks = 0
  }

  /** Activates this timer. Does nothing if already active or if this timer is already elapsed. */
  private fun activate() {
    if (timer == null && remainingSeconds() > 0) {
      timer = timer("Timer", true, tickTime, tickTime) { tick() }
      numResumes++
    }
  }

  /** Deactivates this timer. Does nothing if currently active. */
  private fun deactivate() {
    timer?.cancel()
    timer = null
  }

  /**
   * Sets the subscriber to this timer. This will be invoked on every tick.
   *
   * Composes with any existing subscriber.
   */
  fun subscribe(sub: () -> Unit) {
    subscribers.add(sub)
  }

  /**
   * Sets the subscriber to this timer. This will be invoked on every sub-tick (including ticks).
   *
   * Composes with any existing subscriber.
   */
  fun subscribeSubSecond(sub: () -> Unit) {
    subSecondSubscribers.add(sub)
  }

  /** Clears any existing subscribers. */
  fun clearSubscribers() {
    subscribers.clear()
    subSecondSubscribers.clear()
  }

  /** Timer tick that is invoked once a second. Invokes subscriber, if any. */
  @Synchronized
  private fun tick() {
    ticks++
    subSecondSubscribers.forEach { it.invoke() }

    if (ticks % ticksPerSecond == 0) {
      subscribers.forEach { it.invoke()}

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
}