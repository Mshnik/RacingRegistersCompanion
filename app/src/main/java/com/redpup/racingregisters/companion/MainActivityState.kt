package com.redpup.racingregisters.companion

import androidx.lifecycle.ViewModel
import com.redpup.racingregisters.companion.timer.Event as TimerEvent
import com.redpup.racingregisters.companion.timer.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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
  val coroutineScope : CoroutineScope
  // val music: BackgroundMusic,
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
        transition { executeStart() }
      }

      MainButtonState.BREAK -> executeBreak()

      MainButtonState.CONTINUE -> {
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
    timer.start()
    resetButtonEnabled.value = true
    buttonEnabled.value = true
    isRunning.value = true
  }

  /** Executes the break action, (maybe) after transition. */
  private suspend fun executeBreak() {
    timer.pause()
    backgroundViewModel.accumulate()
    buttonEnabled.value = true
    isRunning.value = false
  }

  /** Executes the continue action, after transition. */
  private suspend fun executeContinue() {
    timer.start()
    buttonEnabled.value = true
    isRunning.value = true
  }

  /** Invoked when the "Reset" button is pushed. */
  fun reset() {
    timer.reset()
    transitionTimer.reset()
    buttonEnabled.value = true
    resetButtonEnabled.value = false
    isRunning.value = false
  }

  // /** Sets up music with event handling. */
  // suspend fun setupMusic(context: Context) {
  //   music.setVolume(context.resources.getFloat(R.dimen.music_volume_master))
  //
  //   eventBus.subscribe(Event.TRANSITION_TO_CONTINUE, tag = "setupMusic") {
  //     music.startTransitionIn()
  //   }
  //   eventBus.subscribe(Event.START, tag = "setupMusic") {
  //     music.start(this)
  //   }
  //   eventBus.subscribe(Event.CONTINUE, tag = "setupMusic") {
  //     music.startContinue(this)
  //   }
  //   eventBus.subscribe(Event.BREAK, tag = "setupMusic") {
  //     music.startBreak(this)
  //   }
  //   timer.eventBus.subscribe(TimerEvent.FINISH, tag = "setupMusic") {
  //     music.reset(this)
  //   }
  //   eventBus.subscribe(Event.HURRY_UP, tag = "setupMusic") {
  //     music.startHurryUp()
  //   }
  //
  //   music.prepareAsync { eventBus.emit(Event.MUSIC_PREPARED) }
  // }

  // /** Sets up sound with event handling. */
  // suspend fun setupSound(context: Context) {
  //   val beginEffect = MediaPlayer.create(context, R.raw.effect_begin)
  //   val resumeEffect = MediaPlayer.create(context, R.raw.effect_start)
  //   val breakEffect = MediaPlayer.create(context, R.raw.effect_break)
  //
  //   scaleTransitionTimerToMusic(beginEffect.duration)
  //   eventBus.subscribe(Event.RESET, tag = "setupSound") {
  //     scaleTransitionTimerToMusic(beginEffect.duration)
  //   }
  //
  //   eventBus.subscribe(Event.TRANSITION_TO_START, tag = "setupSound") {
  //     beginEffect.start()
  //   }
  //   transitionTimer.eventBus.subscribe(TimerEvent.COMPLETE, tag = "setupSound") {
  //     resumeEffect.start()
  //   }
  //   eventBus.subscribe(Event.BREAK, tag = "setupSound") {
  //     breakEffect.start()
  //   }
  //
  //   val countdownEffects = mapOf(
  //     0 to R.raw.effect_finish,
  //     1 to R.raw.effect_countdown_1,
  //     2 to R.raw.effect_countdown_2,
  //     3 to R.raw.effect_countdown_3,
  //     4 to R.raw.effect_countdown_4,
  //     5 to R.raw.effect_countdown_5,
  //     6 to R.raw.effect_countdown_6,
  //     7 to R.raw.effect_countdown_7,
  //     8 to R.raw.effect_countdown_8,
  //     9 to R.raw.effect_countdown_9,
  //     10 to R.raw.effect_countdown_10
  //   ).mapValues { MediaPlayer.create(context, it.value) }
  //
  //   countdownEffects.forEach { timer.incrementBus.subscribe(it.key) { it.value.start() } }
  // }

  /**
   * Scales the transition timer in state to match the given duration in millis,
   * without changing the number of increments in the timer.
   */
  fun scaleTransitionTimerToMusic(durationMillis: Int) {
    transitionTimer.reset()
    transitionTimer.scaleSpeed(durationMillis)
  }
}
