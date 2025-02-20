package com.redpup.racingregisters.companion.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.GuardedBy
import org.checkerframework.checker.nullness.qual.MonotonicNonNull

/**
 * Media player that plays a single sound on loop, without gaps.
 *
 *
 * See [Stack
 * Overflow](https://stackoverflow.com/questions/26274182/not-able-to-achieve-gapless-audio-looping-so-far-on-android).
 */
class LoopMediaPlayer(
  private val context: Context,
  private val resourceId: Int,
) {
  @GuardedBy("this")
  private var currentPlayer: @MonotonicNonNull MediaPlayer? = null

  @GuardedBy("this")
  private var nextPlayer: @MonotonicNonNull MediaPlayer? = null

  @GuardedBy("this")
  private var volume = 1.0f

  @GuardedBy("this")
  private var speed = 1.0f

  @GuardedBy("this")
  private var speedIncrement = 0.0f

  init {
    this.currentPlayer = createMediaPlayer()
    createNextMediaPlayer()
  }

  /**
   * Creates a new media player with the configured parameters.
   */
  private fun createMediaPlayer() = MediaPlayer.create(context, resourceId)

  /**
   * Sets params on `mp`. This is done separately from [.createMediaPlayer] because
   * setting non-zero speed also calls start.
   */
  private fun setMediaPlayerParams(mp: MediaPlayer) {
    mp.setVolume(volume, volume)
    mp.playbackParams = mp.playbackParams.setSpeed(speed)
  }

  /**
   * Creates and sets the [.nextPlayer] field to continue after [.currentPlayer] is
   * complete.
   */
  @Synchronized
  private fun createNextMediaPlayer() {
    nextPlayer = createMediaPlayer()
    currentPlayer!!.setNextMediaPlayer(nextPlayer)
    currentPlayer!!.setOnCompletionListener { mediaPlayer: MediaPlayer ->
      advanceMediaPlayer(mediaPlayer)
    }
  }

  /**
   * Advances `mediaPlayer` to the next player and releases the current `mediaPlayer`.
   */
  @Synchronized
  private fun advanceMediaPlayer(mediaPlayer: MediaPlayer) {
    mediaPlayer.release()
    currentPlayer = nextPlayer
    setMediaPlayerParams(currentPlayer!!)
    createNextMediaPlayer()
  }

  /** Returns true iff this player is currently playing.  */
  @get:Synchronized val isPlaying: Boolean get() = currentPlayer!!.isPlaying


  /**
   * Starts this looping player.
   */
  @Synchronized
  fun start() {
    setMediaPlayerParams(currentPlayer!!)
    currentPlayer!!.start()
  }

  /**
   * Pauses this looping player.
   */
  @Synchronized
  fun pause() {
    currentPlayer!!.pause()
  }

  /**
   * Sets the volume of this looping player. This volume persists across loops.
   */
  @Synchronized
  fun setVolume(volume: Float) {
    this.volume = volume
    currentPlayer!!.setVolume(volume, volume)
    nextPlayer!!.setVolume(volume, volume)
  }

  /**
   * Sets the speed of this looping player.
   */
  @Synchronized
  fun setPlaybackSpeed(speed: Float) {
    this.speed = speed
  }

  /** Increases the speed of this looping player by the speed increment.  */
  @Synchronized
  fun incrementSpeed() {
    setPlaybackSpeed(speed + speedIncrement)
  }

  /**
   * Sets the speed increment to increase every play back.
   */
  @Synchronized
  fun setAutoAdvanceSpeedIncrement(speedIncrement: Float) {
    this.speedIncrement = speedIncrement
  }
}