package dev.code93.daviplata.presentation.auth.register

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.code93.daviplata.ui.theme.DaviPlataTheme

@Preview(showBackground = true, name = "Step 1 - Datos")
@Composable
fun RegisterScreenStep1Preview() {
    DaviPlataTheme {
        RegisterScreen(
            uiState = RegisterUiState.Idle,
            initialStep = 0,
            onRegister = { _, _, _, _, _, _, _ -> },
            onNavigateBack = {},
            onSuccess = {},
            onResetState = {},
        )
    }
}

@Preview(showBackground = true, name = "Step 2 - Acceso")
@Composable
fun RegisterScreenStep2Preview() {
    DaviPlataTheme {
        RegisterScreen(
            uiState = RegisterUiState.Idle,
            initialStep = 1,
            onRegister = { _, _, _, _, _, _, _ -> },
            onNavigateBack = {},
            onSuccess = {},
            onResetState = {},
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun RegisterScreenErrorPreview() {
    DaviPlataTheme {
        RegisterScreen(
            uiState = RegisterUiState.Error("Este número de celular ya está registrado."),
            initialStep = 0,
            onRegister = { _, _, _, _, _, _, _ -> },
            onNavigateBack = {},
            onSuccess = {},
            onResetState = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun RegisterScreenLoadingPreview() {
    DaviPlataTheme {
        RegisterScreen(
            uiState = RegisterUiState.Loading,
            initialStep = 1,
            onRegister = { _, _, _, _, _, _, _ -> },
            onNavigateBack = {},
            onSuccess = {},
            onResetState = {},
        )
    }
}
