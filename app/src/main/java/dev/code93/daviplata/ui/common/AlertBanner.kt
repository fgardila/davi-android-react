package dev.code93.daviplata.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.theme.*

enum class AlertType { Success, Error, Warning, Info }

@Composable
fun AlertBanner(
    type: AlertType,
    text: String,
    modifier: Modifier = Modifier,
) {
    val (bg, iconTint, icon) = when (type) {
        AlertType.Success -> Triple(SuccessLight, Success, Icons.Outlined.CheckCircle as ImageVector)
        AlertType.Error -> Triple(ErrorLight, AppError, Icons.Outlined.ErrorOutline as ImageVector)
        AlertType.Warning -> Triple(WarningLight, Warning, Icons.Outlined.Warning as ImageVector)
        AlertType.Info -> Triple(InfoLight, Info, Icons.Outlined.Info as ImageVector)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(bg)
            .padding(horizontal = Spacing.s12, vertical = Spacing.s12),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp).padding(top = 1.dp),
        )
        Spacer(Modifier.width(Spacing.s8))
        Text(text = text, style = DaviTextStyles.BodyMedium, color = iconTint)
    }
}

@Preview(showBackground = true, name = "Success Alert")
@Composable
fun SuccessAlertPreview() {
    DaviPlataTheme {
        AlertBanner(
            type = AlertType.Success,
            text = "Operación realizada con éxito",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Error Alert")
@Composable
fun ErrorAlertPreview() {
    DaviPlataTheme {
        AlertBanner(
            type = AlertType.Error,
            text = "Ha ocurrido un error inesperado",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Warning Alert")
@Composable
fun WarningAlertPreview() {
    DaviPlataTheme {
        AlertBanner(
            type = AlertType.Warning,
            text = "Su sesión expirará pronto",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Info Alert")
@Composable
fun InfoAlertPreview() {
    DaviPlataTheme {
        AlertBanner(
            type = AlertType.Info,
            text = "Recuerde actualizar sus datos",
            modifier = Modifier.padding(16.dp)
        )
    }
}
