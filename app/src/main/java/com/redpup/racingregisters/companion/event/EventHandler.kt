package com.redpup.racingregisters.companion.event

import androidx.annotation.GuardedBy
import com.google.common.collect.HashMultimap

/**
 * A generic handler for events. Allows subscription to a specific event, then invocation when
 * that event occurs.
 */
class EventHandler<E> {
  private val subscriberLock = Object()

  @GuardedBy("subscriberLock")
  internal val subscribers = HashMultimap.create<E, () -> Unit>()

  /**
   * Adds a subscriber to this timer for the given event.
   */
  fun subscribe(vararg events: E, sub: () -> Unit) {
    synchronized(subscriberLock) {
      events.forEach { subscribers.put(it, sub) }
    }
  }

  /** Clears any existing subscribers. */
  fun clearSubscribers() {
    synchronized(subscriberLock) {
      subscribers.clear()
    }
  }

  /** Handles all subscribers registered for the given Event. */
  fun handleSubscribers(event: E) {
    synchronized(subscriberLock) {
      subscribers.get(event).forEach { it.invoke() }
    }
  }
}