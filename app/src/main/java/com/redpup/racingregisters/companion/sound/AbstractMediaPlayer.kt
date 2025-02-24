package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer

/**
 * An abstract parent of a media player.
 * Exposes common methods to allow media player composition.
 */
interface AbstractMediaPlayer<Self : AbstractMediaPlayer<Self>> {

  /** How many players are wrapped by this media player. */
  fun numMediaPlayers() : Int

  /**
   * Copies this to a new AbstractMediaPlayer.
   * This should only be invoked before volume and speed methods are called.
   */
  fun copy(): Self

  /**
   * Prepares this player, registering a callback when this is ready.
   * Must be called before starting in any way, but must only be called once.
   */
  fun prepareAsync(listener: () -> Unit)

  /** Starts playing this media player. */
  fun start()

  /** Pauses this media player. */
  fun pause()

  /** Stops this media player. */
  fun stop()

  /** Resets this media player. */
  fun reset()

  /** Returns true iff this player is currently playing. */
  fun isPlaying() : Boolean

  /** Seeks this media player to the start of the track. */
  fun seekToStart()

  /** The duration of this track in millis. */
  fun duration(): Int

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

  /** Sets the player to play after this media player is complete. */
  fun setNextMediaPlayer(nextPlayer: Self)

  /** Sets a listener to invoke when this media player is complete. */
  fun setOnCompletionListener(listener: (MediaPlayer) -> Unit)
}