package com.redpup.racingregisters.companion

import androidx.lifecycle.ViewModel
import com.redpup.racingregisters.companion.timer.Event as TimerEvent
import com.redpup.racingregisters.companion.timer.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** State of the main action button on the main activity. */
enum class MainButtonState {
  START,
  BREAK,
  CONTINUE;

  /** Toggles this state to the next state. Break -> Continue, Anything else -> Break. */
  fun toggle(): MainButtonState {
    return if (this == BREAK) {
      CONTINUE
    } else {
      BREAK
    }
  }
}

/** Events that can be fired on the main activity state. */
enum class Event {
  MUSIC_PREPARED,
  TRANSITION_TO_START,
  START,
  BREAK,
  TRANSITION_TO_CONTINUE,
  CONTINUE,
  HURRY_UP,
  RESET,
}

/** Possible states of the timer. */
enum class RunState {
  PAUSED,
  RUNNING,
  HURRY_UP,
  COMPLETE
}

/** Wrapper on mutable state visually displayed in this activity.*/
class MainActivityState(
  val timer: TimerViewModel,
  val hurryUp: Int,
  val transitionTimer: TimerViewModel,
  val backgroundViewModel: BackgroundViewModel = BackgroundViewModel(timer),
  val music: BackgroundMusic,
  val soundEffects: SoundEffects,
  val coroutineScope: CoroutineScope,
) : ViewModel() {
  /** Whether the top reset button is currently enabled. */
  val resetButtonEnabled = MutableStateFlow(true)

  /** Text to show on the timer, based on which timer is running. */
  val timerText: Flow<String> =
    combine(
      transitionTimer.isRunning,
      timer.formattedTime,
      transitionTimer.formattedTime
    ) { transitionRunning, timer, transition ->
      if (transitionRunning) transition else timer
    }

  /**
   * Whether everything is loaded and ready to go.
   * This should start false and be set to true after all loading is done.
   */
  val isReady = MutableStateFlow(true)

  /** The current run state. */
  val isRunning = MutableStateFlow(false)

  /** Whether this is currently in hurry up state. */
  val isHurryUp = timer.remainingIncrements.map { it <= hurryUp }

  /** The current button state. */
  val buttonState = MutableStateFlow(MainButtonState.START)

  /** Whether the main button is enabled. This should be true unless we are in a transition. */
  val buttonEnabled = MutableStateFlow(true)

  /** The current run state of the timer. */
  val runState = combine(isRunning, isHurryUp)
  { running, hurryUp ->
    if (!running) RunState.PAUSED
    else if (hurryUp) RunState.HURRY_UP
    else RunState.RUNNING
  }

  /** Called when the main button is clicked. */
  suspend fun clickButton() {
    when (buttonState.value) {
      MainButtonState.START -> {
        soundEffects.beginEffect.start()
        transition { executeStart() }
      }

      MainButtonState.BREAK -> executeBreak()

      MainButtonState.CONTINUE -> {
        music.startTransitionIn()
        transition { executeContinue() }
      }
    }
  }

  /** Begins a transition, executing execute at the end of the transition. */
  private suspend fun transition(execute: suspend () -> Unit) {
    buttonEnabled.value = false
    transitionTimer.reset()
    coroutineScope.launch {
      transitionTimer.eventBus.subscribe(
        TimerEvent.FINISH,
        limit = 1
      ) {
        execute()
      }
    }
    transitionTimer.start()
    isRunning.value = true
  }

  /** Executes the start action, after transition. */
  private suspend fun executeStart() {
    music.start(this)
    timer.start()
    resetButtonEnabled.value = true
    buttonEnabled.value = true
    isRunning.value = true
    buttonState.value = MainButtonState.BREAK
  }

  /** Executes the break action, (maybe) after transition. */
  private suspend fun executeBreak() {
    soundEffects.breakEffect.start()
    music.startBreak(this)
    timer.pause()
    backgroundViewModel.accumulate()
    buttonEnabled.value = true
    isRunning.value = false
    buttonState.value = MainButtonState.CONTINUE
  }

  /** Executes the continue action, after transition. */
  private suspend fun executeContinue() {
    music.startContinue(this)
    timer.start()
    buttonEnabled.value = true
    isRunning.value = true
    buttonState.value = MainButtonState.BREAK
  }

  /** Invoked when the "Reset" button is pushed. */
  fun reset() {
    scaleTransitionTimerToMusic(soundEffects.beginEffect.duration)
    music.reset(this)
    timer.reset()
    transitionTimer.reset()
    buttonEnabled.value = true
    resetButtonEnabled.value = false
    isRunning.value = false
  }

  /** Sets up music with event handling. */
  fun setupMusic(masterVolume: Float) {
    music.setVolume(masterVolume)
    music.prepareAsync { isReady.value = true }

    coroutineScope.launch {
      isHurryUp
        .filter { it }
        .collect {
          music.startHurryUp()
        }
    }
  }

  /** Sets up sound with event handling. */
  fun setupSound() {
    scaleTransitionTimerToMusic(soundEffects.beginEffect.duration)
    coroutineScope.launch {
      transitionTimer.isComplete
        .filter { it }
        .collect {
          soundEffects.resumeEffect.start()
        }
    }

    coroutineScope.launch {
      timer.remainingIncrements
        .filter { soundEffects.countdownEffects.containsKey(it) }
        .collect {
          soundEffects.countdownEffects[it]!!.start()
        }
    }
  }

  /**
   * Scales the transition timer in state to match the given duration in millis,
   * without changing the number of increments in the timer.
   */
  fun scaleTransitionTimerToMusic(durationMillis: Int) {
    transitionTimer.reset()
    transitionTimer.scaleSpeed(durationMillis)
  }
}
