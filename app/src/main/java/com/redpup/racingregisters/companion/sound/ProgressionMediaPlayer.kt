package com.redpup.racingregisters.companion.sound

import com.redpup.racingregisters.companion.event.ForkedListener
import kotlin.math.min

/** A media player that can play a progression of tracks.
 *
 */
class ProgressionMediaPlayer(private val progression: List<AbstractMediaPlayer<*>>) :
  AbstractMediaPlayer<ProgressionMediaPlayer> {
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

  override fun numMediaPlayers() = progression[currentIndex].numMediaPlayers()

  override fun copy() = ProgressionMediaPlayer(progression.map { it.copy() })

  override fun prepareAsync(listener: () -> Unit): ProgressionMediaPlayer {
    val fork = ForkedListener<Unit>(progression.size, {}, { listener() })
    progression.forEach { it.prepareAsync { fork.handle(Unit) } }
    return this
  }

  override fun applyPlaybackParams(): ProgressionMediaPlayer {
    progression.forEach { it.applyPlaybackParams() }
    return this
  }

  override fun start(): ProgressionMediaPlayer {
    progression[currentIndex].start()
    return this
  }

  override fun pause(): ProgressionMediaPlayer {
    progression[currentIndex].pause()
    return this
  }

  override fun stop(): ProgressionMediaPlayer {
    progression[currentIndex].stop()
    return this
  }

  override fun softReset(): ProgressionMediaPlayer {
    progression[currentIndex].softReset()
    return this
  }

  override fun reset(): ProgressionMediaPlayer {
    progression.forEach { it.reset() }
    return this
  }

  override fun release(): ProgressionMediaPlayer {
    progression.forEach { it.release() }
    return this
  }

  override fun isPlaying(): Boolean {
    return progression[currentIndex].isPlaying()
  }

  override fun seekToStart(): ProgressionMediaPlayer {
    progression[currentIndex].seekToStart()
    return this
  }

  override fun duration(): Int {
    return progression[currentIndex].duration()
  }

  override fun setIsMuted(isMuted: Boolean): ProgressionMediaPlayer {
    progression.forEach { it.setIsMuted(isMuted) }
    return this
  }

  override fun setVolume(volume: Float): ProgressionMediaPlayer {
    progression.forEach { it.setVolume(volume) }
    return this
  }

  override fun multiplyVolume(ratio: Float): ProgressionMediaPlayer {
    progression.forEach { it.multiplyVolume(ratio) }
    return this
  }

  override fun setSpeed(speed: Float): ProgressionMediaPlayer {
    progression.forEach { it.setSpeed(speed) }
    return this
  }

  override fun multiplySpeed(ratio: Float): ProgressionMediaPlayer {
    progression.forEach { it.multiplySpeed(ratio) }
    return this
  }

  override fun setPitch(pitch: Float): ProgressionMediaPlayer {
    progression.forEach { it.setPitch(pitch) }
    return this
  }

  override fun multiplyPitch(ratio: Float): ProgressionMediaPlayer {
    progression.forEach { it.multiplyPitch(ratio) }
    return this
  }

  override fun setOnCompletionListener(listener: (ProgressionMediaPlayer) -> Unit): ProgressionMediaPlayer {
    throw UnsupportedOperationException("onCompletion not supported by ProgressionMediaPlayer")
  }

  override fun setNextMediaPlayer(nextPlayer: ProgressionMediaPlayer): ProgressionMediaPlayer {
    throw UnsupportedOperationException("nextMediaPlayer not supported by ProgressionMediaPlayer")
  }
}