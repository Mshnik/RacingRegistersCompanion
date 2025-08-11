package com.redpup.racingregisters.companion.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.redpup.racingregisters.companion.ui.theme.Grey90
import com.redpup.racingregisters.companion.ui.theme.White90

/**
 * Renders a secondary button used in the top bar.
 */
@Composable
fun RenderSecondaryButton(
  @DrawableRes id: Int,
  description: String,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  val size = 50.dp

  Button(
    onClick = {
      onClick()
    },
    enabled = enabled,
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
      contentDescription = "Back icon",
      colorFilter = ColorFilter.tint(White90)
    )
  }
}