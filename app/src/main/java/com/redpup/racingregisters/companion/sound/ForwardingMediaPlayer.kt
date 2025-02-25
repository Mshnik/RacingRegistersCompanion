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

  override fun applyPlaybackParams(): ForwardingMediaPlayer {
    mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed).setPitch(pitch)
    return this
  }

  override fun prepareAsync(listener: () -> Unit): ForwardingMediaPlayer {
    mediaPlayer.setOnPreparedListener {
      isPrepared = true
      listener.invoke()
    }
    applyPlaybackParams()
    mediaPlayer.prepareAsync()
    return this
  }

  override fun start(): ForwardingMediaPlayer {
    check(isPrepared) { "Player $id is not prepared" }
    applyPlaybackParams()
    mediaPlayer.start()
    return this
  }

  override fun pause(): ForwardingMediaPlayer {
    mediaPlayer.pause()
    return this
  }

  override fun stop(): ForwardingMediaPlayer {
    mediaPlayer.stop()
    return this
  }

  override fun softReset(): ForwardingMediaPlayer {
    // TODO: Fix this. It causes a jump ahead in looping for some reason, even when speed is 1.
    // applyPlaybackParams()
    pause()
    seekToStart()
    return this
  }

  override fun reset(): ForwardingMediaPlayer {
    mediaPlayer.reset()
    return this
  }

  override fun release(): ForwardingMediaPlayer {
    mediaPlayer.release()
    return this
  }

  override fun isPlaying(): Boolean {
    return mediaPlayer.isPlaying
  }

  override fun seekToStart(): ForwardingMediaPlayer {
    mediaPlayer.seekTo(0)
    return this
  }

  override fun duration(): Int {
    return mediaPlayer.duration
  }

  override fun setIsMuted(isMuted: Boolean): ForwardingMediaPlayer {
    this.isMuted = isMuted
    if (isMuted) {
      mediaPlayer.setVolume(0.0F, 0.0F)
    } else {
      mediaPlayer.setVolume(volume, volume)
    }
    return this
  }

  override fun setVolume(volume: Float): ForwardingMediaPlayer {
    if (!isMuted) {
      mediaPlayer.setVolume(volume, volume)
    }
    this.volume = volume
    return this
  }

  override fun multiplyVolume(ratio: Float): ForwardingMediaPlayer {
    setVolume(volume * ratio)
    return this
  }

  override fun setSpeed(speed: Float): ForwardingMediaPlayer {
    this.speed = speed
    return this
  }

  override fun multiplySpeed(ratio: Float): ForwardingMediaPlayer {
    setSpeed(speed * ratio)
    return this
  }

  override fun setPitch(pitch: Float): ForwardingMediaPlayer {
    this.pitch = pitch
    return this
  }

  override fun multiplyPitch(ratio: Float): ForwardingMediaPlayer {
    setPitch(pitch * ratio)
    return this
  }

  override fun setNextMediaPlayer(nextPlayer: ForwardingMediaPlayer): ForwardingMediaPlayer {
    check(isPrepared) { "Player $id is not prepared" }
    check(nextPlayer.isPrepared) { "Player ${nextPlayer.id} is not prepared" }
    mediaPlayer.setNextMediaPlayer(nextPlayer.mediaPlayer)
    return this
  }

  override fun setOnCompletionListener(listener: (ForwardingMediaPlayer) -> Unit): ForwardingMediaPlayer {
    mediaPlayer.setOnCompletionListener { listener(this) }
    return this
  }
}