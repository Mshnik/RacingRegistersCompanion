package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer
import com.redpup.racingregisters.companion.event.ForkedListener

/**
 * An AbstractMediaPlayer that wraps many MediaPlayers and operates on them in sync.
 */
data class MultiTrackMediaPlayer<K, T : AbstractMediaPlayer<T>>(val mediaPlayers: Map<K, T>) :
  AbstractMediaPlayer<MultiTrackMediaPlayer<K, T>> {

  private var isMuted = false
  private var masterVolume: Float = 1.0F

  private val tracksLock = Object()
  private val enabledTracks = mutableSetOf<K>()

  override fun numMediaPlayers() = mediaPlayers.values.sumOf { it.numMediaPlayers() }

  override fun copy(): MultiTrackMediaPlayer<K, T> {
    val player = MultiTrackMediaPlayer(mediaPlayers.mapValues { it.value.copy() })
    player.setIsMuted(isMuted)
    // Master volume is already applied by copying the volume of the underlying tracks.
    // Applying it again here would exponentially decay the overall volume over many copies.
    player.masterVolume = masterVolume
    enabledTracks.forEach { player.setTrackEnabled(it, true) }
    return player
  }

  override fun prepareAsync(listener: () -> Unit) {
    val fork = ForkedListener<Unit>(mediaPlayers.size, {}, { listener() })
    mediaPlayers.values.forEach { it.prepareAsync { fork.handle(Unit) } }
  }

  override fun start() {
    mediaPlayers.entries.forEach {
      updateVolume(it.key)
      it.value.start()
    }
  }

  override fun pause() {
    mediaPlayers.values.forEach { it.pause() }
  }

  override fun stop() {
    mediaPlayers.values.forEach { it.stop() }
  }

  override fun reset() {
    mediaPlayers.values.forEach { it.reset() }
  }

  override fun release() {
    mediaPlayers.values.forEach { it.release() }
  }

  override fun isPlaying(): Boolean {
    return mediaPlayers.values.first().isPlaying()
  }

  override fun seekToStart() {
    mediaPlayers.values.forEach { it.seekToStart() }
  }

  override fun duration(): Int {
    // Assumes all input players have the same duration.
    return mediaPlayers.values.first().duration()
  }

  override fun setIsMuted(isMuted: Boolean) {
    this.isMuted = isMuted
    mediaPlayers.keys.forEach { updateVolume(it) }
  }

  /** Updates track volume based on set volume args. */
  private fun updateVolume(track: K) {
    synchronized(tracksLock) {
      mediaPlayers[track]!!.setIsMuted(isMuted || !enabledTracks.contains(track))
    }
  }

  override fun setVolume(volume: Float) {
    if (volume != masterVolume) {
      val ratioDelta = volume / masterVolume
      mediaPlayers.values.forEach { it.multiplyVolume(ratioDelta) }
      masterVolume = volume
    }
  }

  override fun multiplyVolume(ratio: Float) {
    setVolume(masterVolume * ratio)
  }

  override fun setSpeed(speed: Float) {
    mediaPlayers.values.forEach { it.setSpeed(speed) }
  }

  override fun multiplySpeed(ratio: Float) {
    mediaPlayers.values.forEach { it.multiplySpeed(ratio) }
  }

  override fun setPitch(pitch: Float) {
    mediaPlayers.values.forEach { it.setPitch(pitch) }
  }

  override fun multiplyPitch(ratio: Float) {
    mediaPlayers.values.forEach { it.multiplyPitch(ratio) }
  }

  override fun setNextMediaPlayer(nextPlayer: MultiTrackMediaPlayer<K, T>) {
    check(mediaPlayers.keys == nextPlayer.mediaPlayers.keys)
    for (key in mediaPlayers.keys) {
      mediaPlayers[key]!!.setNextMediaPlayer(nextPlayer.mediaPlayers[key]!!)
    }
  }

  override fun setOnCompletionListener(listener: (MultiTrackMediaPlayer<K, T>) -> Unit) {
    val fork = ForkedListener<T>(
      mediaPlayers.size,
      {
        it.reset()
        it.release()
      },
      { listener(this) })

    mediaPlayers.values.forEach { it.setOnCompletionListener(fork::handle) }
  }

  /** Returns the number of enabled tracks. */
  fun numTracksEnabled(): Int {
    synchronized(tracksLock) {
      return enabledTracks.size
    }
  }

  /** Returns whether the given track is enabled. */
  fun isTrackEnabled(track: K): Boolean {
    synchronized(tracksLock) {
      return enabledTracks.contains(track)
    }
  }

  /** Enables or disables track. */
  fun setTrackEnabled(track: K, enabled: Boolean) {
    synchronized(tracksLock) {
      if (enabled) {
        enabledTracks.add(track)
      } else {
        enabledTracks.remove(track)
      }

      updateVolume(track)
    }
  }
}