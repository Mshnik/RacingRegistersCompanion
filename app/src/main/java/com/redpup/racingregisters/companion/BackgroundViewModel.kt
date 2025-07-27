package com.redpup.racingregisters.companion

import androidx.lifecycle.ViewModel
import com.redpup.racingregisters.companion.timer.TimerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

/** A view model of the state of the background. */
class BackgroundViewModel(val timer: TimerViewModel) : ViewModel() {
  val shift = timer.elapsedMilliIncrements.map { it / 1000F }
  val shiftFactor = timer.numResumes.map { it.toFloat() }
  val previousShift = MutableStateFlow(0F)
  val previousTotal = MutableStateFlow(0F)

  /** Accumulates values from shift and shiftFactor into previousShift and previousTotal. */
  suspend fun accumulate() {
    combine(shift, shiftFactor) { s, sF -> Pair(s, sF) }
      .take(1)
      .collect {
        previousTotal.value += (it.first - previousShift.value) * it.second
        previousShift.value = it.first
      }
  }
}