package com.redpup.racingregisters.companion.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/**
 * Creates a new MediaPlayer from the given args.
 *
 * Same as MediaPlayer.create(), but doesn't prepare at the end.
 */
private fun create(context: Context, resourceId: Int): MediaPlayer {
  context.resources.openRawResourceFd(resourceId).use { afd ->
    Log.d("Create", "$resourceId: $afd")
    val mp = MediaPlayer()
    mp.setAudioAttributes(AudioAttributes.Builder().build())
    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    afd.close()
    return mp
  }
}

/**
 * An AbstractMediaPlayer that delegates to an underlying MediaPlayer.
 */
data class ForwardingMediaPlayer(val context: Context, val resourceId: Int) :
  AbstractMediaPlayer<ForwardingMediaPlayer> {
  private val mediaPlayer = create(context, resourceId)
  private var isMuted = false
  private var volume = 1.0F

  override fun numMediaPlayers() = 1

  override fun copy(): ForwardingMediaPlayer {
    val player = ForwardingMediaPlayer(context, resourceId)
    player.setIsMuted(isMuted)
    player.setVolume(volume)
    return player
  }

  override fun prepareAsync(listener: () -> Unit) {
    mediaPlayer.setOnPreparedListener { listener.invoke() }
    mediaPlayer.prepareAsync()
  }

  override fun start() {
    mediaPlayer.start()
  }

  override fun pause() {
    mediaPlayer.pause()
  }

  override fun stop() {
    mediaPlayer.stop()
  }

  override fun reset() {
    mediaPlayer.reset()
  }

  override fun isPlaying(): Boolean {
    return mediaPlayer.isPlaying
  }

  override fun seekToStart() {
    mediaPlayer.seekTo(0)
  }

  override fun duration(): Int {
    return mediaPlayer.duration
  }

  override fun setIsMuted(isMuted: Boolean) {
    this.isMuted = isMuted
    if (isMuted) {
      mediaPlayer.setVolume(0.0F, 0.0F)
    } else {
      mediaPlayer.setVolume(volume, volume)
    }
  }

  override fun setVolume(volume: Float) {
    if (!isMuted) {
      mediaPlayer.setVolume(volume, volume)
    }
    this.volume = volume
  }

  override fun multiplyVolume(ratio: Float) {
    setVolume(volume * ratio)
  }

  override fun setNextMediaPlayer(nextPlayer: ForwardingMediaPlayer) {
    mediaPlayer.setNextMediaPlayer(nextPlayer.mediaPlayer)
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    mediaPlayer.setOnCompletionListener(listener)
  }
}