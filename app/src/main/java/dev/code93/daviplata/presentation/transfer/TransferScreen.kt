package dev.code93.daviplata.presentation.transfer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.R
import dev.code93.daviplata.domain.model.Transfer
import dev.code93.daviplata.domain.validation.Validators
import dev.code93.daviplata.ui.common.AlertBanner
import dev.code93.daviplata.ui.common.AlertType
import dev.code93.daviplata.ui.common.AmountInput
import dev.code93.daviplata.ui.common.DaviAppBar
import dev.code93.daviplata.ui.common.DaviOutlinedInput
import dev.code93.daviplata.ui.common.PrimaryButton
import dev.code93.daviplata.ui.common.TextLinkButton
import dev.code93.daviplata.ui.common.screens.ResultAction
import dev.code93.daviplata.ui.common.screens.ResultOverlay
import dev.code93.daviplata.ui.common.screens.ResultVariant
import dev.code93.daviplata.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

private val QUICK_AMOUNTS = listOf(10_000.0, 20_000.0, 50_000.0, 100_000.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    state: TransferUiState,
    onLookupRecipient: (String) -> Unit,
    onSubmit: (phone: String, amount: Double, description: String) -> Unit,
    onResetSubmit: () -> Unit,
    onBack: () -> Unit,
    onTransferComplete: (newBalance: Double) -> Unit,
) {
    var phone by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }

    val amount = rawAmount.toDoubleOrNull() ?: 0.0
    val recipientFound = state.recipientState is RecipientState.Found
    val canContinue = recipientFound && amount > 0 && amount <= state.balance
        && state.submitState !is SubmitState.Loading

    // Navigate away when success is consumed
    LaunchedEffect(state.submitState) {
        if (state.submitState is SubmitState.Success) showSheet = false
    }

    Scaffold(
        topBar = { DaviAppBar(title = stringResource(R.string.transfer_title), onBack = onBack) },
        containerColor = SurfaceLight,
    ) { pad ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Balance card
                BalanceInfoCard(balance = state.balance, loading = state.balanceLoading)

                Spacer(Modifier.height(Spacing.s24))

                // Recipient section
                SectionCard(title = stringResource(R.string.transfer_who_title)) {
                    val recipientErrorMessage: String? = when {
                        Validators.phone(phone) != null -> null
                        state.recipientState is RecipientState.NotFound ->
                            stringResource(R.string.transfer_recipient_not_found)
                        state.recipientState is RecipientState.SelfTransfer ->
                            stringResource(R.string.transfer_recipient_self)
                        state.recipientState is RecipientState.Error ->
                            (state.recipientState as RecipientState.Error).message
                        else -> null
                    }
                    DaviOutlinedInput(
                        value = phone,
                        onValueChange = { v ->
                            if (v.length <= 10) {
                                phone = v
                                onLookupRecipient(v)
                            }
                        },
                        label = stringResource(R.string.register_phone_label),
                        leadingIcon = Icons.Outlined.PhoneAndroid,
                        keyboardType = KeyboardType.Phone,
                        maxLength = 10,
                        error = recipientErrorMessage,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AnimatedVisibility(
                        visible = state.recipientState is RecipientState.Found,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        val name = (state.recipientState as? RecipientState.Found)?.recipient?.name ?: ""
                        RecipientPreviewCard(name = name)
                    }
                    if (state.recipientState is RecipientState.Loading) {
                        LinearProgressIndicator(
                            color = BrandRed,
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s8),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.s16))

                // Amount section
                SectionCard(title = stringResource(R.string.transfer_how_much_title)) {
                    AmountInput(
                        rawValue = rawAmount,
                        onValueChange = { rawAmount = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (amount > state.balance && amount > 0) {
                        Spacer(Modifier.height(Spacing.s4))
                        Text(
                            stringResource(R.string.transfer_insufficient_balance, formatCOP(state.balance)),
                            style = DaviTextStyles.BodySmall,
                            color = AppError,
                        )
                    }
                    Spacer(Modifier.height(Spacing.s12))
                    QuickAmountChips(
                        balance = state.balance,
                        onSelect = { rawAmount = it.toLong().toString() },
                    )
                }

                Spacer(Modifier.height(Spacing.s16))

                // Description section
                SectionCard(title = stringResource(R.string.transfer_concept_title)) {
                    DaviOutlinedInput(
                        value = description,
                        onValueChange = { if (it.length <= 100) description = it },
                        label = stringResource(R.string.transfer_concept_label),
                        maxLength = 100,
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(Spacing.s24))

                // Error banner
                AnimatedVisibility(visible = state.submitState is SubmitState.Error) {
                    val msg = (state.submitState as? SubmitState.Error)?.message ?: ""
                    Column {
                        AlertBanner(
                            type = AlertType.Error, text = msg,
                            modifier = Modifier.padding(horizontal = Spacing.s16)
                        )
                        Spacer(Modifier.height(Spacing.s16))
                    }
                }

                PrimaryButton(
                    text = stringResource(R.string.register_button_continue),
                    onClick = { onResetSubmit(); showSheet = true },
                    enabled = canContinue,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.s16),
                )

                Spacer(Modifier.height(Spacing.s32))
            }

            // Success overlay
            val success = state.submitState as? SubmitState.Success
            AnimatedVisibility(
                visible = success != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (success != null) {
                    SuccessOverlay(
                        transfer = success.transfer,
                        recipientName = success.recipientName,
                        onDone = { onTransferComplete(success.transfer.newBalance) },
                    )
                }
            }
        }
    }

    // Confirmation bottom sheet
    if (showSheet) {
        val recipientName = (state.recipientState as? RecipientState.Found)?.recipient?.name ?: ""
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = White,
            shape = RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl),
        ) {
            ConfirmationSheet(
                recipientName = recipientName,
                phone = phone,
                amount = amount,
                description = description,
                loading = state.submitState is SubmitState.Loading,
                onConfirm = { onSubmit(phone, amount, description) },
                onCancel = { showSheet = false },
            )
        }
    }
}

