package dev.code93.daviplata.presentation.auth.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegisterRoute(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        uiState = uiState,
        onRegister = viewModel::register,
        onNavigateBack = onNavigateBack,
        onSuccess = onRegisterSuccess,
        onResetState = viewModel::resetState,
    )
}
