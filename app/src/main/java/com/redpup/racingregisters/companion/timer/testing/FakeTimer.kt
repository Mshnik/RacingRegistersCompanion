package com.redpup.racingregisters.companion.timer.testing

/** An event that can be registered within a timer. */
internal data class Event(val tag: String, val time: Long, val fn: () -> Unit)

/** A fake System timer for use in testing. */
class FakeSystemTimer() {
  var currentTimeMillis: Long = System.currentTimeMillis()
    set(value) {
      field = value
      handleEvents()
    }

  internal val events = mutableListOf<Event>()

  /** Advances [currentTimeMillis] by [millis]. */
  fun advance(millis: Long) {
    currentTimeMillis += millis
  }

  /** Executes fn at time. This simulates a separate threaded timer. */
  fun executeAt(tag: String, time: Long, fn: () -> Unit) {
    if (time <= currentTimeMillis) {
      fn()
    } else {
      synchronized(events) {
        events.add(Event(tag, time, fn))
      }
    }
  }

  /** Handles all events, executing all that are scheduled for a current or past time. */
  private fun handleEvents() {
    synchronized(events) {
      events.removeAll {
        val elapsed = it.time <= currentTimeMillis
        if (elapsed) {
          it.fn()
        }
        elapsed
      }
    }
  }

  /** Clears all events. */
  fun clearEvents() {
    synchronized(events) {
      events.clear()
    }
  }

  /** Removes all events for the given tag. */
  fun cancelEvents(tag: String) {
    synchronized(events) {
      events.removeAll { it.tag == tag }
    }
  }
}
