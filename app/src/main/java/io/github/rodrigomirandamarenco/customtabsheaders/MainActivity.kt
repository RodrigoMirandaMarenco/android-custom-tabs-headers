package io.github.rodrigomirandamarenco.customtabsheaders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rodrigomirandamarenco.customtabsheaders.ui.theme.CustomTabsHeadersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomTabsHeadersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        onSubmit = { onSubmit() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    private fun onSubmit() {
        val customTabWithHeaders = CustomTabWithHeaders(
            activity = this,
            // set your URL here:
            urlToOpen = "https://rodrigomirandamarenco.github.io/",
            // set custom headers here:
            headers = mapOf(
                "AnotherValue" to "A random test value",
            )
        )
        customTabWithHeaders.launch()
    }
}

@Composable
fun Greeting(onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                println("Button clicked")
                onSubmit()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Open Custom Tab")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CustomTabsHeadersTheme {
        Greeting({ })
    }
}