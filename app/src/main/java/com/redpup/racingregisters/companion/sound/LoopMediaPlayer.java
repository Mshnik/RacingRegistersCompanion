package com.redpup.racingregisters.companion.sound;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import androidx.annotation.GuardedBy;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Media player that plays a single sound on loop, without gaps.
 *
 * <p>See <a
 * href="https://stackoverflow.com/questions/26274182/not-able-to-achieve-gapless-audio-looping-so-far-on-android">Stack
 * Overflow</a>.
 */
public final class LoopMediaPlayer {

  private final Context context;
  private final int resourceId;

  @GuardedBy("this")
  private @MonotonicNonNull MediaPlayer currentPlayer;

  @GuardedBy("this")
  private @MonotonicNonNull MediaPlayer nextPlayer;

  @GuardedBy("this")
  private float volume;

  @GuardedBy("this")
  private float speed;

  @GuardedBy("this")
  private float speedIncrement;

  public static LoopMediaPlayer create(Context context, int resourceId) {
    return new LoopMediaPlayer(context, resourceId);
  }

  private LoopMediaPlayer(Context context, int resourceId) {
    this.context = context;
    this.resourceId = resourceId;

    this.volume = 1.0f;
    this.speed = 1.0f;
    this.speedIncrement = 0.0f;

    this.currentPlayer = createMediaPlayer();
    this.currentPlayer.setOnPreparedListener(this::setMediaPlayerParams);
    createNextMediaPlayer();
  }

  /**
   * Creates a new media player with the configured parameters.
   */
  private MediaPlayer createMediaPlayer() {
    return MediaPlayer.create(context, resourceId);
  }

  /**
   * Sets params on {@code mp}. This is done separately from {@link #createMediaPlayer()} because
   * setting non-zero speed also calls start.
   */
  private void setMediaPlayerParams(MediaPlayer mp) {
    mp.setVolume(volume, volume);
    mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
  }

  /**
   * Creates and sets the {@link #nextPlayer} field to continue after {@link #currentPlayer} is
   * complete.
   */
  private synchronized void createNextMediaPlayer() {
    nextPlayer = createMediaPlayer();
    currentPlayer.setNextMediaPlayer(nextPlayer);
    currentPlayer.setOnCompletionListener(this::advanceMediaPlayer);
  }

  /**
   * Advances {@code mediaPlayer} to the next player and releases the current {@code mediaPlayer}.
   */
  private synchronized void advanceMediaPlayer(MediaPlayer mediaPlayer) {
    mediaPlayer.release();

    if (speedIncrement > 0) {
      setPlaybackSpeed(speed + speedIncrement);
    }

    currentPlayer = nextPlayer;
    setMediaPlayerParams(currentPlayer);
    createNextMediaPlayer();
  }

  /**
   * Starts this looping player.
   */
  public synchronized void start() {
    currentPlayer.start();
  }

  /**
   * Pauses this looping player.
   */
  public synchronized void pause() {
    currentPlayer.pause();
  }

  /**
   * Sets the volume of this looping player. This volume persists across loops.
   */
  public synchronized void setVolume(float volume) {
    this.volume = volume;
    currentPlayer.setVolume(volume, volume);
    nextPlayer.setVolume(volume, volume);
  }

  /**
   * Sets the speed of this looping player.
   */
  public synchronized void setPlaybackSpeed(float speed) {
    this.speed = speed;
  }

  /**
   * Sets the speed increment to increase every play back.
   */
  public synchronized void setAutoAdvanceSpeedIncrement(float speedIncrement) {
    this.speedIncrement = speedIncrement;
  }
}