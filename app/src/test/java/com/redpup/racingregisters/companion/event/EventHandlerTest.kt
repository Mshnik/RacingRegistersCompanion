package com.redpup.racingregisters.companion.event

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventHandlerTest {
  /** Typed handler function with name based equality for test clarity. */
  class Handler(val name: String = "Handler", val fn: () -> Unit = {}) : () -> Unit {
    override fun invoke() {
      fn()
    }

    override fun equals(other: Any?): Boolean {
      return other is Handler && other.name == name
    }

    override fun hashCode(): Int {
      return name.hashCode()
    }

    override fun toString(): String {
      return name
    }
  }


  private val eventHandler = EventHandler<Int>()

  @Test
  fun subscribe() {
    val handler = Handler()
    eventHandler.subscribe(1, tag = "", sub = handler)

    assertThat(eventHandler.subscribers).containsCell(1, "", mutableSetOf(handler))
  }

  @Test
  fun subscribeWithTag() {
    val handler = Handler()
    eventHandler.subscribe(1, tag = "Tag", sub = handler)

    assertThat(eventHandler.subscribers).containsCell(1, "Tag", mutableSetOf(handler))
  }

  @Test
  fun subscribeMultipleEvents() {
    val handler = Handler()
    eventHandler.subscribe(1, 2, 3, sub = handler)

    assertThat(eventHandler.subscribers).containsCell(1, "", mutableSetOf(handler))
    assertThat(eventHandler.subscribers).containsCell(2, "", mutableSetOf(handler))
    assertThat(eventHandler.subscribers).containsCell(3, "", mutableSetOf(handler))
  }

  @Test
  fun subscribeMultipleEventsWithTag() {
    val handler = Handler()
    eventHandler.subscribe(1, 2, 3, tag = "Tag", sub = handler)

    assertThat(eventHandler.subscribers).containsCell(1, "Tag", mutableSetOf(handler))
    assertThat(eventHandler.subscribers).containsCell(2, "Tag", mutableSetOf(handler))
    assertThat(eventHandler.subscribers).containsCell(3, "Tag", mutableSetOf(handler))
  }

  @Test
  fun handleSubscribers_incorrectEvent() {
    val input = 5
    var output = 0
    val handler = Handler(fn = { output = input })

    eventHandler.subscribe(2, sub = handler)
    eventHandler.handleSubscribers(1)

    assertThat(output).isEqualTo(0)
  }

  @Test
  fun handleSubscribers_correctEvent() {
    val input = 5
    var output = 0
    val handler = Handler(fn = { output = input })

    eventHandler.subscribe(1, sub = handler)
    eventHandler.handleSubscribers(1)

    assertThat(output).isEqualTo(input)
  }

  @Test
  fun clearSubscribers() {
    val handler = Handler()

    eventHandler.subscribe(1, sub = handler)
    eventHandler.clearSubscribers()

    assertThat(eventHandler.subscribers).isEmpty()
  }

  @Test
  fun clearSubscribersByTag() {
    val handler = Handler()

    eventHandler.subscribe(1, tag = "A", sub = handler)
    eventHandler.subscribe(1, tag = "B", sub = handler)
    eventHandler.subscribe(1, tag = "C", sub = handler)

    eventHandler.clearSubscribers("A")

    assertThat(eventHandler.subscribers).doesNotContainCell(1, "A", mutableSetOf(handler))
    assertThat(eventHandler.subscribers).containsCell(1, "B", mutableSetOf(handler))
    assertThat(eventHandler.subscribers).containsCell(1, "C", mutableSetOf(handler))
  }
}