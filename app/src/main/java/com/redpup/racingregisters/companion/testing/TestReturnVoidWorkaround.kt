package com.redpup.racingregisters.companion.testing

// https://youtrack.jetbrains.com/issue/KTIJ-28566/K2-IDE-kotlinx.coroutines-Method-annotated-with-Test-should-be-of-type-void

/** Returns nothing. */
fun pass() : Unit {}

/** Converts anything into nothing. */
fun Any.asUnit() : Unit = Unit
