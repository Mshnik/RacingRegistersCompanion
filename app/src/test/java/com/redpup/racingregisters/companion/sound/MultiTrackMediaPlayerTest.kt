package com.redpup.racingregisters.companion.sound

import com.google.common.truth.Truth.assertThat
import com.redpup.racingregisters.companion.sound.testing.FakeMediaPlayer
import com.redpup.racingregisters.companion.sound.testing.State
import com.redpup.racingregisters.companion.timer.testing.FakeSystemTimer
import org.junit.Test

class MultiTrackMediaPlayerTest {
  private val timer = FakeSystemTimer()
  private val player1 = FakeMediaPlayer(timer, prepareMillis = 10L)
  private val player2 = FakeMediaPlayer(timer, prepareMillis = 10L)
  private val player3 = FakeMediaPlayer(timer, prepareMillis = 10L)
  private val multiTrackPlayer =
    MultiTrackMediaPlayer(mapOf(Pair(1, player1), Pair(2, player2), Pair(3, player3)))

  @Test
  fun prepare() {
    multiTrackPlayer.prepareAsync {}
    timer.advance(100L)
    multiTrackPlayer.mediaPlayers.values.forEach { assertThat(it.state).isEqualTo(State.PREPARED) }
  }
}
