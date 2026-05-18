package dev.code93.daviplata.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.code93.daviplata.domain.model.Session

@Stable
class AppCoordinator(
    val navController: NavHostController,
    private val onAuthCompleted: (session: Session) -> Unit,
    private val onSecurityBlocked: () -> Unit,
) {
    fun toLoginFromSplash() = navController.navigate(AppRoute.Login) {
        popUpTo(AppRoute.Splash) { inclusive = true }
        launchSingleTop = true
    }
    fun toRegister() = navController.navigate(AppRoute.Register) {
        launchSingleTop = true
    }
    fun back() { navController.popBackStack() }
    fun completed(session: Session) = onAuthCompleted(session)
    fun blocked() = onSecurityBlocked()
}

@Composable
fun rememberAppCoordinator(
    navController: NavHostController = rememberNavController(),
    onAuthCompleted: (session: Session) -> Unit,
    onSecurityBlocked: () -> Unit,
): AppCoordinator = remember(navController) {
    AppCoordinator(navController, onAuthCompleted, onSecurityBlocked)
}
