package com.redpup.racingregisters.companion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.redpup.racingregisters.companion.ui.RenderSecondaryButton

@Composable
fun SettingsScreen(
  navController: NavController,
) {
  Scaffold(topBar = { RenderSettingsTopBar(navController) }) { innerPadding ->
    Box(Modifier.padding(innerPadding))
  }
}

@Composable
fun RenderSettingsTopBar(
  navController: NavController,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .offset(0.dp, 40.dp)
      .padding(20.dp),
    horizontalArrangement = Arrangement.Start
  ) {
    RenderSecondaryButton(
      R.drawable.back,
      "Back Button"
    ) {
      navController.navigateUp()
    }
  }
}