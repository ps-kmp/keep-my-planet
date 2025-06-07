package pt.isel.keepmyplanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appViewModel = remember { AppViewModel() }
            App(appViewModel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val appViewModel = remember { AppViewModel() }
    App(appViewModel)
}
