package com.redpup.racingregisters.companion.event

/** A tagged [E] is a pair of [E] and a tag. */
typealias Tagged<E> = Pair<E, String>

/** Tags [this] with [tag], defaulting to empty. */
fun <E> E.tagged(tag: String = "") : Tagged<E> {
  return Pair(this@tagged, tag)
}

/** Returns the value in this tagged element. */
fun <E> Tagged<E>.value() : E = this@value.first

/** Returns the tag in this tagged element. */
fun <E> Tagged<E>.tag() : String = this@tag.second
