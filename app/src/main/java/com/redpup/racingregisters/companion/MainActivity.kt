package com.redpup.racingregisters.companion

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
  Row(
    modifier = modifier
      .padding(all = 8.dp)
      .border(1.5.dp, MaterialTheme.colorScheme.primary)
  ) {
    Image(
      painter = painterResource(R.drawable.circuit),
      contentDescription = "Circuit Image",
      modifier = modifier
        .size(40.dp)
    )
    Column {
      Text(
        text = "Hi, my name is ${message.author}!",
        modifier = modifier.padding(24.dp),
      )
      Spacer(modifier = modifier.width(8.dp))
      Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
        Text(
          text = "My message is ${message.body}!",
          color = MaterialTheme.colorScheme.secondary,
          style = MaterialTheme.typography.bodySmall,
          modifier = modifier.padding(24.dp)
        )
      }
    }
  }
}

@Preview(name = "Light Mode")
@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  showBackground = true,
  name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
  RacingRegistersCompanionTheme {
    Surface {
      Greeting(message = Message("Michael", "Hello"))
    }
  }
}