package com.redpup.racingregisters.companion.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

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

private val idCounter = AtomicInteger()

/**
 * An AbstractMediaPlayer that delegates to an underlying MediaPlayer.
 */
class ForwardingMediaPlayer(private val context: Context, private val resourceId: Int) :
  AbstractMediaPlayer<ForwardingMediaPlayer> {
  private val mediaPlayer = create(context, resourceId)
  private val id = idCounter.getAndIncrement()
  private var isPrepared = false
  private var isMuted = false
  private var volume = 1.0F
  private var speed = 1.0F
  private var pitch = 1.0F

  override fun numMediaPlayers() = 1

  override fun copy(): ForwardingMediaPlayer {
    val player = ForwardingMediaPlayer(context, resourceId)
    player.setIsMuted(isMuted)
    player.setVolume(volume)
    player.setSpeed(speed)
    player.setPitch(pitch)
    return player
  }

  override fun prepareAsync(listener: () -> Unit) {
    mediaPlayer.setOnPreparedListener {
      isPrepared = true
      listener.invoke()
    }
    mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed).setPitch(pitch)
    mediaPlayer.prepareAsync()
  }

  override fun start() {
    check(isPrepared) { "Player $id is not prepared" }
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

  override fun setSpeed(speed: Float) {
    this.speed = speed
  }

  override fun multiplySpeed(ratio: Float) {
    setSpeed(speed * ratio)
  }

  override fun setPitch(pitch: Float) {
    this.pitch = pitch
  }

  override fun multiplyPitch(ratio: Float) {
    setPitch(pitch * ratio)
  }

  override fun setNextMediaPlayer(nextPlayer: ForwardingMediaPlayer) {
    check(isPrepared) { "Player $id is not prepared" }
    check(nextPlayer.isPrepared) { "Player ${nextPlayer.id} is not prepared" }
    mediaPlayer.setNextMediaPlayer(nextPlayer.mediaPlayer)
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    mediaPlayer.setOnCompletionListener(listener)
  }
}