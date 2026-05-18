package dev.code93.daviplata.presentation.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.code93.daviplata.domain.model.Session

@Composable
fun LoginRoute(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (session: Session) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        uiState = uiState,
        onLogin = viewModel::login,
        onNavigateToRegister = onNavigateToRegister,
        onLoginSuccess = onLoginSuccess,
        onResetState = viewModel::resetState,
    )
}