// ─── Sub-composables ────────────────────────────────────────────────────

@Composable
private fun BalanceInfoCard(balance: Double, loading: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s16, vertical = Spacing.s16)
            .clip(RoundedCornerShape(Radius.md))
            .background(BrandRedSurface)
            .padding(Spacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = BrandRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Spacing.s8))
        Text(stringResource(R.string.transfer_available_balance), style = DaviTextStyles.BodyMedium, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandRed, strokeWidth = 2.dp)
        } else {
            Text(formatCOP(balance), style = DaviTextStyles.LabelStrong, color = BrandRed)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s16)
            .clip(RoundedCornerShape(Radius.md))
            .background(White)
            .padding(Spacing.s16),
    ) {
        Text(title, style = DaviTextStyles.LabelStrong, color = TextSecondary)
        Spacer(Modifier.height(Spacing.s12))
        content()
    }
}

@Composable
private fun RecipientPreviewCard(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s12)
            .clip(RoundedCornerShape(Radius.sm))
            .background(Color(0xFFE8F5E9))
            .padding(Spacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(CreditGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = DaviTextStyles.Heading3,
                color = White,
            )
        }
        Spacer(Modifier.width(Spacing.s12))
        Text(name, style = DaviTextStyles.BodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.CheckCircle, null, tint = CreditGreen, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun QuickAmountChips(balance: Double, onSelect: (Double) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s8)) {
        QUICK_AMOUNTS.forEach { amt ->
            val enabled = amt <= balance
            FilterChip(
                selected = false,
                onClick = { if (enabled) onSelect(amt) },
                label = { Text(formatCOPShort(amt), style = DaviTextStyles.BodySmall) },
                enabled = enabled,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SurfaceMedium,
                    labelColor = TextSecondary,
                    disabledContainerColor = SurfaceLight,
                    disabledLabelColor = TextDisabled,
                ),
                border = null,
            )
        }
    }
}

@Composable
private fun ConfirmationSheet(
    recipientName: String,
    phone: String,
    amount: Double,
    description: String,
    loading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s24)
            .padding(bottom = Spacing.s32),
    ) {
        Text(stringResource(R.string.transfer_confirm_title), style = DaviTextStyles.Heading2, color = TextPrimary)
        Spacer(Modifier.height(Spacing.s24))
        SummaryRow(stringResource(R.string.transfer_summary_to), "$recipientName (+57 $phone)")
        SummaryRow(stringResource(R.string.transfer_summary_amount), formatCOP(amount))
        if (description.isNotBlank()) SummaryRow(stringResource(R.string.transfer_summary_concept), description)
        Spacer(Modifier.height(Spacing.s32))
        PrimaryButton(
            text = stringResource(R.string.transfer_button_confirm),
            onClick = onConfirm,
            loading = loading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Spacing.s12))
        TextLinkButton(
            text = stringResource(R.string.transfer_button_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            color = TextSecondary,
        )
        Spacer(Modifier.height(Spacing.s16))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.s8),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = DaviTextStyles.BodyMedium, color = TextSecondary)
        Text(value, style = DaviTextStyles.LabelStrong, color = TextPrimary,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f, fill = false).padding(start = Spacing.s16))
    }
    HorizontalDivider(color = StrokeDefault)
}

@Composable
private fun SuccessOverlay(
    transfer: Transfer,
    recipientName: String,
    onDone: () -> Unit,
) {
    ResultOverlay(
        variant = ResultVariant.Success,
        title = stringResource(R.string.transfer_success_title),
        message = stringResource(R.string.transfer_success_message, formatCOP(transfer.amount), recipientName),
        primaryAction = ResultAction(
            label = stringResource(R.string.transfer_button_done),
            onClick = onDone,
        ),
        secondaryAction = ResultAction(
            label = stringResource(R.string.transfer_button_share),
            onClick = { /* Phase 10 */ },
        ),
        extraContent = { NewBalanceCard(amount = transfer.newBalance) },
    )
}

@Composable
private fun NewBalanceCard(amount: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(SurfaceLight)
            .padding(Spacing.s16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.transfer_new_balance),
            style = DaviTextStyles.BodySmall,
            color = TextSecondary,
        )
        Spacer(Modifier.height(Spacing.s4))
        Text(formatCOP(amount), style = DaviTextStyles.CurrencyDisplay, color = TextPrimary)
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────

private fun formatCOP(amount: Double): String =
    "$ ${NumberFormat.getNumberInstance(Locale("es", "CO")).format(amount.toLong())}"

private fun formatCOPShort(amount: Double): String = when {
    amount >= 1_000_000 -> "$ ${(amount / 1_000_000).toLong()}M"
    amount >= 1_000 -> "$ ${(amount / 1_000).toLong()}K"
    else -> "$ ${amount.toLong()}"
}
