package com.redpup.racingregisters.companion.timer.testing

/** An event that can be registered within a timer. */
private data class Event(val tag: String, val time: Long, val fn: () -> Unit)

/** A fake System timer for use in testing. */
class FakeSystemTimer(private var increment: Long = 100L) {
  var currentTimeMillis: Long = System.currentTimeMillis()
    set(value) {
      field = value
      handleEvents()
    }

  private var events = mutableListOf<Event>()

  /** Advances [currentTimeMillis] by [increment]. */
  fun advance() {
    advance(increment)
  }

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

  /** Removes all events for the given tag. */
  fun cancelEvents(tag: String) {
    synchronized(events) {
      events.removeAll { it.tag == tag }
    }
  }
}
