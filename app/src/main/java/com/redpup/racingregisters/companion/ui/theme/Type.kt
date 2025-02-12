package com.redpup.racingregisters.companion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.R

val mPlus1Code = FontFamily(Font(R.font.m_plus_1_code))
val anonymousPro = FontFamily(Font(R.font.anonymous_pro))
val sixtyFour = FontFamily(Font(R.font.sixtyfour))

private val DefaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
  bodyLarge = DefaultTypography.bodyLarge.copy(
    fontFamily = anonymousPro
  ),
  bodyMedium = DefaultTypography.bodyMedium.copy(
    fontFamily = anonymousPro
  ),
  bodySmall = DefaultTypography.bodySmall.copy(
    fontFamily = anonymousPro
  ),
  displayLarge = DefaultTypography.displayLarge.copy(
    fontFamily = anonymousPro
  ),
  displayMedium = DefaultTypography.displayMedium.copy(
    fontFamily = anonymousPro
  ),
  displaySmall = DefaultTypography.displaySmall.copy(
    fontFamily = anonymousPro
  ),
  titleLarge = DefaultTypography.titleLarge.copy(
    fontFamily = mPlus1Code
  ),
  titleMedium = DefaultTypography.titleMedium.copy(
    fontFamily = mPlus1Code
  ),
  titleSmall = DefaultTypography.titleSmall.copy(
    fontFamily = mPlus1Code
  ),
  headlineLarge = DefaultTypography.headlineLarge.copy(
    fontFamily = mPlus1Code
  ),
  headlineMedium = DefaultTypography.headlineMedium.copy(
    fontFamily = mPlus1Code
  ),
  headlineSmall = DefaultTypography.headlineSmall.copy(
    fontFamily = mPlus1Code
  ),
  labelLarge = DefaultTypography.labelLarge.copy(
    fontFamily = sixtyFour
  ),
  labelMedium = DefaultTypography.labelMedium.copy(
    fontFamily = mPlus1Code
  ),
  labelSmall = DefaultTypography.labelSmall.copy(
    fontFamily = mPlus1Code
  ),
)