package com.redpup.racingregisters.companion

import android.content.Context
import com.redpup.racingregisters.companion.event.EventHandler
import com.redpup.racingregisters.companion.timer.Timer

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

