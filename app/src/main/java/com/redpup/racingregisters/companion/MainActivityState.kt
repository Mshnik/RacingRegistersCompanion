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
  RESET
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

    music.prepareAsync { eventHandler.handleSubscribers(Event.MUSIC_PREPARED) }
  }

  /** Sets up sound with event handling. */
  fun setupSound(context: Context) {
    eventHandler.clearSubscribers("setupSound")

    val soundEffectStart = MediaPlayer.create(context, R.raw.effect_start)
    val soundEffectBreak = MediaPlayer.create(context, R.raw.effect_break)

    transitionTimer.eventHandler.subscribe(TimerEvent.COMPLETE, tag = "setupSound") {
      soundEffectStart.start()
    }
    eventHandler.subscribe(Event.BREAK, tag = "setupSound") {
      soundEffectBreak.start()
    }
  }
}

