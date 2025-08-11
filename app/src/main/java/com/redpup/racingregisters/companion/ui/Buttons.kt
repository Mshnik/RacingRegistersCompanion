package com.redpup.racingregisters.companion.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redpup.racingregisters.companion.flow.asState
import com.redpup.racingregisters.companion.ui.theme.Grey50
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.White90
import com.redpup.racingregisters.companion.ui.theme.sixtyFour

/**
 * Renders a primary button used in the center screen with static state.
 */
@Composable
fun RenderPrimaryButton(
  text: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  textColor: Color = Color.Black,
  backgroundColor: Color = White90,
  borderColor: Color = White90,
  onClick: () -> Unit,
) {
  RenderPrimaryButton(
    text.asState(),
    modifier = modifier,
    enabled = enabled.asState(),
    textColor = textColor.asState(),
    backgroundColor = backgroundColor.asState(),
    borderColor = borderColor.asState(),
    onClick
  )
}

/**
 * Renders a primary button used in the center screen.
 */
@Composable
fun RenderPrimaryButton(
  text: State<String>,
  modifier: Modifier = Modifier,
  enabled: State<Boolean> = true.asState(),
  textColor: State<Color> = Color.Black.asState(),
  backgroundColor: State<Color> = White90.asState(),
  borderColor: State<Color> = White90.asState(),
  onClick: () -> Unit,
) {
  val buttonFont = TextStyle(
    fontFamily = sixtyFour,
    fontWeight = FontWeight.Bold,
    fontSize = 23.sp,
    lineHeight = 0.sp,
    letterSpacing = 2.sp,
    shadow = Shadow(color = Grey50, offset = Offset(6F, 6F), blurRadius = 0f)
  )

  val borderThickness = 3.dp

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(borderThickness * 2))
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Button(
      onClick = onClick,
      enabled = enabled.value,
      border = BorderStroke(
        width = borderThickness, color = borderColor.value
      ),
      colors = ButtonColors(backgroundColor.value, textColor.value, Grey90, textColor.value),
      shape = RoundedCornerShape(borderThickness),
      modifier = modifier.padding(borderThickness)
    ) {
      Text(
        text.value, style = buttonFont, modifier = modifier.padding(0.dp, 15.dp)
      )
    }
  }
}


/**
 * Renders a secondary button used in the top bar.
 */
@Composable
fun RenderSecondaryButton(
  @DrawableRes id: Int,
  description: String,
  enabled: State<Boolean> = true.asState(),
  onClick: () -> Unit,
) {
  val size = 50.dp

  Button(
    onClick = onClick,
    enabled = enabled.value,
    colors = ButtonColors(
      Color.Black, Color.Black, Color.Black, Color.Black
    ),
    shape = CircleShape,
    border = BorderStroke(width = 3.dp, color = Grey90),
    modifier = Modifier.size(size),
    contentPadding = PaddingValues(size * 0.2F)
  ) {
    Image(
      painter = painterResource(id),
      contentDescription = description,
      colorFilter = ColorFilter.tint(White90)
    )
  }
}