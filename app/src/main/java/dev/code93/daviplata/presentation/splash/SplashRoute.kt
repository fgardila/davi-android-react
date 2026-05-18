package dev.code93.daviplata.presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.code93.daviplata.domain.model.Session

@Composable
fun SplashRoute(
    onSecurityBlocked: () -> Unit,
    onAuthCompleted: (session: Session) -> Unit,
    onSessionMissing: () -> Unit,
    onSplashContentReady: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToHome -> onAuthCompleted(state.session)
            SplashUiState.NavigateToLogin -> onSessionMissing()
            else -> Unit
        }
        if (uiState !is SplashUiState.Loading) {
            onSplashContentReady()
        }
    }

    SplashScreen(
        uiState = uiState,
        onBlockedDismiss = onSecurityBlocked,
    )
}
