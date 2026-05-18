package dev.code93.daviplata.ui.common.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.common.PrimaryButton
import dev.code93.daviplata.ui.common.TextLinkButton
import dev.code93.daviplata.ui.theme.AppError
import dev.code93.daviplata.ui.theme.DaviPlataTheme
import dev.code93.daviplata.ui.theme.DaviTextStyles
import dev.code93.daviplata.ui.theme.Radius
import dev.code93.daviplata.ui.theme.Spacing
import dev.code93.daviplata.ui.theme.Success
import dev.code93.daviplata.ui.theme.SurfaceLight
import dev.code93.daviplata.ui.theme.TextPrimary
import dev.code93.daviplata.ui.theme.TextSecondary
import dev.code93.daviplata.ui.theme.White
import dev.code93.daviplata.ui.theme.rememberIsReduceMotion

enum class ResultVariant { Success, Error }

data class ResultAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * Overlay fullscreen genérico para mostrar el resultado final de un flujo (éxito o error).
 * - El call-site provee todos los textos y acciones; nada está hardcodeado al feature.
 * - `extraContent` es un slot opcional para inyectar contenido específico (ej. tarjeta de saldo).
 */
@Composable
fun ResultOverlay(
    variant: ResultVariant,
    title: String,
    message: String,
    primaryAction: ResultAction,
    modifier: Modifier = Modifier,
    secondaryAction: ResultAction? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val accent = when (variant) {
        ResultVariant.Success -> Success
        ResultVariant.Error -> AppError
    }
    val icon: ImageVector = when (variant) {
        ResultVariant.Success -> Icons.Outlined.CheckCircle
        ResultVariant.Error -> Icons.Outlined.Cancel
    }

    val reduceMotion = rememberIsReduceMotion()
    var animTarget by remember { mutableFloatStateOf(if (reduceMotion) 1f else 0f) }
    val progress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = if (reduceMotion) snap() else tween(600, easing = FastOutSlowInEasing),
        label = "resultArc",
    )
    LaunchedEffect(Unit) { if (!reduceMotion) animTarget = 1f }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = Spacing.s32),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .drawWithContent {
                        drawContent()
                        drawArc(
                            brush = Brush.sweepGradient(listOf(accent, accent)),
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(visible = progress >= 1f, enter = scaleIn() + fadeIn()) {
                        Icon(icon, null, tint = accent, modifier = Modifier.size(64.dp))
                    }
                }
            }

            Spacer(Modifier.height(Spacing.s32))
            Text(
                title,
                style = DaviTextStyles.Heading1,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.s12))
            Text(
                message,
                style = DaviTextStyles.BodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            if (extraContent != null) {
                Spacer(Modifier.height(Spacing.s24))
                extraContent()
            }

            Spacer(Modifier.height(Spacing.s40))
            PrimaryButton(
                text = primaryAction.label,
                onClick = primaryAction.onClick,
                modifier = Modifier.fillMaxWidth(),
            )
            if (secondaryAction != null) {
                Spacer(Modifier.height(Spacing.s12))
                TextLinkButton(
                    text = secondaryAction.label,
                    onClick = secondaryAction.onClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ─── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Success", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ResultOverlaySuccessPreview() {
    DaviPlataTheme {
        ResultOverlay(
            variant = ResultVariant.Success,
            title = "¡Transferencia exitosa!",
            message = "Enviaste $ 50.000 a Juan Pérez",
            primaryAction = ResultAction("Listo") {},
            secondaryAction = ResultAction("Compartir comprobante") {},
            extraContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(SurfaceLight)
                        .padding(Spacing.s16),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Nuevo saldo", style = DaviTextStyles.BodySmall, color = TextSecondary)
                    Spacer(Modifier.height(Spacing.s4))
                    Text("$ 1.200.345", style = DaviTextStyles.CurrencyDisplay, color = TextPrimary)
                }
            },
        )
    }
}

@Preview(name = "Error", showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun ResultOverlayErrorPreview() {
    DaviPlataTheme {
        ResultOverlay(
            variant = ResultVariant.Error,
            title = "Algo salió mal",
            message = "No pudimos completar tu solicitud. Verifica tu conexión e intenta de nuevo.",
            primaryAction = ResultAction("Reintentar") {},
            secondaryAction = ResultAction("Volver al inicio") {},
        )
    }
}
