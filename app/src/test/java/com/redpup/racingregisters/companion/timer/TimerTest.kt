package com.redpup.racingregisters.companion.timer

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

class TimerTest {
  companion object {
    private const val TIMER_DURATION_SECONDS = 100
    private const val TIMER_PERIOD_MILLIS = 10L
  }

  private val timer = Timer(TIMER_DURATION_SECONDS, TIMER_PERIOD_MILLIS)

  @After
  fun tearDown() {
    timer.reset()
  }

  @Test
  fun initializesTime() {
    assertThat(timer.initialSeconds).isEqualTo(TIMER_DURATION_SECONDS)
    assertThat(timer.secondsRemaining).isEqualTo(TIMER_DURATION_SECONDS)
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
    timer.secondsRemaining = 0
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
  fun tickDecrementsSeconds() = runBlocking {
    timer.start()
    delay(TIMER_PERIOD_MILLIS *2)
    assertThat(timer.secondsRemaining).isLessThan(TIMER_DURATION_SECONDS)
  }

  @Test
  fun tickCallsSubscriber() = runBlocking {
    var observed = false
    timer.subscribe { observed = true}
    timer.start()
    delay(TIMER_PERIOD_MILLIS *2)
    assertThat(observed).isTrue()
  }

  @Test
  fun resetTimer() = runBlocking {
    timer.start()
    delay(TIMER_PERIOD_MILLIS *2)
    assertThat(timer.secondsRemaining).isLessThan(TIMER_DURATION_SECONDS)

    timer.reset()
    assertThat(timer.secondsRemaining).isEqualTo(TIMER_DURATION_SECONDS)
    assertThat(timer.timer).isNull()
  }

  @Test
  fun toStringFormatsString() {
    assertThat(timer.toString()).isEqualTo("1:40")
    timer.secondsRemaining = 62
    assertThat(timer.toString()).isEqualTo("1:02")
    timer.secondsRemaining = 0
    assertThat(timer.toString()).isEqualTo("DONE")
  }
}