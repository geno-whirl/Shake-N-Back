package app.shakenback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.shakenback.ui.ShakeNBackApp
import app.shakenback.ui.theme.ShakeNBackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ShakeNBackTheme {
                ShakeNBackApp()
            }
        }
    }
}
