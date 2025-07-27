package com.redpup.racingregisters.companion.event.testing

import com.redpup.racingregisters.companion.event.EventBus
import com.redpup.racingregisters.companion.event.Tagged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


/** A testing wrapper on [EventBus].
 *
 * Event results are automatically collected into [_results].
 */
class FakeEventBus<E : Any>(
  private val eventBus: EventBus<E>,
  private val testDispatcher: TestDispatcher,
) {
  private val _results = mutableListOf<Tagged<E>>()

  private var job: Job? = null

  /** Starts this fake event bus. */
  fun launch() {
    _results.clear()
    job = CoroutineScope(testDispatcher).launch(testDispatcher) {
      eventBus.collectEvents { _results.add(it) }
    }
  }

  /** Returns the results collected so far. */
  fun results(): List<Tagged<E>> {
    testDispatcher.scheduler.advanceUntilIdle()
    return _results.toList()
  }

  /** Returns the results collected so far and clears the list. */
  fun consumeResults() : List<Tagged<E>> {
    testDispatcher.scheduler.advanceUntilIdle()
    val results = _results.toList()
    _results.clear()
    return results
  }

  /** Stops this fake event bus. */
  fun tearDown() {
    testDispatcher.scheduler.advanceUntilIdle()
    _results.clear()
    job?.cancel()
    job = null
  }
}