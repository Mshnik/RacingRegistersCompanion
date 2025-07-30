package com.redpup.racingregisters.companion

import android.util.Log
import androidx.lifecycle.ViewModel
import com.redpup.racingregisters.companion.timer.TimerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** A view model of the state of the background. */
class BackgroundViewModel(val timer: TimerViewModel) : ViewModel() {
  val shift = timer.elapsedMilliIncrements.map { it / 1000F }
  val shiftFactor = timer.numResumes.map { it.toFloat() }
  val previousShift = MutableStateFlow(0F)
  val previousTotal = MutableStateFlow(0F)

  /** Accumulates values from shift and shiftFactor into previousShift and previousTotal. */
  suspend fun accumulate() {
    val s = shift.first()
    val sF = shiftFactor.first()
    previousTotal.value += (s - previousShift.value) * sF
    previousShift.value = s
  }
}