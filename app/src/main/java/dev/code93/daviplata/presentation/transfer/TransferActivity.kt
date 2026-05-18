package dev.code93.daviplata.presentation.transfer

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.code93.daviplata.bridge.BridgeEventBus
import dev.code93.daviplata.bridge.BridgeEvents
import com.facebook.react.bridge.Arguments
import dev.code93.daviplata.ui.theme.DaviPlataTheme
import javax.inject.Inject

@AndroidEntryPoint
class TransferActivity : ComponentActivity() {

    private val viewModel: TransferViewModel by viewModels()

    @Inject lateinit var eventBus: BridgeEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DaviPlataTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()

                TransferScreen(
                    state = state,
                    onLookupRecipient = viewModel::lookupRecipient,
                    onSubmit = { phone, amount, desc -> viewModel.submit(phone, amount, desc) },
                    onResetSubmit = viewModel::resetSubmitState,
                    onBack = { finish() },
                    onTransferComplete = { newBalance ->
                        hapticSuccess()
                        val params = Arguments.createMap().apply {
                            putDouble("newBalance", newBalance)
                        }
                        eventBus.emit(BridgeEvents.TRANSFER_COMPLETED, params)
                        finish()
                    },
                )

                // Haptic on error
                val submitState = state.submitState
                LaunchedEffect(submitState) {
                    if (submitState is SubmitState.Error) hapticError()
                }
            }
        }
    }

    private fun hapticSuccess() {
        window.decorView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    private fun hapticError() {
        window.decorView.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}
