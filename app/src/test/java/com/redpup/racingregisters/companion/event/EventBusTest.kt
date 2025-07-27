package com.redpup.racingregisters.companion.event

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.redpup.racingregisters.companion.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class EventBusTest {
  private val eventBus = EventBus<Int>()

  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun emitAddsEventToBus() = runBlocking {
    eventBus.events.test {
      eventBus.emit(1.tagged())
      assertThat(awaitItem()).isEqualTo(1.tagged())
    }
  }

  @Test
  fun collectCollectsEventFromBus() = runBlocking {
    val results = mutableListOf<Tagged<Int>>()
    val job = launch(mainDispatcherRule.testDispatcher) {
      eventBus.collectEvents { results.add(it) }
    }

    eventBus.emit(1.tagged())
    mainDispatcherRule.advanceUntilIdle()

    assertThat(results).containsExactly(1.tagged())
    job.cancel()
  }
}