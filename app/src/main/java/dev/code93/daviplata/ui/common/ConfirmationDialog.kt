package dev.code93.daviplata.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.theme.*

@Composable
fun ConfirmationDialog(
    title: String,
    body: String,
    confirmLabel: String = "Confirmar",
    cancelLabel: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = DaviTextStyles.Heading3) },
        text = { Text(body, style = DaviTextStyles.BodyMedium, color = TextSecondary) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = BrandRed),
            ) {
                Text(confirmLabel, style = DaviTextStyles.ButtonLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
            ) {
                Text(cancelLabel, style = DaviTextStyles.ButtonLabel)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = White,
    )
}

@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview() {
    DaviPlataTheme {
        ConfirmationDialog(
            title = "¿Estás seguro?",
            body = "Esta acción no se puede deshacer.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
