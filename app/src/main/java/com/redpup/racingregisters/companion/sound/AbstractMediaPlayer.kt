package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer
import com.google.errorprone.annotations.CanIgnoreReturnValue

/**
 * An abstract parent of a media player.
 * Exposes common methods to allow media player composition.
 */
interface AbstractMediaPlayer<Self : AbstractMediaPlayer<Self>> {

  /** How many players are wrapped by this media player. */
  fun numMediaPlayers(): Int

  /**
   * Returns a new copy of this AbstractMediaPlayer.
   * This should only be invoked before volume and speed methods are called.
   */
  fun copy(): Self

  /**
   * Prepares this player, registering a callback when this is ready.
   * Must be called before starting in any way, but must only be called once.
   */
  fun prepareAsync(listener: () -> Unit): Self

  /** Applies necessary state to the playback params inside this player. */
  fun applyPlaybackParams() : Self

  /** Starts playing this media player. Returns this. */
  @CanIgnoreReturnValue
  fun start(): Self

  /** Pauses this media player. Returns this. */
  @CanIgnoreReturnValue
  fun pause(): Self

  /** Stops this media player. Returns this. */
  @CanIgnoreReturnValue
  fun stop(): Self

  /** Resets this media player. Returns this. */
  @CanIgnoreReturnValue
  fun reset(): Self

  /** Releases this media player. It cannot be used afterwards. Returns this. */
  @CanIgnoreReturnValue
  fun release(): Self

  /** Returns true iff this player is currently playing. */
  fun isPlaying(): Boolean

  /** Seeks this media player to the start of the track. Returns this. */
  @CanIgnoreReturnValue
  fun seekToStart(): Self

  /** The duration of this track in millis. */
  fun duration(): Int

  /**
   * Mutes or unmutes this media player.
   * Retains the old volume value if unmuted.
   * Returns this.
   */
  @CanIgnoreReturnValue
  fun setIsMuted(isMuted: Boolean): Self

  /**
   * Sets the volume of this media player.
   * If this is muted, the set value is stored but not applied until this is unmuted.
   * Returns this.
   */
  @CanIgnoreReturnValue
  fun setVolume(volume: Float): Self

  /** Sets the volume of this media player to the current volume times ratio. Returns this. */
  @CanIgnoreReturnValue
  fun multiplyVolume(ratio: Float): Self

  /**
   * Sets the speed of the media player.
   * Only updates when this media player starts playing.
   * Returns this.
   */
  @CanIgnoreReturnValue
  fun setSpeed(speed: Float): Self

  /** Multiplies the speed of this media player by ratio. Returns this. */
  @CanIgnoreReturnValue
  fun multiplySpeed(ratio: Float): Self

  /**
   * Sets the pitch of the media player.
   * Only updates when this media player starts playing.
   * Returns this.
   */
  @CanIgnoreReturnValue
  fun setPitch(pitch: Float): Self

  /** Multiplies the pitch of this media player by ratio. Returns this. */
  @CanIgnoreReturnValue
  fun multiplyPitch(ratio: Float): Self

  /** Sets the player to play after this media player is complete. Returns this. */
  @CanIgnoreReturnValue
  fun setNextMediaPlayer(nextPlayer: Self): Self

  /** Sets a listener to invoke when this media player is complete. Returns this. */
  @CanIgnoreReturnValue
  fun setOnCompletionListener(listener: (Self) -> Unit): Self
}