package com.redpup.racingregisters.companion

import android.content.Context
import android.media.MediaPlayer
import com.redpup.racingregisters.companion.event.EventHandler
import com.redpup.racingregisters.companion.timer.Timer
import com.redpup.racingregisters.companion.timer.Event as TimerEvent

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
  RESET,
}

/** Wrapper on mutable state visually displayed in this activity.*/
class MainActivityState(val timer: Timer, val transitionTimer: Timer, val music: BackgroundMusic) {
  val eventHandler = EventHandler<Event>()

  /** Invoked when the button is pushed. */
  fun action(buttonState: MainButtonState) {
    when (buttonState) {
      MainButtonState.START -> {
        transition { executeStart() }
        eventHandler.handleSubscribers(Event.TRANSITION_TO_START)
      }

      MainButtonState.BREAK -> executeBreak()

      MainButtonState.CONTINUE -> {
        transition { executeContinue() }
        eventHandler.handleSubscribers(Event.TRANSITION_TO_CONTINUE)
      }
    }
  }

  /** Begins a transition, executing execute at the end of the transition. */
  private fun transition(execute: () -> Unit) {
    transitionTimer.reset()
    transitionTimer.eventHandler.clearSubscribers("Transition")
    transitionTimer.eventHandler.subscribe(TimerEvent.FINISH, tag = "Transition") { execute() }
    transitionTimer.start()
  }

  /** Executes the start action, after transition. */
  private fun executeStart() {
    timer.start()
    eventHandler.handleSubscribers(Event.START)
  }

  /** Executes the break action, (maybe) after transition. */
  private fun executeBreak() {
    timer.pause()
    eventHandler.handleSubscribers(Event.BREAK)
  }

  /** Executes the continue action, after transition. */
  private fun executeContinue() {
    timer.start()
    eventHandler.handleSubscribers(Event.CONTINUE)
  }

  /** Invoked when the "Reset" button is pushed. */
  fun reset() {
    timer.reset()
    transitionTimer.reset()
    music.reset(this)
    eventHandler.handleSubscribers(Event.RESET)
  }

  /** Sets up music with event handling. */
  fun setupMusic(context: Context) {
    eventHandler.clearSubscribers("setupMusic")

    music.setVolume(context.resources.getFloat(R.dimen.music_volume_master))

    eventHandler.subscribe(Event.TRANSITION_TO_CONTINUE, tag = "setupMusic") {
      music.startTransitionIn()
    }
    eventHandler.subscribe(Event.START, tag = "setupMusic") {
      music.start(this)
    }
    eventHandler.subscribe(Event.CONTINUE, tag = "setupMusic") {
      music.startContinue(this)
    }
    eventHandler.subscribe(Event.BREAK, tag = "setupMusic") {
      music.startBreak()
    }
    timer.eventHandler.subscribe(TimerEvent.FINISH, tag = "setupMusic") {
      music.reset(this)
    }

    music.prepareAsync { eventHandler.handleSubscribers(Event.MUSIC_PREPARED) }
  }

  /** Sets up sound with event handling. */
  fun setupSound(context: Context) {
    eventHandler.clearSubscribers("setupSound")

    val beginEffect = MediaPlayer.create(context, R.raw.effect_begin)
    val resumeEffect = MediaPlayer.create(context, R.raw.effect_start)
    val breakEffect = MediaPlayer.create(context, R.raw.effect_break)

    scaleTransitionTimerToMusic(beginEffect.duration)
    eventHandler.subscribe(Event.RESET, tag = "setupSound") {
      scaleTransitionTimerToMusic(beginEffect.duration)
    }

    eventHandler.subscribe(Event.TRANSITION_TO_START, tag = "setupSound") {
      beginEffect.start()
    }
    transitionTimer.eventHandler.subscribe(TimerEvent.COMPLETE, tag = "setupSound") {
      resumeEffect.start()
    }
    eventHandler.subscribe(Event.BREAK, tag = "setupSound") {
      breakEffect.start()
    }

    val countdownEffects = mapOf(
      0 to R.raw.effect_finish,
      1 to R.raw.effect_countdown_1,
      2 to R.raw.effect_countdown_2,
      3 to R.raw.effect_countdown_3,
      4 to R.raw.effect_countdown_4,
      5 to R.raw.effect_countdown_5,
      6 to R.raw.effect_countdown_6,
      7 to R.raw.effect_countdown_7,
      8 to R.raw.effect_countdown_8,
      9 to R.raw.effect_countdown_9,
      10 to R.raw.effect_countdown_10
    ).mapValues { MediaPlayer.create(context, it.value) }

    countdownEffects.forEach { timer.incrementHandler.subscribe(it.key) { it.value.start() } }
  }

  /**
   * Scales the transition timer in state to match the given duration in millis,
   * without changing the number of increments in the timer.
   */
  fun scaleTransitionTimerToMusic(durationMillis: Int) {
    transitionTimer.reset()
    val timerIntervalDuration =
      (durationMillis / transitionTimer.remainingIncrements().toFloat()).toLong()
    transitionTimer.setSpeed(timerIntervalDuration, 1)
  }
}

