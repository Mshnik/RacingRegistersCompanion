package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer
import androidx.annotation.GuardedBy

/**
 * Media player that plays a single sound on loop, without gaps.
 *
 * See [StackOverflow](https://stackoverflow.com/questions/26274182/not-able-to-achieve-gapless-audio-looping-so-far-on-android).
 */
class LoopMediaPlayer<T : AbstractMediaPlayer<T>>(mediaPlayer: T) :
  AbstractMediaPlayer<LoopMediaPlayer<T>> {

  @GuardedBy("this")
  private var currentPlayer: T = mediaPlayer

  @GuardedBy("this")
  private var nextPlayer: T = mediaPlayer.copy()

  @Synchronized
  private fun attachPlayers() {
    currentPlayer.setOnCompletionListener(this::advanceMediaPlayer)
    currentPlayer.setNextMediaPlayer(nextPlayer)
  }

  /** Advances `mediaPlayer` to the next player and releases the current `mediaPlayer`. */
  @Synchronized
  private fun advanceMediaPlayer(mediaPlayer: MediaPlayer) {
    mediaPlayer.reset()
    mediaPlayer.release()
    currentPlayer = nextPlayer
    nextPlayer = currentPlayer.copy()
    attachPlayers()
  }

  @Synchronized
  override fun copy(): LoopMediaPlayer<T> = LoopMediaPlayer(currentPlayer.copy())

  @Synchronized
  override fun start() {
    currentPlayer.start()
    attachPlayers()
  }

  @Synchronized
  override fun pause() {
    currentPlayer.pause()
  }

  @Synchronized
  override fun stop() {
    currentPlayer.stop()
  }

  @Synchronized
  override fun reset() {
    currentPlayer.reset()
  }

  @Synchronized
  override fun isPlaying(): Boolean {
    return currentPlayer.isPlaying()
  }

  @Synchronized
  override fun seekToStart() {
    currentPlayer.seekToStart()
  }

  override fun duration(): Int {
    throw UnsupportedOperationException("duration not supported on Looping player. ")
  }

  @Synchronized
  override fun setIsMuted(isMuted: Boolean) {
    currentPlayer.setIsMuted(isMuted)
    nextPlayer.setIsMuted(isMuted)
  }

  @Synchronized
  override fun setVolume(volume: Float) {
    currentPlayer.setVolume(volume)
    nextPlayer.setVolume(volume)
  }

  @Synchronized
  override fun multiplyVolume(ratio: Float) {
    currentPlayer.multiplyVolume(ratio)
    nextPlayer.multiplyVolume(ratio)
  }

  @Synchronized
  override fun setPlaybackSpeed(speed: Float) {
    currentPlayer.setPlaybackSpeed(speed)
    nextPlayer.setPlaybackSpeed(speed)
    nextPlayer.stop()
  }

  @Synchronized
  override fun setPlaybackSpeedIncrement(speedIncrement: Float) {
    currentPlayer.setPlaybackSpeedIncrement(speedIncrement)
    nextPlayer.setPlaybackSpeedIncrement(speedIncrement)
  }

  @Synchronized
  override fun incrementSpeed() {
    currentPlayer.incrementSpeed()
    nextPlayer.incrementSpeed()
    nextPlayer.stop()
  }

  @Synchronized
  override fun setPlaybackPitch(pitch: Float) {
    currentPlayer.setPlaybackPitch(pitch)
    nextPlayer.setPlaybackPitch(pitch)
  }

  @Synchronized
  override fun setPlaybackPitchRatio(pitchRatio: Float) {
    currentPlayer.setPlaybackPitchRatio(pitchRatio)
    nextPlayer.setPlaybackPitchRatio(pitchRatio)
  }

  @Synchronized
  override fun incrementPitch() {
    currentPlayer.incrementSpeed()
    nextPlayer.incrementSpeed()
  }

  /** Applies the given function to current player and next player. */
  @Synchronized
  fun applyToPlayers(fn: (T) -> Unit) {
    fn(currentPlayer)
    fn(nextPlayer)
  }

  /** Applies a get on the current player. */
  @Synchronized
  fun <R> get(fn: (T) -> R): R {
    return fn(currentPlayer)
  }

  override fun setNextMediaPlayer(nextPlayer: LoopMediaPlayer<T>) {
    throw UnsupportedOperationException("setNextMediaPlayer not supported on already Looping player. ")
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    throw UnsupportedOperationException("setOnCompletionListener not supported on already Looping player. ")
  }
}