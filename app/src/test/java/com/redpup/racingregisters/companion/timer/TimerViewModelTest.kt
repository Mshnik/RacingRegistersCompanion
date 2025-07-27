package com.redpup.racingregisters.companion.timer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.redpup.racingregisters.companion.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class TimerViewModelTest {
  companion object {
    private const val DURATION_INCREMENTS = 100
    private const val INCREMENT_MILLIS = 10
    private const val TICKS_PER_INCREMENT = 10
  }

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  private lateinit var timer: TimerViewModel

  @Before
  fun setup() {
    timer = TimerViewModel(DURATION_INCREMENTS, INCREMENT_MILLIS, TICKS_PER_INCREMENT)
  }

  @Test
  fun initializesTime() = runBlocking {
    assertThat(timer.initialIncrements).isEqualTo(DURATION_INCREMENTS)

    assertThat(timer.ticks.first()).isEqualTo(0)
    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(0)
    assertThat(timer.elapsedMilliIncrements.first()).isEqualTo(0)
    assertThat(timer.elapsedIncrements.first()).isEqualTo(0)
    assertThat(timer.remainingIncrements.first()).isEqualTo(DURATION_INCREMENTS)
  }

  @Test
  fun startStartsTimer() = runBlocking {
    timer.start()

    assertThat(timer.isRunning.first()).isTrue()
    assertThat(timer.numResumes.first()).isEqualTo(1)
  }

  @Test
  fun multipleStartsNoOps() = runBlocking {
    timer.start()
    timer.start()
    timer.start()

    assertThat(timer.isRunning.first()).isTrue()
    assertThat(timer.numResumes.first()).isEqualTo(1)
  }

  @Test
  fun startDoesNotStartIfTimeIsElapsed() = runBlocking {
    timer.ticks.value = Integer.MAX_VALUE
    timer.start()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(0)
  }

  @Test
  fun startAndPauseStopsTimer() = runBlocking {
    timer.start()
    timer.pause()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(1)
  }

  @Test
  fun startAndPauseIncrementsNumResumes() = runBlocking {
    timer.start()
    timer.pause()
    timer.start()
    timer.pause()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(2)
  }

  @Test
  fun multiplePausesNoOps() = runBlocking {
    timer.start()
    timer.pause()
    timer.pause()
    timer.pause()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(1)
  }

  @Test
  fun timerIncrementsTicks() = runBlocking {
    timer.start()
    timer.ticks.test {
      delayTicks(2L)
      assertThat(awaitItem()).isEqualTo(0)
      assertThat(awaitItem()).isEqualTo(1)
      assertThat(awaitItem()).isEqualTo(2)
    }
  }

  @Test
  fun timerIncrementsIncrements() = runBlocking {
    timer.start()
    timer.elapsedIncrements.test {
      delayTicks(2L * TICKS_PER_INCREMENT)
      assertThat(awaitItem()).isEqualTo(0)
      assertThat(awaitItem()).isEqualTo(1)
      assertThat(awaitItem()).isEqualTo(2)
    }
  }

  @Test
  fun timerDecrementsRemainingIncrements() = runBlocking {
    timer.start()
    timer.remainingIncrements.test {
      delayTicks(2L * TICKS_PER_INCREMENT)
      assertThat(awaitItem()).isEqualTo(DURATION_INCREMENTS)
      assertThat(awaitItem()).isEqualTo(DURATION_INCREMENTS - 1)
      assertThat(awaitItem()).isEqualTo(DURATION_INCREMENTS - 2)
    }
  }

  @Test
  fun resetTimer() = runBlocking {
    timer.start()
    assertThat(timer.isRunning.first()).isTrue()

    timer.reset()
    assertThat(timer.isRunning.first()).isFalse()
  }

  @Test
  fun toStringFormatsString() {
    assertThat(timer.formatTime()).isEqualTo("1:40")
    timer.ticks.value = 50
    assertThat(timer.formatTime()).isEqualTo("1:35")
    timer.ticks.value = Integer.MAX_VALUE
    assertThat(timer.formatTime()).isEqualTo("DONE")
  }

  private fun delayTicks(ticks: Long = 1L) {
    mainDispatcherRule.advanceTimeBy(INCREMENT_MILLIS / TICKS_PER_INCREMENT * ticks + 1L)
  }
}