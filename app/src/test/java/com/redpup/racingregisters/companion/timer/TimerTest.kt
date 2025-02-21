package com.redpup.racingregisters.companion.timer

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

class TimerTest {
  companion object {
    private const val DURATION_INCREMENTS = 100
    private const val INCREMENT_MILLIS = 10L
    private const val TICKS_PER_INCREMENT = 10
  }

  private val timer = Timer(DURATION_INCREMENTS, INCREMENT_MILLIS, TICKS_PER_INCREMENT)

  @After
  fun tearDown() {
    timer.reset()
  }

  @Test
  fun initializesTime() {
    assertThat(timer.initialIncrements).isEqualTo(DURATION_INCREMENTS)
    assertThat(timer.ticks).isEqualTo(0)
    assertThat(timer.isActive()).isFalse()
    assertThat(timer.elapsedMilliIncrements()).isEqualTo(0L)
    assertThat(timer.elapsedIncrements()).isEqualTo(0)
    assertThat(timer.remainingIncrements()).isEqualTo(DURATION_INCREMENTS)
    assertThat(timer.numResumes).isEqualTo(0)
  }

  @Test
  fun startStartsTimer() {
    timer.start()
    assertThat(timer.isActive()).isTrue()
    assertThat(timer.timer).isNotNull()
    assertThat(timer.numResumes).isEqualTo(1)
  }

  @Test
  fun multipleStartsNoOps() {
    timer.start()
    timer.start()
    timer.start()
    assertThat(timer.isActive()).isTrue()
    assertThat(timer.timer).isNotNull()
    assertThat(timer.numResumes).isEqualTo(1)
  }

  @Test
  fun startDoesNotStartIfTimeIsElapsed() {
    timer.ticks = Integer.MAX_VALUE
    timer.start()
    assertThat(timer.timer).isNull()
    assertThat(timer.isActive()).isFalse()
  }

  @Test
  fun startAndPauseStopsTimer() {
    timer.start()
    timer.pause()
    assertThat(timer.isActive()).isFalse()
    assertThat(timer.timer).isNull()
    assertThat(timer.numResumes).isEqualTo(1)
  }

  @Test
  fun startAndPauseIncrementsNumResumes() {
    timer.start()
    timer.pause()
    timer.start()
    timer.pause()
    assertThat(timer.isActive()).isFalse()
    assertThat(timer.numResumes).isEqualTo(2)
  }

  @Test
  fun multiplePausesNoOps() {
    timer.start()
    timer.pause()
    timer.pause()
    timer.pause()
    assertThat(timer.isActive()).isFalse()
    assertThat(timer.timer).isNull()
  }

  @Test
  fun tickIncrementsTicks() = runBlocking {
    timer.start()
    delayAtLeastOneTick()
    assertThat(timer.isActive()).isTrue()
    assertThat(timer.ticks).isGreaterThan(0)
    assertThat(timer.elapsedMilliIncrements()).isGreaterThan(0)
  }

  @Test
  fun tickCallsSubIncrementSubscriber() = runBlocking {
    var observed = false
    timer.eventHandler.subscribe(Event.TICK) { observed = true }
    timer.start()
    delayAtLeastOneTick()
    assertThat(observed).isTrue()
  }

  @Test
  fun resetTimer() = runBlocking {
    timer.start()
    delayAtLeastOneTick()
    assertThat(timer.isActive()).isTrue()
    assertThat(timer.ticks).isGreaterThan(0)

    timer.reset()
    assertThat(timer.ticks).isEqualTo(0)
    assertThat(timer.isActive()).isFalse()
    assertThat(timer.timer).isNull()
  }

  @Test
  fun toStringFormatsString() {
    assertThat(timer.toString()).isEqualTo("1:40")
    timer.ticks = 50
    assertThat(timer.toString()).isEqualTo("1:35")
    timer.ticks = Integer.MAX_VALUE
    assertThat(timer.toString()).isEqualTo("DONE")
  }

  private fun delayAtLeastOneTick() = runBlocking {
    delay(INCREMENT_MILLIS / TICKS_PER_INCREMENT * 2)
  }
}