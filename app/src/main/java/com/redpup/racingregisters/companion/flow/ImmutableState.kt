package com.redpup.racingregisters.companion.flow

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

/**
 * An immutable override of [State].
 */
@Stable
data class ImmutableState<T>(override val value: T) : State<T>

/** Converts anything into an immutable state of itself. */
fun <T> T.asState(): State<T> = ImmutableState(this@asState)
