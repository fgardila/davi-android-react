package dev.code93.daviplata.presentation.session

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.code93.daviplata.presentation.DaviPlataActivity
import dev.code93.daviplata.ui.theme.DaviPlataTheme

@AndroidEntryPoint
class SessionExpiredActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DaviPlataTheme {
                BackHandler {
                    // No back navigation from SessionExpired
                }
                SessionExpiredScreen(
                    onReturnToLogin = {
                        startActivity(
                            Intent(this, DaviPlataActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                    }
                )
            }
        }
    }
}
