package com.redpup.racingregisters.companion.event

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class ForkedListenerTest {

  @Test
  fun noForks() {
    val output = mutableListOf<Int>()

    val fork = ForkedListener(1) { output.addAll(it) }

    assertThat(output).isEmpty()
  }

  @Test
  fun oneFork() {
    val output = mutableListOf<Int>()

    val fork = ForkedListener(1) { output.addAll(it) }
    fork.handle(1)

    assertThat(output).containsExactly(1)
  }

  @Test
  fun manyForks() {
    val output = mutableListOf<Int>()

    val fork = ForkedListener(3) { output.addAll(it) }
    fork.handle(1)
    fork.handle(2)
    fork.handle(3)

    assertThat(output).containsExactly(1, 2, 3).inOrder()
  }

  @Test
  fun tooManyForks() {
    val output = mutableListOf<Int>()

    val fork = ForkedListener(3) { output.addAll(it) }
    fork.handle(1)
    fork.handle(2)
    fork.handle(3)

    assertThrows<Exception> { fork.handle(4) }
  }
}