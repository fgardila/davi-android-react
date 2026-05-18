package dev.code93.daviplata.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.usecase.auth.RegisterUseCase
import dev.code93.daviplata.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed interface RegisterUiState {
    data object Idle : RegisterUiState
    data object Loading : RegisterUiState
    data class Error(val message: String) : RegisterUiState
    data object Success : RegisterUiState
}

enum class PasswordStrength { Weak, Fair, Good, Strong }

fun evaluatePasswordStrength(password: String): PasswordStrength {
    var score = 0
    if (password.length >= Validators.PASSWORD_MIN_LENGTH) score++
    if (Validators.hasUppercase(password)) score++
    if (Validators.hasDigit(password)) score++
    if (password.length >= 12 || Validators.hasSymbol(password)) score++
    return when (score) {
        0, 1 -> PasswordStrength.Weak
        2    -> PasswordStrength.Fair
        3    -> PasswordStrength.Good
        else -> PasswordStrength.Strong
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        phone: String,
        name: String,
        document: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
    ) {
        if (_uiState.value is RegisterUiState.Loading) return
        registerUseCase(phone, name, document, email, username, password, confirmPassword)
            .onEach { result ->
                _uiState.value = when (result) {
                    ApiResult.Loading -> RegisterUiState.Loading
                    is ApiResult.Success -> RegisterUiState.Success
                    is ApiResult.Failure -> when (val e = result.error) {
                        is AppError.PhoneTaken ->
                            RegisterUiState.Error("Este número de celular ya está registrado.")
                        is AppError.Validation ->
                            RegisterUiState.Error(e.msg)
                        else ->
                            RegisterUiState.Error("Algo salió mal. Intenta de nuevo.")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
