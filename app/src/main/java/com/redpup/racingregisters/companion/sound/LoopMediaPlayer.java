package com.redpup.racingregisters.companion.sound;

import android.content.Context;
import android.media.MediaPlayer;
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

  public static LoopMediaPlayer create(Context context, int resourceId) {
    return new LoopMediaPlayer(context, resourceId);
  }

  private LoopMediaPlayer(Context context, int resourceId) {
    this.context = context;
    this.resourceId = resourceId;

    this.volume = 1.0f;
    this.currentPlayer = createMediaPlayer();
    createNextMediaPlayer();
  }

  /**
   * Creates a new media player with the configured parameters.
   */
  private MediaPlayer createMediaPlayer() {
    MediaPlayer mediaPlayer = MediaPlayer.create(context, resourceId);
    mediaPlayer.setVolume(volume, volume);
    return mediaPlayer;
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
    currentPlayer = nextPlayer;
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
}