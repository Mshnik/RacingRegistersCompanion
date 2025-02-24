package com.redpup.racingregisters.companion.event

/**
 * A listener that synchronously combines multiple listeners and invokes the onComplete
 * when all sub-handlers have been invoked.
 */
class ForkedListener<T>(private val numForks: Int, val onComplete: (List<T>) -> Unit) {
  private val forkValues = mutableListOf<T>()

  @Synchronized
  fun handle(value: T) {
    check(forkValues.size < numForks)

    forkValues.add(value)
    if (forkValues.size == numForks) {
      onComplete(forkValues)
    }
  }
}