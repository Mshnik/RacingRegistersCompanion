package com.redpup.racingregisters.companion.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.annotations.VisibleForTesting
import com.redpup.racingregisters.companion.event.EventBus
import kotlin.math.max
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimerViewModel(
  internal val initialIncrements: Int,
  internal var millisPerIncrement: Int = 1000,
  internal var ticksPerIncrement: Int = 100,
  internal val completeAtIncrements: Int = 0,
  internal val completionMessage: String = "DONE",
) : ViewModel() {

  init {
    check(ticksPerIncrement > 0 && millisPerIncrement % ticksPerIncrement == 0) {
      "ticksPerIncrement must be positive and evenly divide $millisPerIncrement."
    }
    require(completeAtIncrements >= 0)
  }

  internal val ticks = MutableStateFlow(0)
  internal val numResumes = MutableStateFlow(0)
  internal val isRunning = MutableStateFlow(false)

  /** The currently running timer job, if any. */
  private var timerJob: Job? = null

  val eventBus = EventBus<Event>()
  val incrementBus = EventBus<Int>()

  /** The amount of milli-Increments that have passed. */
  private fun elapsedMilliIncrements(ticks: Int = this.ticks.value): Int {
    return ticks * millisPerIncrement / ticksPerIncrement
  }

  val elapsedMilliIncrements: Flow<Int> =
    ticks.map { elapsedMilliIncrements(it) }.distinctUntilChanged()

  /** The whole number of elapsed increments. */
  private fun elapsedIncrements(ticks: Int = this.ticks.value): Int {
    return ticks / ticksPerIncrement
  }

  val elapsedIncrements: Flow<Int> = ticks.map { elapsedIncrements(it) }.distinctUntilChanged()

  /** The whole number of remaining increments. */
  private fun remainingIncrements(ticks: Int = this.ticks.value): Int {
    return max(0, initialIncrements - elapsedIncrements(ticks))
  }

  val remainingIncrements: Flow<Int> = this.ticks.map { remainingIncrements(it) }
    .distinctUntilChanged()

  /**
   * Starts the timer. If the timer is already running, this does nothing.
   */
  suspend fun start() {
    if (isRunning.value || remainingIncrements() == 0) {
      return
    }

    isRunning.value = true
    val delay = (millisPerIncrement / ticksPerIncrement).toLong()
    numResumes.value++

    timerJob = viewModelScope.launch {
      while (remainingIncrements() > 0 && isRunning.value) {
        delay(delay)
        tick()
      }
    }

    eventBus.emit(Event.ACTIVATE)
  }

  /**
   * Pauses the timer.
   */
  suspend fun pause() {
    if (!isRunning.value) {
      return
    }

    isRunning.value = false
    timerJob?.cancel()
    eventBus.emit(Event.DEACTIVATE)
  }

  /**
   * Resets the timer to the initial state.
   */
  fun reset() {
    isRunning.value = false
    timerJob?.cancel()
    ticks.value = 0
  }

  /** Advances the timer one tick. */
  private suspend fun tick() {
    if (!isRunning.value) {
      return
    }

    ticks.value++
    eventBus.emit(Event.TICK)

    if (ticks.value % ticksPerIncrement == 0) {
      eventBus.emit(Event.SECOND)

      val remainingIncrements = remainingIncrements()
      incrementBus.emit(remainingIncrements)

      if (remainingIncrements == completeAtIncrements) {
        eventBus.emit(Event.COMPLETE)
      }

      if (remainingIncrements == 0) {
        pause()
        eventBus.emit(Event.FINISH)
      }
    }
  }

  /**
   * Formats the time in seconds to a MM:ss string.
   */
  fun formatTime(): String {
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