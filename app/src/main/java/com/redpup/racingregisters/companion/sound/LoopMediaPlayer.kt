package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer
import androidx.annotation.GuardedBy

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
    next.seekToStart()
  }

  /** Advances to the next player. */
  @Synchronized
  private fun onCurrentComplete() {
    val next = players.second
    val following = players.first.copy()
    players = Pair(next, following)
    following.prepareAsync { attachPlayers(next, following) }
  }

  override fun numMediaPlayers() = 2 * players().first.numMediaPlayers()

  override fun copy(): LoopMediaPlayer<T> = LoopMediaPlayer(players().first.copy())

  override fun prepareAsync(listener: () -> Unit) {
    players().forEach { it.prepareAsync(listener) }
  }

  override fun start() {
    val (current, next) = players()
    attachPlayers(current, next)
    current.start()
  }

  override fun pause() {
    players().first.pause()
  }

  override fun stop() {
    players().first.stop()
  }

  override fun reset() {
    players().first.reset()
  }

  override fun isPlaying(): Boolean {
    return players().first.isPlaying()
  }

  override fun seekToStart() {
    players().first.seekToStart()
  }

  override fun duration(): Int {
    throw UnsupportedOperationException("duration not supported on Looping player. ")
  }

  override fun setIsMuted(isMuted: Boolean) {
    players().forEach { it.setIsMuted(isMuted) }
  }

  override fun setVolume(volume: Float) {
    players().forEach { it.setVolume(volume) }
  }

  override fun multiplyVolume(ratio: Float) {
    players().forEach { it.multiplyVolume(ratio) }
  }

  /** Applies the given function to current player and next player. */
  fun applyToPlayers(fn: (T) -> Unit) {
    players().forEach { fn(it) }
  }

  /** Applies a get on the current player. */
  fun <R> get(fn: (T) -> R): R {
    return fn(players().first)
  }

  override fun setNextMediaPlayer(nextPlayer: LoopMediaPlayer<T>) {
    throw UnsupportedOperationException("setNextMediaPlayer not supported on already Looping player. ")
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    throw UnsupportedOperationException("setOnCompletionListener not supported on already Looping player. ")
  }
}