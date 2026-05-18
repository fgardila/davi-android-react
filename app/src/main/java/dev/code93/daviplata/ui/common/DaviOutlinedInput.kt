package dev.code93.daviplata.ui.common

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.theme.*

@Composable
fun DaviOutlinedInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    error: String? = null,
    helper: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLength: Int = Int.MAX_VALUE,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }
    val hasError = !error.isNullOrEmpty()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.s64),
            label = { Text(label, style = DaviTextStyles.BodyLarge) },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null, tint = TextSecondary) }
            },
            trailingIcon = when {
                isPassword -> {
                    {
                        val icon = if (passwordVisible)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = if (passwordVisible) "Ocultar" else "Mostrar")
                        }
                    }
                }
                trailingIcon != null -> {
                    {
                        IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                            Icon(trailingIcon, contentDescription = null)
                        }
                    }
                }
                maxLength < Int.MAX_VALUE -> {
                    { Text("${value.length}/$maxLength", style = DaviTextStyles.BodySmall, color = TextSecondary) }
                }
                else -> null
            },
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            enabled = enabled,
            isError = hasError,
            shape = RoundedCornerShape(Radius.sm),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandRed,
                focusedLabelColor = BrandRed,
                unfocusedBorderColor = StrokeDefault,
                unfocusedLabelColor = TextSecondary,
                errorBorderColor = AppError,
                errorLabelColor = AppError,
                disabledBorderColor = StrokeDefault,
                disabledContainerColor = SurfaceLight,
                cursorColor = BrandRed,
            ),
            textStyle = DaviTextStyles.BodyLarge,
        )

        AnimatedVisibility(
            visible = hasError,
            enter = slideInVertically { -it / 2 } + fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = AppError,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = error ?: "",
                    style = DaviTextStyles.BodySmall,
                    color = AppError,
                )
            }
        }

        if (!helper.isNullOrEmpty() && !hasError) {
            Text(
                text = helper,
                style = DaviTextStyles.BodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "Default Input")
@Composable
fun DaviOutlinedInputDefaultPreview() {
    DaviPlataTheme {
        Box(Modifier.padding(16.dp)) {
            DaviOutlinedInput(
                value = "",
                onValueChange = {},
                label = "Nombre de usuario",
                leadingIcon = Icons.Outlined.Person
            )
        }
    }
}

@Preview(showBackground = true, name = "Error Input")
@Composable
fun DaviOutlinedInputErrorPreview() {
    DaviPlataTheme {
        Box(Modifier.padding(16.dp)) {
            DaviOutlinedInput(
                value = "usuario@invalid",
                onValueChange = {},
                label = "Correo electrónico",
                error = "El formato del correo es inválido"
            )
        }
    }
}

@Preview(showBackground = true, name = "Password Input")
@Composable
fun DaviOutlinedInputPasswordPreview() {
    DaviPlataTheme {
        Box(Modifier.padding(16.dp)) {
            DaviOutlinedInput(
                value = "123456",
                onValueChange = {},
                label = "Contraseña",
                isPassword = true
            )
        }
    }
}
