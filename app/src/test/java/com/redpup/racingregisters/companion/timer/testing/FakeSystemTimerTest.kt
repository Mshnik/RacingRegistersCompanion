package com.redpup.racingregisters.companion.timer.testing

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class FakeSystemTimerTest {

  private val timer = FakeSystemTimer()

  @Before
  fun setup() {
    timer.currentTimeMillis = 0L
    timer.clearEvents()
  }

  @Test
  fun holdsCurrentTime() {
    timer.currentTimeMillis = 12345L
    assertThat(timer.currentTimeMillis).isEqualTo(12345L)
  }

  @Test
  fun handleEvent_notYetElapsed_noTime() {
    val output = mutableListOf<Boolean>()

    timer.executeAt("Tag", 10000L) { output.add(true) }

    assertThat(output).isEmpty()
    assertThat(timer.events).hasSize(1)
  }

  @Test
  fun handleEvent_notYetElapsed_insufficientTime() {
    val output = mutableListOf<Boolean>()

    timer.executeAt("Tag", 10000L) { output.add(true) }
    timer.advance(2000L)

    assertThat(output).isEmpty()
    assertThat(timer.events).hasSize(1)
  }

  @Test
  fun handleEvent_elapsed() {
    val output = mutableListOf<Boolean>()

    timer.executeAt("Tag", 10000L) { output.add(true) }
    timer.advance(20000L)

    assertThat(output).containsExactly(true)
    assertThat(timer.events).isEmpty()
  }

  @Test
  fun clearEvents() {
    val output = mutableListOf<Boolean>()

    timer.executeAt("Tag", 10000L) { output.add(true) }
    timer.clearEvents()

    assertThat(timer.events).isEmpty()
  }

  @Test
  fun cancelEvents_incorrectTag() {
    val output = mutableListOf<Boolean>()

    timer.executeAt("Tag", 10000L) { output.add(true) }
    timer.cancelEvents("WrongTag")

    assertThat(timer.events).hasSize(1)
  }

  @Test
  fun cancelEvents_correctTag() {
    val output = mutableListOf<Boolean>()

    timer.executeAt("Tag", 10000L) { output.add(true) }
    timer.cancelEvents("Tag")

    assertThat(timer.events).isEmpty()
  }
}