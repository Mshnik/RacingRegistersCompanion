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
  RESET
}

/** Wrapper on mutable state visually displayed in this activity.*/
class MainActivityState(val timer : Timer) {
  private val eventHandler = EventHandler<Event>()

  /** Resets this state to the initial state. */
  fun reset() {
    timer.reset()
    eventHandler.handleSubscribers(Event.RESET)
  }

  /**
   * Adds a subscriber to this timer for the given event.
   */
  fun subscribe(event: Event, sub: () -> Unit) {
    eventHandler.subscribe(event, sub)
  }

  /** Clears any existing subscribers. */
  fun clearSubscribers() {
    eventHandler.clearSubscribers()
  }
}

