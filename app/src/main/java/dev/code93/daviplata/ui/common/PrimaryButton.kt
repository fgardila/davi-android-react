package dev.code93.daviplata.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.theme.*

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.7f),
        label = "btnScale",
    )
    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .height(Spacing.s56)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .daviShadow(
                if (enabled && !loading) ShadowLevel.Brand else ShadowLevel.None,
                RoundedCornerShape(Radius.full),
            ),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(Radius.full),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandRed,
            contentColor = White,
            disabledContainerColor = TextDisabled,
            disabledContentColor = White,
        ),
        interactionSource = interactionSource,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = text, style = DaviTextStyles.ButtonLabel)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(Spacing.s56),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.full),
        border = BorderStroke(1.5.dp, if (enabled) BrandRed else TextDisabled),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BrandRed,
            disabledContentColor = TextDisabled,
        ),
    ) {
        Text(text = text, style = DaviTextStyles.ButtonLabel)
    }
}

@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = BrandRed,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
    ) {
        Text(text = text, style = DaviTextStyles.ButtonLabel)
    }
}

@Preview(showBackground = true, name = "Primary Button")
@Composable
fun PrimaryButtonPreview() {
    DaviPlataTheme {
        PrimaryButton(
            text = "Continuar",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Loading Button")
@Composable
fun PrimaryButtonLoadingPreview() {
    DaviPlataTheme {
        PrimaryButton(
            text = "Continuar",
            onClick = {},
            loading = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Secondary Button")
@Composable
fun SecondaryButtonPreview() {
    DaviPlataTheme {
        SecondaryButton(
            text = "Cancelar",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Text Link Button")
@Composable
fun TextLinkButtonPreview() {
    DaviPlataTheme {
        TextLinkButton(
            text = "¿Olvidaste tu contraseña?",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
