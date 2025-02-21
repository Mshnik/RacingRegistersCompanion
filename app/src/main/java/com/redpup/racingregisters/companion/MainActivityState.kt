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
  fun toggle() :MainButtonState {
    return if (this == BREAK) {
      CONTINUE
    } else {
      BREAK
    }
  }
}

/** Events that can be fired on the main activity state. */
enum class Event {
  START,
  BREAK,
  CONTINUE,
  RESET
}

/** Wrapper on mutable state visually displayed in this activity.*/
class MainActivityState(val timer : Timer, val resetTimer: Timer) {
  val eventHandler = EventHandler<Event>()

  /** Invoked when the button is pushed. */
  fun action(buttonState: MainButtonState) {
    when (buttonState) {
      MainButtonState.START -> actionStart()
      MainButtonState.BREAK -> actionBreak()
      MainButtonState.CONTINUE -> actionContinue()
    }
  }

  /** Invoked when the "Start" button is pushed. */
  fun actionStart() {
    timer.start()
    eventHandler.handleSubscribers(Event.START)
  }

  /** Invoked when the "Break" button is pushed. */
  fun actionBreak() {
    timer.pause()
    eventHandler.handleSubscribers(Event.BREAK)
  }

  /** Invoked when the "Continue" button is pushed. */
  fun actionContinue() {
    timer.start()
    eventHandler.handleSubscribers(Event.CONTINUE)
  }

  /** Invoked when the "Reset" button is pushed. */
  fun reset() {
    timer.reset()
    resetTimer.reset()
    eventHandler.handleSubscribers(Event.RESET)
  }
}

