package com.redpup.racingregisters.companion.event

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class ForkedListenerTest {

  @Test
  fun noForks() {
    val outputEach = mutableListOf<Int>()
    val outputMain = mutableListOf<Int>()

    ForkedListener(1, { outputEach.add(it) }, { outputMain.addAll(it) })

    assertThat(outputEach).isEmpty()
    assertThat(outputMain).isEmpty()
  }

  @Test
  fun oneFork() {
    val outputEach = mutableListOf<Int>()
    val outputMain = mutableListOf<Int>()

    val fork = ForkedListener(1, { outputEach.add(it) }, { outputMain.addAll(it) })
    fork.handle(1)

    assertThat(outputEach).containsExactly(1)
    assertThat(outputMain).containsExactly(1)
  }

  @Test
  fun manyForks_incomplete() {
    val outputEach = mutableListOf<Int>()
    val outputMain = mutableListOf<Int>()

    val fork = ForkedListener(3, { outputEach.add(it) }, { outputMain.addAll(it) })
    fork.handle(1)
    fork.handle(2)

    assertThat(outputEach).containsExactly(1, 2).inOrder()
    assertThat(outputMain).isEmpty()
  }

  @Test
  fun manyForks_complete() {
    val outputEach = mutableListOf<Int>()
    val outputMain = mutableListOf<Int>()

    val fork = ForkedListener(3, { outputEach.add(it) }, { outputMain.addAll(it) })
    fork.handle(1)
    fork.handle(2)
    fork.handle(3)

    assertThat(outputEach).containsExactly(1, 2, 3).inOrder()
    assertThat(outputMain).containsExactly(1, 2, 3).inOrder()
  }

  @Test
  fun tooManyForks() {
    val outputEach = mutableListOf<Int>()
    val outputMain = mutableListOf<Int>()

    val fork = ForkedListener(3, { outputEach.add(it) }, { outputMain.addAll(it) })
    fork.handle(1)
    fork.handle(2)
    fork.handle(3)

    assertThat(outputEach).containsExactly(1, 2, 3).inOrder()
    assertThat(outputMain).containsExactly(1, 2, 3).inOrder()

    assertThrows<Exception> { fork.handle(4) }
  }
}