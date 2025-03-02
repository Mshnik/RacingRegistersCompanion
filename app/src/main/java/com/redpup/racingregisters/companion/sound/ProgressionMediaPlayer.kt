package com.redpup.racingregisters.companion.sound

import com.redpup.racingregisters.companion.event.ForkedListener
import kotlin.math.min

/** A media player that can play a progression of tracks.
 *
 */
class ProgressionMediaPlayer<T : AbstractMediaPlayer<T>>(private val progression: List<T>) :
  AbstractMediaPlayer<ProgressionMediaPlayer<T>> {
  private var currentIndex = 0

  /**
   * Advances this to the next track in the progression.
   * Goes back to the start if we've reached the end. */
  fun advanceAndRotate() {
    check(!isPlaying())
    currentIndex++
    if (currentIndex == progression.size) {
      currentIndex = 0
    }
  }

  /**
   * Advances this to the next track in the progression.
   * Does nothing if we've reached the end.
   */
  fun advanceAndCap() {
    check(!isPlaying())
    currentIndex = min(currentIndex + 1, progression.size - 1)
  }

  override fun self() = this

  override fun numMediaPlayers() = current().numMediaPlayers()

  override fun copy() = ProgressionMediaPlayer<T>(progression.map { it.copy() })

  internal fun current() = progression[currentIndex]

  override fun prepareAsync(listener: () -> Unit): ProgressionMediaPlayer<T> {
    val fork = ForkedListener<Unit>(progression.size, {}, { listener() })
    progression.forEach { it.prepareAsync { fork.handle(Unit) } }
    return this
  }

  override fun applyPlaybackParams(): ProgressionMediaPlayer<T> {
    progression.forEach { it.applyPlaybackParams() }
    return this
  }

  override fun start(): ProgressionMediaPlayer<T> {
    current().start()
    return this
  }

  override fun pause(): ProgressionMediaPlayer<T> {
    current().pause()
    return this
  }

  override fun stop(): ProgressionMediaPlayer<T> {
    current().stop()
    return this
  }

  override fun softReset(): ProgressionMediaPlayer<T> {
    current().softReset()
    return this
  }

  override fun reset(): ProgressionMediaPlayer<T> {
    progression.forEach { it.reset() }
    return this
  }

  override fun release(): ProgressionMediaPlayer<T> {
    progression.forEach { it.release() }
    return this
  }

  override fun isPlaying() = current().isPlaying()

  override fun seekToStart(): ProgressionMediaPlayer<T> {
    current().seekToStart()
    return this
  }

  override fun duration(): Int {
    return current().duration()
  }

  override fun setIsMuted(isMuted: Boolean): ProgressionMediaPlayer<T> {
    progression.forEach { it.setIsMuted(isMuted) }
    return this
  }

  override fun setVolume(volume: Float): ProgressionMediaPlayer<T> {
    progression.forEach { it.setVolume(volume) }
    return this
  }

  override fun multiplyVolume(ratio: Float): ProgressionMediaPlayer<T> {
    progression.forEach { it.multiplyVolume(ratio) }
    return this
  }

  override fun setSpeed(speed: Float): ProgressionMediaPlayer<T> {
    progression.forEach { it.setSpeed(speed) }
    return this
  }

  override fun multiplySpeed(ratio: Float): ProgressionMediaPlayer<T> {
    progression.forEach { it.multiplySpeed(ratio) }
    return this
  }

  override fun setPitch(pitch: Float): ProgressionMediaPlayer<T> {
    progression.forEach { it.setPitch(pitch) }
    return this
  }

  override fun multiplyPitch(ratio: Float): ProgressionMediaPlayer<T> {
    progression.forEach { it.multiplyPitch(ratio) }
    return this
  }

  override fun setOnCompletionListener(listener: (ProgressionMediaPlayer<T>) -> Unit): ProgressionMediaPlayer<T> {
    throw UnsupportedOperationException("onCompletion not supported by ProgressionMediaPlayer")
  }

  override fun setNextMediaPlayer(nextPlayer: ProgressionMediaPlayer<T>): ProgressionMediaPlayer<T> {
    current().setNextMediaPlayer(nextPlayer.current())
    return this
  }
}