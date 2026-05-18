package dev.code93.daviplata.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String, val attemptCount: Int = 0) : LoginUiState
    data class Locked(val retryAfterSeconds: Int) : LoginUiState
    data class Success(val session: Session) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(phone: String, password: String) {
        if (_uiState.value is LoginUiState.Loading) return
        loginUseCase(phone, password)
            .map { it.toUiState() }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun ApiResult<Session>.toUiState(): LoginUiState = when (this) {
        ApiResult.Loading -> LoginUiState.Loading
        is ApiResult.Success -> LoginUiState.Success(data)
        is ApiResult.Failure -> when (val e = error) {
            is AppError.InvalidCredentials ->
                LoginUiState.Error("Usuario o contraseña incorrectos.", e.attempts ?: 1)
            is AppError.AccountLocked ->
                LoginUiState.Locked(e.retryAfterSeconds)
            is AppError.Validation ->
                LoginUiState.Error(e.msg)
            else ->
                LoginUiState.Error("Algo salió mal. Intenta de nuevo.")
        }
    }
}
