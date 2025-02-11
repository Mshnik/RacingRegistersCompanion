package com.redpup.racingregisters.companion

import androidx.compose.runtime.MutableState
import kotlin.concurrent.timer
import java.util.Timer as JavaTimer

/**
 * Timer class that counts down from a specified number of seconds.
 */
class Timer(seconds: Int) {
  private val initialSeconds = seconds
  private var secondsRemaining = seconds
  private var timer: JavaTimer? = null
  private var subscriber: MutableState<String>? = null

  /** Starts this timer. Does nothing if already started. */
  internal fun start() {
    activate()
  }

  /** Pauses this timer. Does nothing if not already started. */
  internal fun pause() {
    deactivate()
  }

  /** Resets this timer. Does nothing if not yet started. */
  internal fun reset() {
    deactivate()
    secondsRemaining = initialSeconds
  }

  /** Activates this timer. Does nothing if already active. */
  private fun activate() {
    if (timer == null) {
      timer = timer("Timer", true, 0, 1000L) { tick() }
    }
  }

  /** Deactivates this timer. Does nothing if currently active. */
  private fun deactivate() {
    timer?.cancel()
    timer = null
  }

  /** Sets the subscriber to this timer. This will be invoked on every tick. */
  fun subscribe(sub: MutableState<String>) {
    subscriber = sub
  }

  /** Timer tick that is invoked once a second. */
  @Synchronized
  private fun tick() {
    secondsRemaining--
    subscriber?.value = toString()

    if (secondsRemaining == 0) {
      deactivate()
    }
  }

  override fun toString(): String {
    val remaining = secondsRemaining
    val minutes = remaining / 60
    val seconds = remaining % 60
    if (seconds < 10) {
      return "${minutes}:0$seconds"
    } else {
      return "${minutes}:$seconds"
    }
  }
}