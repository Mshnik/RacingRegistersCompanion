package com.redpup.racingregisters.companion.timer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.redpup.racingregisters.companion.event.tagged
import com.redpup.racingregisters.companion.event.testing.FakeEventBus
import com.redpup.racingregisters.companion.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
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
  private lateinit var fakeEventBus: FakeEventBus<Event>
  private lateinit var fakeIncrementBus: FakeEventBus<Int>

  @Before
  fun setup() {
    timer = TimerViewModel(DURATION_INCREMENTS, INCREMENT_MILLIS, TICKS_PER_INCREMENT)
    fakeEventBus = FakeEventBus(timer.eventBus, mainDispatcherRule.testDispatcher)
    fakeIncrementBus = FakeEventBus(timer.incrementBus, mainDispatcherRule.testDispatcher)

    fakeEventBus.launch()
    fakeIncrementBus.launch()
  }

  @After
  fun tearDown() {
    fakeEventBus.tearDown()
    fakeIncrementBus.tearDown()
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

    assertThat(fakeEventBus.consumeResults()).contains(Event.ACTIVATE.tagged())
  }

  @Test
  fun multipleStartsNoOps() = runBlocking {
    timer.start()

    assertThat(fakeEventBus.consumeResults()).contains(Event.ACTIVATE.tagged())

    timer.start()
    timer.start()

    assertThat(fakeEventBus.consumeResults()).isEmpty()
  }

  @Test
  fun startDoesNotStartIfTimeIsElapsed() = runBlocking {
    timer.ticks.value = Integer.MAX_VALUE
    timer.start()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(0)
    assertThat(fakeEventBus.consumeResults()).isEmpty()
  }

  @Test
  fun startAndPauseStopsTimer() = runBlocking {
    timer.start()
    timer.pause()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(1)
    assertThat(fakeEventBus.consumeResults()).containsAllOf(
      Event.ACTIVATE.tagged(),
      Event.DEACTIVATE.tagged()
    ).inOrder()
  }

  @Test
  fun startAndPauseIncrementsNumResumes() = runBlocking {
    timer.start()
    timer.pause()
    timer.start()
    timer.pause()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(2)
    assertThat(fakeEventBus.consumeResults()).containsAllOf(
      Event.ACTIVATE.tagged(),
      Event.DEACTIVATE.tagged(),
      Event.ACTIVATE.tagged(),
      Event.DEACTIVATE.tagged()
    ).inOrder()
  }

  @Test
  fun multiplePausesNoOps() = runBlocking {
    timer.start()
    timer.pause()
    timer.pause()
    timer.pause()

    assertThat(timer.isRunning.first()).isFalse()
    assertThat(timer.numResumes.first()).isEqualTo(1)
    assertThat(fakeEventBus.consumeResults()).containsAllOf(
      Event.ACTIVATE.tagged(),
      Event.DEACTIVATE.tagged(),
    ).inOrder()
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

    assertThat(fakeEventBus.consumeResults()).containsAllOf(
      Event.ACTIVATE.tagged(),
      Event.TICK.tagged(),
      Event.TICK.tagged(),
      Event.TICK.tagged(),
    ).inOrder()
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

    assertThat(fakeIncrementBus.consumeResults()).containsAllOf(
      (DURATION_INCREMENTS - 1).tagged(),
      (DURATION_INCREMENTS - 2).tagged(),
    ).inOrder()
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

    assertThat(fakeIncrementBus.consumeResults()).containsAllOf(
      (DURATION_INCREMENTS - 1).tagged(),
      (DURATION_INCREMENTS - 2).tagged(),
    ).inOrder()
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