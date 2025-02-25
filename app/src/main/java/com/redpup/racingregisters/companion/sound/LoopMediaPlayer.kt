package com.redpup.racingregisters.companion.sound

import android.util.Log
import androidx.annotation.GuardedBy
import com.redpup.racingregisters.companion.event.ForkedListener
import java.util.Timer
import kotlin.concurrent.schedule

/** Applies fn to each value in pair. */
private fun <T> Pair<T, T>.forEach(fn: (T) -> Unit) {
  fn(this.first)
  fn(this.second)
}

/**
 * Media player that plays a single sound on loop, without gaps.
 *
 * See [StackOverflow](https://stackoverflow.com/questions/26274182/not-able-to-achieve-gapless-audio-looping-so-far-on-android).
 */
class LoopMediaPlayer<T : AbstractMediaPlayer<T>>(mediaPlayer: T) :
  AbstractMediaPlayer<LoopMediaPlayer<T>> {

  @GuardedBy("this")
  private var players = Pair(mediaPlayer, mediaPlayer.copy())

  @Synchronized
  private fun players(): Pair<T, T> {
    return players.copy()
  }

  /** Attaches completion and next handlers to current. */
  private fun attachPlayers(current: T, next: T) {
    current.setOnCompletionListener { _ -> onCurrentComplete() }
    current.setNextMediaPlayer(next)
    next.softReset()
  }

  /** Advances to the next player. */
  @Synchronized
  private fun onCurrentComplete() {
    val next = players.second
    val following = players.second.copy()
    players = Pair(next, following)
    following.prepareAsync { attachPlayers(next, following) }
  }

  override fun numMediaPlayers() = 2 * players().first.numMediaPlayers()

  override fun copy(): LoopMediaPlayer<T> = LoopMediaPlayer(players().first.copy())

  override fun prepareAsync(listener: () -> Unit): LoopMediaPlayer<T> {
    val fork = ForkedListener<Unit>(2, {}, { listener() })
    players().forEach { it.prepareAsync { fork.handle(Unit) } }
    return this
  }

  override fun applyPlaybackParams(): LoopMediaPlayer<T> {
    players().forEach { it.applyPlaybackParams() }
    return this
  }

  override fun start(): LoopMediaPlayer<T> {
    val (current, next) = players()
    current.start()
    attachPlayers(current, next)
    return this
  }

  override fun pause(): LoopMediaPlayer<T> {
    players().first.pause()
    return this
  }

  override fun stop(): LoopMediaPlayer<T> {
    players().first.stop()
    return this
  }

  override fun softReset(): LoopMediaPlayer<T> {
    players().first.softReset()
    return this
  }

  override fun reset(): LoopMediaPlayer<T> {
    players().first.reset()
    return this
  }

  override fun release(): LoopMediaPlayer<T> {
    players().forEach { it.release() }
    return this
  }

  override fun isPlaying(): Boolean {
    return players().first.isPlaying()
  }

  override fun seekToStart(): LoopMediaPlayer<T> {
    players().first.seekToStart()
    return this
  }

  override fun duration(): Int {
    throw UnsupportedOperationException("duration not supported on Looping player. ")
  }

  override fun setIsMuted(isMuted: Boolean): LoopMediaPlayer<T> {
    players().forEach { it.setIsMuted(isMuted) }
    return this
  }

  override fun setVolume(volume: Float): LoopMediaPlayer<T> {
    players().forEach { it.setVolume(volume) }
    return this
  }

  override fun multiplyVolume(ratio: Float): LoopMediaPlayer<T> {
    players().forEach { it.multiplyVolume(ratio) }
    return this
  }

  override fun setSpeed(speed: Float): LoopMediaPlayer<T> {
    players().forEach { it.setSpeed(speed) }
    return this
  }

  override fun multiplySpeed(ratio: Float): LoopMediaPlayer<T> {
    players().forEach { it.multiplySpeed(ratio) }
    return this
  }

  override fun setPitch(pitch: Float): LoopMediaPlayer<T> {
    players().forEach { it.setPitch(pitch) }
    return this
  }

  override fun multiplyPitch(ratio: Float): LoopMediaPlayer<T> {
    players().forEach { it.multiplyPitch(ratio) }
    return this
  }

  /** Applies the given function to current player and next player. */
  fun applyToPlayers(fn: (T) -> Unit) {
    players().forEach { fn(it) }
  }

  /** Applies a get on the current player. */
  fun <R> get(fn: (T) -> R): R {
    return fn(players().first)
  }

  override fun setNextMediaPlayer(nextPlayer: LoopMediaPlayer<T>): LoopMediaPlayer<T> {
    throw UnsupportedOperationException("setNextMediaPlayer not supported on already Looping player. ")
  }

  override fun setOnCompletionListener(listener: (LoopMediaPlayer<T>) -> Unit): LoopMediaPlayer<T> {
    throw UnsupportedOperationException("setOnCompletionListener not supported on already Looping player. ")
  }
}