package dev.code93.daviplata.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.usecase.security.CheckSecurityUseCase
import dev.code93.daviplata.domain.usecase.security.SecurityStatus
import dev.code93.daviplata.domain.usecase.session.GetCurrentSessionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToLogin : SplashUiState
    data class NavigateToHome(val session: Session) : SplashUiState
    data class Blocked(val reason: SecurityStatus) : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkSecurity: CheckSecurityUseCase,
    private val getCurrentSession: GetCurrentSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        check()
    }

    private fun check() {
        viewModelScope.launch {
            delay(1500L)
            val security = checkSecurity()
            if (security != SecurityStatus.SAFE) {
                _uiState.value = SplashUiState.Blocked(security)
                return@launch
            }
            val session = getCurrentSession()
            _uiState.value = if (session != null && session.expiresAtMillis > System.currentTimeMillis()) {
                SplashUiState.NavigateToHome(session)
            } else {
                SplashUiState.NavigateToLogin
            }
        }
    }
}
