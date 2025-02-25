package com.redpup.racingregisters.companion.event

/**
 * A listener that synchronously combines multiple listeners and invokes the onCompleteMain
 * when all sub-handlers have been invoked.
 *
 * On the final element, onCompleteEach is invoked before onCompleteAll.
 */
class ForkedListener<T>(
  private val numForks: Int,
  private val onCompleteEach: (T) -> Unit,
  private val onCompleteAll: (List<T>) -> Unit,
) {
  private val forkValues = mutableListOf<T>()

  @Synchronized
  fun handle(value: T) {
    check(forkValues.size < numForks)

    onCompleteEach(value)
    forkValues.add(value)
    if (forkValues.size == numForks) {
      onCompleteAll(forkValues)
    }
  }
}