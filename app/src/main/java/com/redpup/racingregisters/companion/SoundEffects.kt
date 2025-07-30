package com.redpup.racingregisters.companion

import android.content.Context
import android.media.MediaPlayer


/** Wrapper of different sound effects that can be made in the game.
 *
 */
class SoundEffects(context: Context) {
  val beginEffect = MediaPlayer.create(context, R.raw.effect_begin)
  val resumeEffect = MediaPlayer.create(context, R.raw.effect_start)
  val breakEffect = MediaPlayer.create(context, R.raw.effect_break)

  val countdownEffects = mapOf(
    0 to R.raw.effect_finish,
    1 to R.raw.effect_countdown_1,
    2 to R.raw.effect_countdown_2,
    3 to R.raw.effect_countdown_3,
    4 to R.raw.effect_countdown_4,
    5 to R.raw.effect_countdown_5,
    6 to R.raw.effect_countdown_6,
    7 to R.raw.effect_countdown_7,
    8 to R.raw.effect_countdown_8,
    9 to R.raw.effect_countdown_9,
    10 to R.raw.effect_countdown_10
  ).mapValues { MediaPlayer.create(context, it.value) }
}