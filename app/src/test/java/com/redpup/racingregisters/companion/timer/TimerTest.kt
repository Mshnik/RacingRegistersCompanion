package com.redpup.racingregisters.companion.timer

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

class TimerTest {
  companion object {
    private const val TIMER_DURATION_SECONDS = 100
  }

  private val timer = Timer(TIMER_DURATION_SECONDS)

  @After
  fun tearDown() {
    timer.reset()
  }

  @Test
  fun initializesTime() {
    assertThat(timer.initialSeconds).isEqualTo(TIMER_DURATION_SECONDS)
    assertThat(timer.ticks).isEqualTo(0)
    assertThat(timer.elapsedMillis()).isEqualTo(0L)
    assertThat(timer.elapsedSeconds()).isEqualTo(0)
    assertThat(timer.remainingSeconds()).isEqualTo(TIMER_DURATION_SECONDS)
  }

  @Test
  fun startStartsTimer() {
    timer.start()
    assertThat(timer.timer).isNotNull()
  }

  @Test
  fun multipleStartsNoOps() {
    timer.start()
    timer.start()
    timer.start()
    assertThat(timer.timer).isNotNull()
  }

  @Test
  fun startDoesNotStartIfTimeIsElapsed() {
    timer.ticks = Integer.MAX_VALUE
    timer.start()
    assertThat(timer.timer).isNull()
  }

  @Test
  fun startAndPauseStopsTimer() {
    timer.start()
    timer.pause()
    assertThat(timer.timer).isNull()
  }

  @Test
  fun multiplePausesNoOps() {
    timer.start()
    timer.pause()
    timer.pause()
    timer.pause()
    assertThat(timer.timer).isNull()
  }

  @Test
  fun tickIncrementsTicks() = runBlocking {
    timer.start()
    delayAtLeastOneTick()
    assertThat(timer.ticks).isGreaterThan(0)
    assertThat(timer.elapsedMillis()).isGreaterThan(0)
  }

  @Test
  fun tickCallsSubSecondSubscriber() = runBlocking {
    var observed = false
    timer.subscribeSubSecond { observed = true}
    timer.start()
    delayAtLeastOneTick()
    assertThat(observed).isTrue()
  }

  @Test
  fun resetTimer() = runBlocking {
    timer.start()
    delayAtLeastOneTick()
    assertThat(timer.ticks).isGreaterThan(0)

    timer.reset()
    assertThat(timer.ticks).isEqualTo(0)
    assertThat(timer.timer).isNull()
  }

  @Test
  fun toStringFormatsString() {
    assertThat(timer.toString()).isEqualTo("1:40")
    timer.ticks = 3500
    assertThat(timer.toString()).isEqualTo("1:05")
    timer.ticks = Integer.MAX_VALUE
    assertThat(timer.toString()).isEqualTo("DONE")
  }

  private fun delayAtLeastOneTick() = runBlocking {
    delay(250)
  }
}