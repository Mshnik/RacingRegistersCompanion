package com.redpup.racingregisters.companion.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redpup.racingregisters.companion.event.EventBus
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

/** Constructor overload for [TimerViewModel] that takes a constant [initialIncrements]. */
fun TimerViewModel(
  coroutineScope: CoroutineScope,
  initialIncrements: Int,
  millisPerIncrement: Int = 1000,
  ticksPerIncrement: Int = 100,
  completeAtIncrements: Int = 0,
  completionMessage: String = "DONE",
): TimerViewModel {
  return TimerViewModel(
    coroutineScope,
    MutableStateFlow(initialIncrements),
    millisPerIncrement,
    ticksPerIncrement,
    completeAtIncrements,
    completionMessage,
  )
}

/**
 * View model wrapping a game timer.
 *
 * Timers run on "increments", where each increment corresponds to a variable number of milliseconds.
 *
 * @param coroutineScope Scope to run blocking flows within.
 * @param initialIncrements Initial number of increments to set the timer to.
 *                          Read at initialization and whenever [reset] is invoked.
 * @param millisPerIncrement Number of milliseconds per increment (converts from "increments" to real time).
 * @param ticksPerIncrement Amount of ways to subdivide an increment.
 * @param completeAtIncrements Number of increments to call "done".
 * @param completionMessage A message to show when the timer is "done".
 * */
class TimerViewModel(
  private val coroutineScope: CoroutineScope,
  internal val initialIncrements: StateFlow<Int>,
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

  /** Number of ticks that have occurred on this timer so far. */
  internal val ticks = MutableStateFlow(0)

  /** Number of times that this timer has been resumed. */
  internal val numResumes = MutableStateFlow(0)

  /** True iff this timer is currently running. */
  internal val isRunning = MutableStateFlow(false)

  /** The currently running timer job, if any. */
  private var timerJob: Job? = null

  val eventBus = EventBus<Event>()
  val incrementBus = EventBus<Int>()

  /**
   * Returns whether this is a count down timer.
   * If false, this counts upwards forever.
   */
  fun isCountDown(): Boolean {
    return initialIncrements.value > 0
  }

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
  private fun remainingIncrements(
    ticks: Int = this.ticks.value,
    initialIncrements: Int = this.initialIncrements.value,
  ): Int {
    return max(0, initialIncrements - elapsedIncrements(ticks))
  }

  val remainingIncrements: Flow<Int> = this.ticks
    .combine(initialIncrements) { ticks, increments -> remainingIncrements(ticks, increments) }
    .distinctUntilChanged()

  /** Whether or not this timer is "complete" (which is different from finished). */
  private fun isComplete(increments: Int = remainingIncrements()): Boolean {
    return increments <= completeAtIncrements
  }

  val isComplete: Flow<Boolean> =
    this.remainingIncrements.map { isComplete(it) }.distinctUntilChanged()

  /** Scales the speed of this timer to match [durationMillis]. */
  fun scaleSpeed(durationMillis: Int) {
    val timerIntervalDuration =
      (durationMillis / remainingIncrements().toFloat()).toInt()
    setSpeed(timerIntervalDuration, 1)
  }

  /**
   * Sets the speed of this timer. This can only be done when the timer is not running and
   * has been reset.
   */
  fun setSpeed(millisPerIncrement: Int, ticksPerIncrement: Int) {
    check(!isRunning.value && ticks.value == 0)
    check(ticksPerIncrement > 0 && millisPerIncrement % ticksPerIncrement == 0) {
      "ticksPerIncrement must be positive and evenly divide $millisPerIncrement."
    }
    this.millisPerIncrement = millisPerIncrement
    this.ticksPerIncrement = ticksPerIncrement
  }

  /**
   * Starts the timer. If the timer is already running, this does nothing.
   */
  suspend fun start() {
    val countDown = isCountDown()
    if (isRunning.value || (countDown && remainingIncrements() == 0)) {
      return
    }

    isRunning.value = true
    val delay = (millisPerIncrement / ticksPerIncrement).toLong()
    numResumes.value++

    timerJob = viewModelScope.launch {
      while ((!countDown || remainingIncrements() > 0) && isRunning.value) {
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

      if (isCountDown()) {
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
  }

  /**
   * Formats the time in seconds to a MM:ss string.
   */
  fun formatTime(remaining: Int = remainingIncrements() - completeAtIncrements): String {
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

  val formattedTime: Flow<String> =
    remainingIncrements.map { formatTime(it - completeAtIncrements) }
      .distinctUntilChanged()
}