package com.redpup.racingregisters.companion.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take

/**
 * A generic handler for events. Allows subscription to a specific event, then invocation when
 * that event occurs.
 */
class EventBus<E : Any> {
  internal val events = MutableSharedFlow<Tagged<E>>(replay = 0)

  /** Events [event] with [tag] through the bus. */
  suspend fun emit(event: E, tag: String = "") {
    emit(event.tagged(tag))
  }

  /** Events [event] through the bus. */
  suspend fun emit(event: Tagged<E>) {
    events.emit(event)
  }

  /** Subscribes to events on the bus. */
  suspend fun collectEvents(action: suspend (Tagged<E>) -> Unit) {
    events.collect { action(it) }
  }

  /**
   * Subscribes to events on the bus with the given events and tag.
   */
  suspend fun subscribe(vararg events: E, tag: String = "", limit: Int? = null, action: suspend () -> Unit) {
    val filtered = this.events
      .filter { events.contains(it.value()) && it.tag() == tag }
    val limited = if(limit == null) filtered else filtered.take(limit)
    limited.collect { action() }
  }
}