package com.redpup.racingregisters.companion.sound

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

  override fun prepareAsync(listener: () -> Unit): MultiTrackMediaPlayer<K, T> {
    val fork = ForkedListener<Unit>(mediaPlayers.size, {}, { listener() })
    mediaPlayers.values.forEach { it.prepareAsync { fork.handle(Unit) } }
    return this
  }

  override fun applyPlaybackParams(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.applyPlaybackParams() }
    return this
  }

  override fun start(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.entries.forEach {
      updateVolume(it.key)
      it.value.start()
    }
    return this
  }

  override fun pause(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.pause() }
    return this
  }

  override fun stop(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.stop() }
    return this
  }

  override fun reset(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.reset() }
    return this
  }

  override fun release(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.release() }
    return this
  }

  override fun isPlaying(): Boolean {
    return mediaPlayers.values.first().isPlaying()
  }

  override fun seekToStart(): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.seekToStart() }
    return this
  }

  override fun duration(): Int {
    // Assumes all input players have the same duration.
    return mediaPlayers.values.first().duration()
  }

  override fun setIsMuted(isMuted: Boolean): MultiTrackMediaPlayer<K, T> {
    this.isMuted = isMuted
    mediaPlayers.keys.forEach { updateVolume(it) }
    return this
  }

  /** Updates track volume based on set volume args. */
  private fun updateVolume(track: K) {
    synchronized(tracksLock) {
      mediaPlayers[track]!!.setIsMuted(isMuted || !enabledTracks.contains(track))
    }
  }

  override fun setVolume(volume: Float): MultiTrackMediaPlayer<K, T> {
    if (volume != masterVolume) {
      val ratioDelta = volume / masterVolume
      mediaPlayers.values.forEach { it.multiplyVolume(ratioDelta) }
      masterVolume = volume
    }
    return this
  }

  override fun multiplyVolume(ratio: Float): MultiTrackMediaPlayer<K, T> {
    setVolume(masterVolume * ratio)
    return this
  }

  override fun setSpeed(speed: Float): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.setSpeed(speed) }
    return this
  }

  override fun multiplySpeed(ratio: Float): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.multiplySpeed(ratio) }
    return this
  }

  override fun setPitch(pitch: Float): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.setPitch(pitch) }
    return this
  }

  override fun multiplyPitch(ratio: Float): MultiTrackMediaPlayer<K, T> {
    mediaPlayers.values.forEach { it.multiplyPitch(ratio) }
    return this
  }

  override fun setNextMediaPlayer(nextPlayer: MultiTrackMediaPlayer<K, T>): MultiTrackMediaPlayer<K, T> {
    check(mediaPlayers.keys == nextPlayer.mediaPlayers.keys)
    for (key in mediaPlayers.keys) {
      mediaPlayers[key]!!.setNextMediaPlayer(nextPlayer.mediaPlayers[key]!!)
    }
    return this
  }

  override fun setOnCompletionListener(listener: (MultiTrackMediaPlayer<K, T>) -> Unit): MultiTrackMediaPlayer<K, T> {
    val fork = ForkedListener<T>(
      mediaPlayers.size,
      {
        it.reset()
        it.release()
      },
      { listener(this) })

    mediaPlayers.values.forEach { it.setOnCompletionListener(fork::handle) }
    return this
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