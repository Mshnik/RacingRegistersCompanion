package com.redpup.racingregisters.companion.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.annotations.VisibleForTesting
import kotlin.math.max
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
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

  private val _ticks = MutableStateFlow(0)
  val ticks: StateFlow<Int> = _ticks

  @VisibleForTesting
  internal fun setTicksForTest(ticks: Int) {
    _ticks.value = ticks
  }

  private val _numResumes = MutableStateFlow(0)
  val numResumes: StateFlow<Int> = _numResumes

  private val _isRunning = MutableStateFlow(false)
  val isRunning: StateFlow<Boolean> = _isRunning

  private var timerJob: Job? = null

  /** The amount of milli-Increments that have passed. */
  private fun elapsedMilliIncrements(ticks: Int = _ticks.value): Int {
    return ticks * millisPerIncrement / ticksPerIncrement
  }

  val elapsedMilliIncrements: Flow<Int> =
    ticks.map { elapsedMilliIncrements(it) }.distinctUntilChanged()

  /** The whole number of elapsed increments. */
  private fun elapsedIncrements(ticks: Int = _ticks.value): Int {
    return ticks / ticksPerIncrement
  }

  val elapsedIncrements: Flow<Int> = ticks.map { elapsedIncrements(it) }.distinctUntilChanged()


  /** The whole number of remaining increments. */
  private fun remainingIncrements(ticks: Int = _ticks.value): Int {
    return max(0, initialIncrements - elapsedIncrements(ticks))
  }

  val remainingIncrements: Flow<Int> = ticks.map { remainingIncrements(it) }
    .distinctUntilChanged()

  /**
   * Starts the timer. If the timer is already running, this does nothing.
   */
  fun start() {
    if (_isRunning.value || timerJob?.isActive == true || remainingIncrements() == 0) {
      return
    }

    _isRunning.value = true
    val delay = (millisPerIncrement / ticksPerIncrement).toLong()
    _numResumes.value++

    timerJob = viewModelScope.launch {
      while (remainingIncrements() > 0 && _isRunning.value) {
        delay(delay)
        tick()
      }
    }
  }

  private fun tick() {
    if (!_isRunning.value) {
      return
    }

    _ticks.value++
    if (_ticks.value % ticksPerIncrement == 0) {
      val remainingIncrements = remainingIncrements()
      if (remainingIncrements == 0) {
        pause()
      }
    }
  }

  /**
   * Pauses the timer.
   */
  fun pause() {
    _isRunning.value = false
    timerJob?.cancel()
  }

  /**
   * Resets the timer to the initial state.
   */
  fun reset() {
    _isRunning.value = false
    timerJob?.cancel()
    _ticks.value = 0
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