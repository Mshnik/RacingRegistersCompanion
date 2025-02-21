package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer

/**
 * An abstract parent of a media player.
 * Exposes common methods to allow media player composition.
 */
interface AbstractMediaPlayer<Self : AbstractMediaPlayer<Self>> {

  /**
   * Copies this to a new AbstractMediaPlayer.
   * This should only be invoked before volume and speed methods are called.
   */
  fun copy(): Self

  /** Starts playing this media player. */
  fun start()

  /** Pauses this media player. */
  fun pause()

  /** Stops this media player. */
  fun stop()

  /** Resets this media player. */
  fun reset()

  /**
   * Mutes or unmutes this media player.
   * Retains the old volume value if unmuted.
   */
  fun setIsMuted(isMuted: Boolean)

  /**
   * Sets the volume of this media player.
   * If this is muted, the set value is stored but not applied until this is unmuted.
   */
  fun setVolume(volume: Float)

  /** Sets the volume of this media player to the current volume times ratio. */
  fun multiplyVolume(ratio: Float)

  /** Sets the speed of this looping player. */
  fun setPlaybackSpeed(speed: Float)

  /** Sets the speed increment to increase whenever incrementSpeed() is called. */
  fun setPlaybackSpeedIncrement(speedIncrement: Float)

  /** Increases the speed of this media player by the speed increment.  */
  fun incrementSpeed()

  /** Sets the player to play after this media player is complete. */
  fun setNextMediaPlayer(nextPlayer: Self)

  /**
   * Sets a listener to invoke when this media player is complete.
   * Inputs are the listener that completed and the index within the parent, if any.
   */
  fun setOnCompletionListener(listener: (MediaPlayer) -> Unit)
}