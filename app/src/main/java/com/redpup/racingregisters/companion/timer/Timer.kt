package com.redpup.racingregisters.companion.timer

import kotlin.concurrent.timer
import java.util.Timer as JavaTimer

/**
 * Timer class that counts down from a specified number of seconds.
 */
class Timer(internal val initialSeconds: Int, private val delay: Long = 1000L) {
  var secondsRemaining = initialSeconds; internal set
  var timer: JavaTimer? = null; private set
  private var subscriber: (() -> Unit)? = null

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
    secondsRemaining = initialSeconds
  }

  /** Activates this timer. Does nothing if already active or if this timer is already elapsed. */
  private fun activate() {
    if (timer == null && secondsRemaining > 0) {
      timer = timer("Timer", true,  delay, delay) { tick() }
    }
  }

  /** Deactivates this timer. Does nothing if currently active. */
  private fun deactivate() {
    timer?.cancel()
    timer = null
  }

  /** Sets the subscriber to this timer. This will be invoked on every tick. */
  fun subscribe(sub: () -> Unit) {
    subscriber = sub
  }

  /** Timer tick that is invoked once a second. Invokes subscriber, if any. */
  @Synchronized
  private fun tick() {
    secondsRemaining--
    subscriber?.invoke()

    if (secondsRemaining == 0) {
      deactivate()
    }
  }

  override fun toString(): String {
    val remaining = secondsRemaining
    if (secondsRemaining == 0) {
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