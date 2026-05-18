package dev.code93.daviplata.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.code93.daviplata.presentation.auth.login.LoginRoute
import dev.code93.daviplata.presentation.auth.register.RegisterRoute
import dev.code93.daviplata.presentation.splash.SplashRoute

@Composable
fun AppNavHost(
    coordinator: AppCoordinator,
    onSplashContentReady: () -> Unit,
) {
    NavHost(
        navController = coordinator.navController,
        startDestination = AppRoute.Splash,
    ) {
        composable<AppRoute.Splash> {
            SplashRoute(
                onSecurityBlocked = coordinator::blocked,
                onAuthCompleted = coordinator::completed,
                onSessionMissing = coordinator::toLoginFromSplash,
                onSplashContentReady = onSplashContentReady,
            )
        }
        composable<AppRoute.Login> {
            LoginRoute(
                onNavigateToRegister = coordinator::toRegister,
                onLoginSuccess = coordinator::completed,
            )
        }
        composable<AppRoute.Register> {
            RegisterRoute(
                onNavigateBack = coordinator::back,
                onRegisterSuccess = coordinator::back,
            )
        }
    }
}
