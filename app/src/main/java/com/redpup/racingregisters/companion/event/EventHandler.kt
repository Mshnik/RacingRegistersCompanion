package com.redpup.racingregisters.companion.event

import androidx.annotation.GuardedBy
import com.google.common.collect.HashBasedTable

/**
 * A generic handler for events. Allows subscription to a specific event, then invocation when
 * that event occurs.
 */
class EventHandler<E : Any> {
  @GuardedBy("this")
  internal val subscribers = HashBasedTable.create<E, String, MutableSet<() -> Unit>>()

  /**
   * Adds a subscriber to this timer for the given event.
   */
  @Synchronized
  fun subscribe(vararg events: E, tag: String = "", sub: () -> Unit) {
    events.forEach { event ->
      if (!subscribers.contains(event, tag)) {
        subscribers.put(event, tag, mutableSetOf())
      }
      subscribers.get(event, tag)!!.add(sub)
    }
  }

  /** Clears any existing subscribers. */
  @Synchronized
  fun clearSubscribers() {
    subscribers.clear()
  }

  /** Clears any existing subscribers for the given tag. */
  @Synchronized
  fun clearSubscribers(tag: String) {
    subscribers.column(tag).values.forEach { it.clear() }
  }

  /** Handles all subscribers registered for the given Event, for all tags. */
  @Synchronized
  fun handleSubscribers(event: E) {
    subscribers.row(event).values.flatten().forEach { it.invoke() }
  }
}