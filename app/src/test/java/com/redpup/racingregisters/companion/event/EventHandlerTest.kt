package com.redpup.racingregisters.companion.event

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventHandlerTest {

  private val eventHandler = EventHandler<Int>()

  @Test
  fun subscribe() {
    val handler : () -> Unit = {}

    eventHandler.subscribe(1, handler)

    assertThat(eventHandler.subscribers).containsExactly(1, handler)
  }

  @Test
  fun handleSubscribers_incorrectEvent() {
    val input = 5
    var output = 0
    val handler : () -> Unit = { output = input }

    eventHandler.subscribe(2, handler)
    eventHandler.handleSubscribers(1)

    assertThat(output).isEqualTo(0)
  }

  @Test
  fun handleSubscribers_correctEvent() {
    val input = 5
    var output = 0
    val handler : () -> Unit = { output = input }

    eventHandler.subscribe(1, handler)
    eventHandler.handleSubscribers(1)

    assertThat(output).isEqualTo(input)
  }

  @Test
  fun clearSubscribers() {
    val handler : () -> Unit = {}

    eventHandler.subscribe(1, handler)
    eventHandler.clearSubscribers()

    assertThat(eventHandler.subscribers).isEmpty()
  }
}