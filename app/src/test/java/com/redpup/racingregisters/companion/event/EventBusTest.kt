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

    eventBus.emit(1)
    eventBus.emit(1.tagged("tag"))
    eventBus.emit(2)
    mainDispatcherRule.advanceUntilIdle()

    assertThat(results).containsExactly(1.tagged(), 1.tagged("tag"), 2.tagged())
    job.cancel()
  }

  @Test
  fun subscribeSubscribesToFilteredEvents() = runBlocking {
    val results = mutableListOf<Int>()
    val job = launch(mainDispatcherRule.testDispatcher) {
      eventBus.subscribe(1, 2, 3) { results.add(99) }
    }

    eventBus.emit(1)
    eventBus.emit(2)
    eventBus.emit(3)
    eventBus.emit(4)
    mainDispatcherRule.advanceUntilIdle()

    assertThat(results).containsExactly(99, 99, 99)
    job.cancel()
  }

  @Test
  fun subscribeSubscribesToFilteredEvents_withTag() = runBlocking {
    val results = mutableListOf<Int>()
    val job = launch(mainDispatcherRule.testDispatcher) {
      eventBus.subscribe(1, 2, 3, tag = "tag") { results.add(99) }
    }

    eventBus.emit(1.tagged("tag"))
    eventBus.emit(2.tagged("tag"))
    eventBus.emit(3.tagged("tag"))
    eventBus.emit(3.tagged("other tag"))
    mainDispatcherRule.advanceUntilIdle()

    assertThat(results).containsExactly(99, 99, 99)
    job.cancel()
  }

  @Test
  fun subscribeSubscribesToFilteredEvents_withLimit() = runBlocking {
    val results = mutableListOf<Int>()
    val job = launch(mainDispatcherRule.testDispatcher) {
      eventBus.subscribe(1, 2, 3, limit = 2) { results.add(99) }
    }

    eventBus.emit(1)
    eventBus.emit(2)
    eventBus.emit(3)
    eventBus.emit(4)
    mainDispatcherRule.advanceUntilIdle()

    assertThat(results).containsExactly(99, 99)
    job.cancel()
  }
}