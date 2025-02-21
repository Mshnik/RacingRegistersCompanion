package com.redpup.racingregisters.companion.sound

import android.media.MediaPlayer

/**
 * An AbstractMediaPlayer that wraps many MediaPlayers and operates on them in sync.
 */
data class MultiTrackMediaPlayer<K, T : AbstractMediaPlayer<T>>(val mediaPlayers: Map<K, T>) :
  AbstractMediaPlayer<MultiTrackMediaPlayer<K, T>> {

  private var isMuted = false
  private var masterVolume: Float = 1.0F

  private val tracksLock = Object()
  private val enabledTracks = mutableSetOf<K>()

  override fun copy(): MultiTrackMediaPlayer<K, T> {
    val player = MultiTrackMediaPlayer(mediaPlayers.mapValues { it.value.copy() })
    player.isMuted = isMuted
    player.masterVolume = masterVolume
    player.enabledTracks.addAll(enabledTracks)
    return player
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

  override fun setPlaybackSpeed(speed: Float) {
    mediaPlayers.values.forEach { it.setPlaybackSpeed(speed) }
  }

  override fun setPlaybackSpeedIncrement(speedIncrement: Float) {
    mediaPlayers.values.forEach { it.setPlaybackSpeedIncrement(speedIncrement) }
  }

  override fun incrementSpeed() {
    mediaPlayers.values.forEach { it.incrementSpeed() }
  }

  override fun setNextMediaPlayer(nextPlayer: MultiTrackMediaPlayer<K, T>) {
    check(mediaPlayers.keys == nextPlayer.mediaPlayers.keys)
    for (key in mediaPlayers.keys) {
      mediaPlayers[key]!!.setNextMediaPlayer(nextPlayer.mediaPlayers[key]!!)
    }
  }

  override fun setOnCompletionListener(listener: (MediaPlayer) -> Unit) {
    // Only set on first listener, to avoid exploding listener invocations when this ends.
    mediaPlayers.values.first().setOnCompletionListener(listener)

    // For all other listeners, make sure to reset and release.
    mediaPlayers.values.stream().skip(1).forEach { p ->
      p.setOnCompletionListener { m ->
        m.reset()
        m.release()
      }
    }
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