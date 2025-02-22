package com.redpup.racingregisters.companion.sound

import android.content.Context
import android.content.res.Resources
import android.media.MediaPlayer
import android.util.Log
import com.redpup.racingregisters.companion.R


/**
 * An AbstractMediaPlayer that delegates to an underlying MediaPlayer.
 */
data class ForwardingMediaPlayer(val context: Context, val resourceId: Int) :
  AbstractMediaPlayer<ForwardingMediaPlayer> {
  private val mediaPlayer = MediaPlayer.create(context, resourceId)
  private var isMuted = false
  private var volume = 1.0F
  private var speed = 1.0F
  private var playbackSpeedIncrement = 0.0F

  override fun copy(): ForwardingMediaPlayer {
    val player = ForwardingMediaPlayer(context, resourceId)
    player.setIsMuted(isMuted)
    player.setVolume(volume)
    player.setPlaybackSpeedIncrement(playbackSpeedIncrement)
    player.speed = speed
    return player
  }

  override fun start() {
    mediaPlayer.start()
    mediaPlayer.playbackParams.setSpeed(speed)
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

  override fun seekToStart() {
    mediaPlayer.seekTo(0)
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

  override fun setPlaybackSpeed(speed: Float) {
    this.speed = speed
    mediaPlayer.playbackParams.setSpeed(speed)
  }

  override fun setPlaybackSpeedIncrement(speedIncrement: Float) {
    playbackSpeedIncrement = speedIncrement
  }

  override fun incrementSpeed() {
    setPlaybackSpeed(mediaPlayer.playbackParams.speed + playbackSpeedIncrement)
  }

  override fun setNextMediaPlayer(nextPlayer: ForwardingMediaPlayer) {
    mediaPlayer.setNextMediaPlayer(nextPlayer.mediaPlayer)
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    mediaPlayer.setOnCompletionListener(listener)
  }
}