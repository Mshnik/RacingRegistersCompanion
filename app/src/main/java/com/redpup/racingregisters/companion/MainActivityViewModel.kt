package com.redpup.racingregisters.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

  // Total time for the timer in seconds (10 minutes)
  private val totalTime = 10 * 60L

  // Backing property for the timer's current time
  private val _time = MutableStateFlow(totalTime)
  // Publicly exposed StateFlow for observing the time
  val time: StateFlow<Long> = _time

  // Backing property for the timer's running state
  private val _isRunning = MutableStateFlow(false)
  // Publicly exposed StateFlow for observing the running state
  val isRunning: StateFlow<Boolean> = _isRunning

  // Coroutine job for the timer
  private var timerJob: Job? = null

  /**
   * Starts the timer. If the timer is already running, this does nothing.
   */
  fun startTimer() {
    if (timerJob?.isActive == true) {
      return // Timer is already running
    }
    _isRunning.value = true
    timerJob = viewModelScope.launch {
      while (_time.value > 0 && _isRunning.value) {
        delay(1000)
        if(_isRunning.value) { // Check again in case it was paused during the delay
          _time.value--
        }
      }
      if (_time.value <= 0) {
        _isRunning.value = false // Timer finished
      }
    }
  }

  /**
   * Pauses the timer.
   */
  fun pauseTimer() {
    _isRunning.value = false
    timerJob?.cancel()
  }

  /**
   * Resets the timer to the initial state.
   */
  fun resetTimer() {
    _isRunning.value = false
    timerJob?.cancel()
    _time.value = totalTime
  }

  /**
   * Formats the time in seconds to a MM:ss string.
   */
  fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
  }
}
