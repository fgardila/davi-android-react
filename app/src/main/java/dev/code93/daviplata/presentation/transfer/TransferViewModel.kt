package dev.code93.daviplata.presentation.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.code93.daviplata.domain.common.AppError
import dev.code93.daviplata.domain.common.ApiResult
import dev.code93.daviplata.domain.model.Recipient
import dev.code93.daviplata.domain.model.Transfer
import dev.code93.daviplata.domain.usecase.account.GetBalanceUseCase
import dev.code93.daviplata.domain.usecase.session.GetCurrentSessionUseCase
import dev.code93.daviplata.domain.usecase.transfer.CreateTransferUseCase
import dev.code93.daviplata.domain.usecase.transfer.FindRecipientUseCase
import dev.code93.daviplata.domain.validation.Validators
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class TransferUiState(
    val balance: Double = 0.0,
    val balanceLoading: Boolean = true,
    val recipientState: RecipientState = RecipientState.Idle,
    val submitState: SubmitState = SubmitState.Idle,
)

sealed interface RecipientState {
    data object Idle : RecipientState
    data object Loading : RecipientState
    data class Found(val recipient: Recipient) : RecipientState
    data object NotFound : RecipientState
    data object SelfTransfer : RecipientState
    data class Error(val message: String) : RecipientState
}

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Loading : SubmitState
    data class Success(val transfer: Transfer, val recipientName: String) : SubmitState
    data class Error(val message: String, val code: String? = null) : SubmitState
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val getBalance: GetBalanceUseCase,
    private val findRecipient: FindRecipientUseCase,
    private val createTransfer: CreateTransferUseCase,
    getCurrentSession: GetCurrentSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TransferUiState())
    val state: StateFlow<TransferUiState> = _state.asStateFlow()

    private val currentUserPhone: String? = getCurrentSession()?.phone
    private var lookupJob: Job? = null

    init {
        loadBalance()
    }

    private fun loadBalance() {
        getBalance().onEach { result ->
            when (result) {
                ApiResult.Loading -> Unit
                is ApiResult.Success -> _state.value = _state.value.copy(
                    balance = result.data.amount,
                    balanceLoading = false,
                )
                is ApiResult.Failure -> _state.value = _state.value.copy(balanceLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun lookupRecipient(phone: String) {
        if (Validators.phone(phone) != null) {
            _state.value = _state.value.copy(recipientState = RecipientState.Idle)
            return
        }
        if (phone == currentUserPhone) {
            lookupJob?.cancel()
            _state.value = _state.value.copy(recipientState = RecipientState.SelfTransfer)
            return
        }
        lookupJob?.cancel()
        lookupJob = findRecipient(phone).onEach { result ->
            _state.value = _state.value.copy(
                recipientState = when (result) {
                    ApiResult.Loading -> RecipientState.Loading
                    is ApiResult.Success -> if (result.data != null)
                        RecipientState.Found(result.data) else RecipientState.NotFound
                    is ApiResult.Failure -> when (result.error) {
                        is AppError.NetworkError -> RecipientState.Error("Sin conexión. Verifica tu red.")
                        else -> RecipientState.NotFound
                    }
                },
            )
        }.launchIn(viewModelScope)
    }

    fun submit(toPhone: String, amount: Double, description: String) {
        val recipientName = (_state.value.recipientState as? RecipientState.Found)?.recipient?.name
            ?: return
        if (_state.value.submitState is SubmitState.Loading) return

        createTransfer(toPhone, amount, description).onEach { result ->
            when (result) {
                ApiResult.Loading -> _state.value = _state.value.copy(submitState = SubmitState.Loading)
                is ApiResult.Success -> _state.value = _state.value.copy(
                    submitState = SubmitState.Success(result.data, recipientName),
                    balance = result.data.newBalance,
                )
                is ApiResult.Failure -> {
                    val submitError = when (val e = result.error) {
                        is AppError.InsufficientFunds ->
                            SubmitState.Error("Saldo insuficiente para realizar esta transferencia.", "INSUFFICIENT_FUNDS")
                        is AppError.RecipientNotFound ->
                            SubmitState.Error("Destinatario no encontrado.", "RECIPIENT_NOT_FOUND")
                        is AppError.Validation -> SubmitState.Error(e.msg)
                        else -> SubmitState.Error("Algo salió mal. Intenta de nuevo.")
                    }
                    _state.value = _state.value.copy(submitState = submitError)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetSubmitState() {
        _state.value = _state.value.copy(submitState = SubmitState.Idle)
    }
}
