package dev.code93.daviplata.presentation.auth.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.R
import dev.code93.daviplata.domain.model.Session
import dev.code93.daviplata.domain.validation.Validators
import dev.code93.daviplata.ui.common.AlertBanner
import dev.code93.daviplata.ui.common.AlertType
import dev.code93.daviplata.ui.common.DaviOutlinedInput
import dev.code93.daviplata.ui.common.PrimaryButton
import dev.code93.daviplata.ui.common.TextLinkButton
import dev.code93.daviplata.ui.theme.BrandRed
import dev.code93.daviplata.ui.theme.DaviTextStyles
import dev.code93.daviplata.ui.theme.Radius
import dev.code93.daviplata.ui.theme.Spacing
import dev.code93.daviplata.ui.theme.StrokeDefault
import dev.code93.daviplata.ui.theme.TextSecondary
import dev.code93.daviplata.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLogin: (phone: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (session: Session) -> Unit,
    onResetState: () -> Unit,
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }

    var shakeOffset by remember { mutableStateOf(0f) }
    val animatedShake by animateFloatAsState(
        targetValue = shakeOffset,
        animationSpec = spring(stiffness = 1500f, dampingRatio = 0.3f),
        label = "shake",
        finishedListener = { shakeOffset = 0f },
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> onLoginSuccess(uiState.session)
            is LoginUiState.Error -> {
                shakeOffset = 8f
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BrandRed)) {
        DecorativeHeaderCircles()

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.login_brand),
                        style = DaviTextStyles.DisplayHero,
                        color = White,
                    )
                    Spacer(Modifier.height(Spacing.s8))
                    Text(
                        text = stringResource(R.string.login_title),
                        style = DaviTextStyles.BodyLarge,
                        color = White.copy(alpha = 0.85f),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl))
                    .background(White),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.s24, vertical = Spacing.s32)
                        .graphicsLayer { translationX = animatedShake },
                ) {
                    if (uiState is LoginUiState.Error) {
                        val attemptText = if (uiState.attemptCount in 1..2)
                            stringResource(R.string.login_error_attempt, uiState.attemptCount) else ""
                        AlertBanner(
                            type = AlertType.Error,
                            text = uiState.message + attemptText,
                        )
                        Spacer(Modifier.height(Spacing.s16))
                    }
                    if (uiState is LoginUiState.Locked) {
                        LockCountdown(
                            retryAfterSeconds = uiState.retryAfterSeconds,
                            onExpired = onResetState,
                        )
                        Spacer(Modifier.height(Spacing.s16))
                    }

                    DaviOutlinedInput(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it },
                        label = stringResource(R.string.login_phone),
                        leadingIcon = Icons.Outlined.PhoneAndroid,
                        keyboardType = KeyboardType.Phone,
                        maxLength = 10,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.s16))
                    DaviOutlinedInput(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.login_password),
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.s8))
                    TextLinkButton(
                        text = stringResource(R.string.login_forgot_password),
                        onClick = { showForgotDialog = true },
                        modifier = Modifier.align(Alignment.End),
                    )

                    Spacer(Modifier.height(Spacing.s24))
                    PrimaryButton(
                        text = stringResource(R.string.login_button),
                        onClick = { onLogin(phone, password) },
                        loading = uiState is LoginUiState.Loading,
                        enabled = Validators.phone(phone) == null
                            && Validators.password(password) == null
                            && uiState !is LoginUiState.Locked,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.s24))
                    DividerWithText(stringResource(R.string.login_divider_or))
                    Spacer(Modifier.height(Spacing.s16))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.login_footer_no_account),
                            style = DaviTextStyles.BodyMedium,
                            color = TextSecondary,
                        )
                        TextLinkButton(
                            text = stringResource(R.string.login_footer_register),
                            onClick = onNavigateToRegister,
                        )
                    }
                }
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text(stringResource(R.string.login_forgot_dialog_title), style = DaviTextStyles.Heading3) },
            text = {
                Text(
                    stringResource(R.string.login_forgot_dialog_body),
                    style = DaviTextStyles.BodyMedium,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showForgotDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrandRed),
                ) { Text(stringResource(R.string.login_forgot_dialog_button)) }
            },
        )
    }
}

@Composable
private fun LockCountdown(retryAfterSeconds: Int, onExpired: () -> Unit) {
    var secondsLeft by remember(retryAfterSeconds) { mutableIntStateOf(retryAfterSeconds) }
    LaunchedEffect(retryAfterSeconds) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        onExpired()
    }
    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    AlertBanner(
        type = AlertType.Warning,
        text = stringResource(R.string.login_lock_message, minutes, secs),
    )
}

@Composable
private fun DividerWithText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = StrokeDefault)
        Text(
            text = "  $text  ",
            style = DaviTextStyles.BodySmall,
            color = TextSecondary,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = StrokeDefault)
    }
}

@Composable
private fun DecorativeHeaderCircles() {
    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.06f),
                radius = size.width * 0.6f,
                center = androidx.compose.ui.geometry.Offset(size.width * 1.05f, -size.height * 0.3f),
            )
        }
    }
}
