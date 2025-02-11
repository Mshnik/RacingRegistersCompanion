package com.redpup.racingregisters.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.redpup.racingregisters.companion.ui.theme.RacingRegistersCompanionTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RacingRegistersCompanionTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Greeting(
            message = Message("Michael", "Hello"),
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

data class Message(val author: String, val body: String)

@Composable
fun Greeting(message: Message, modifier: Modifier = Modifier) {
  Surface(color = Color.Black) {
    Column {
      Image(
        painter = painterResource(R.drawable.circuit),
        contentDescription = "Circuit Image",
        modifier = modifier.size(40.dp)
      )
      Text(
        text = "Hi, my name is ${message.author}!",
        modifier = modifier.padding(24.dp),
      )
      Spacer(modifier = modifier.width(8.dp))
      Text(
        text = "My message is ${message.body}!",
        modifier = modifier.padding(24.dp)
      )
    }
  }
}
