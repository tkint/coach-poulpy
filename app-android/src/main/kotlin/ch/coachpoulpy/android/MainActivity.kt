package ch.coachpoulpy.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ch.coachpoulpy.android.ui.CoachPoulpyRoot
import ch.coachpoulpy.android.ui.CoachPoulpyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as CoachPoulpyApp).graph
        setContent {
            CoachPoulpyTheme {
                CoachPoulpyRoot(graph)
            }
        }
    }
}
