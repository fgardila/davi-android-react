package dev.code93.daviplata.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.presentation.home.HomeReactActivity
import dev.code93.daviplata.presentation.navigation.AppNavHost
import dev.code93.daviplata.presentation.navigation.rememberAppCoordinator
import dev.code93.daviplata.ui.theme.DaviPlataTheme
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class DaviPlataActivity : ComponentActivity() {

    private val systemSplashVisible = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { systemSplashVisible.value }

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DaviPlataTheme {
                val coordinator = rememberAppCoordinator(
                    onAuthCompleted = ::navigateToHome,
                    onSecurityBlocked = ::finishAffinity,
                )
                AppNavHost(
                    coordinator = coordinator,
                    onSplashContentReady = { systemSplashVisible.value = false },
                )
            }
        }
    }

    private fun navigateToHome(session: Session) {
        startActivity(
            Intent(this, HomeReactActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("screen", "HOME")
                putExtra("userId", session.userId)
                putExtra("name", session.name)
            }
        )
        finish()
    }
}
