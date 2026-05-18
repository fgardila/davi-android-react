package dev.code93.daviplata.presentation.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.R
import dev.code93.daviplata.domain.usecase.security.SecurityStatus
import dev.code93.daviplata.ui.theme.*
import dev.code93.daviplata.ui.theme.rememberIsReduceMotion

@Composable
internal fun SplashScreen(
    uiState: SplashUiState,
    onBlockedDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val reduceMotion = rememberIsReduceMotion()

    fun enterAnim(delayMs: Int) =
        if (reduceMotion) fadeIn(tween(150, delayMillis = 0))
        else fadeIn(tween(400, delayMillis = delayMs)) + slideInVertically(tween(400, delayMillis = delayMs)) { (it * 0.1f).toInt() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandGradient),
        contentAlignment = Alignment.Center,
    ) {
        DecorativeCircles()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = Spacing.s32),
        ) {
            AnimatedVisibility(visible = visible, enter = enterAnim(0)) {
                Text(
                    text = "D",
                    style = DaviTextStyles.DisplayHero.copy(fontSize = androidx.compose.ui.unit.TextUnit(72f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    color = White,
                )
            }

            Spacer(Modifier.height(Spacing.s16))

            AnimatedVisibility(visible = visible, enter = enterAnim(if (reduceMotion) 0 else 150)) {
                Text(
                    text = stringResource(R.string.login_brand),
                    style = DaviTextStyles.Heading1,
                    color = White,
                )
            }

            Spacer(Modifier.height(Spacing.s8))

            AnimatedVisibility(
                visible = visible,
                enter = if (reduceMotion) fadeIn(tween(150)) else fadeIn(tween(400, delayMillis = 300)),
            ) {
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = DaviTextStyles.BodyLarge,
                    color = White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(Spacing.s48))

            AnimatedVisibility(
                visible = visible,
                enter = if (reduceMotion) fadeIn(tween(150)) else fadeIn(tween(400, delayMillis = 450)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = White,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(Modifier.height(Spacing.s16))
                    Text(
                        text = stringResource(R.string.splash_loading),
                        style = DaviTextStyles.BodySmall,
                        color = White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }

    if (uiState is SplashUiState.Blocked) {
        val message = when (uiState.reason) {
            SecurityStatus.ROOTED -> stringResource(R.string.splash_blocked_root)
            SecurityStatus.EMULATOR_NOT_DEBUG -> stringResource(R.string.splash_blocked_emulator)
            else -> stringResource(R.string.splash_blocked_generic)
        }
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.splash_blocked_title), style = DaviTextStyles.Heading3) },
            text = { Text(message, style = DaviTextStyles.BodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = onBlockedDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = BrandRed),
                ) {
                    Text(stringResource(R.string.splash_close))
                }
            },
        )
    }
}

@Composable
private fun DecorativeCircles() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = 0.06f),
            radius = size.width * 0.7f,
            center = Offset(size.width * 1.1f, -size.height * 0.1f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = size.width * 0.5f,
            center = Offset(-size.width * 0.2f, size.height * 1.05f),
        )
    }
}
