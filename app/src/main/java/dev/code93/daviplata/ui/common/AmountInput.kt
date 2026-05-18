package dev.code93.daviplata.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AmountInput(
    rawValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "0",
) {
    val displayText = formatAmountDisplay(rawValue)
    val borderColor = BrandRed

    BasicTextField(
        value = rawValue,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            onValueChange(digits)
        },
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val y = size.height
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx(),
                )
            },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = DaviTextStyles.CurrencyInput.copy(color = TextPrimary),
        singleLine = true,
        decorationBox = { innerField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = Spacing.s8),
            ) {
                Text(
                    text = "$",
                    style = DaviTextStyles.CurrencyInput,
                    color = BrandRed,
                    modifier = Modifier.padding(end = Spacing.s4),
                )
                Box {
                    if (rawValue.isEmpty()) {
                        Text(
                            text = hint,
                            style = DaviTextStyles.CurrencyInput,
                            color = TextDisabled,
                        )
                    }
                    innerField()
                }
            }
        },
    )

    if (rawValue.isNotEmpty()) {
        Text(
            text = displayText,
            style = DaviTextStyles.BodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = Spacing.s4),
        )
    }
}

@Preview(showBackground = true, name = "Empty Amount Input")
@Composable
fun AmountInputEmptyPreview() {
    DaviPlataTheme {
        Box(Modifier.padding(24.dp)) {
            AmountInput(
                rawValue = "",
                onValueChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Filled Amount Input")
@Composable
fun AmountInputFilledPreview() {
    DaviPlataTheme {
        Box(Modifier.padding(24.dp)) {
            AmountInput(
                rawValue = "50000",
                onValueChange = {}
            )
        }
    }
}

private fun formatAmountDisplay(raw: String): String {
    if (raw.isEmpty()) return ""
    val number = raw.toLongOrNull() ?: return raw
    return NumberFormat.getNumberInstance(Locale("es", "CO")).format(number)
}
