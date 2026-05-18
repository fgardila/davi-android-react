package dev.code93.daviplata.presentation.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.common.PrimaryButton
import dev.code93.daviplata.ui.theme.BrandRed
import dev.code93.daviplata.ui.theme.DaviTextStyles
import dev.code93.daviplata.ui.theme.Spacing
import dev.code93.daviplata.ui.theme.TextPrimary
import dev.code93.daviplata.ui.theme.TextSecondary

@Composable
fun SessionExpiredScreen(onReturnToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.s32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.LockClock,
                contentDescription = null,
                tint = BrandRed,
                modifier = Modifier.size(80.dp),
            )
        }

        Spacer(Modifier.height(Spacing.s32))

        Text(
            text = "Tu sesión ha expirado",
            style = DaviTextStyles.Heading1,
            color = TextPrimary,
        )

        Spacer(Modifier.height(Spacing.s12))

        Text(
            text = "Por seguridad, cerramos tu sesión automáticamente. Ingresa de nuevo para continuar.",
            style = DaviTextStyles.BodyLarge,
            color = TextSecondary,
        )

        Spacer(Modifier.height(Spacing.s40))

        PrimaryButton(
            text = "Volver a ingresar",
            onClick = onReturnToLogin,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.s48))

        Text(text = "DaviPlata", style = DaviTextStyles.LabelStrong, color = BrandRed)
        Text(
            text = "Tu billetera digital",
            style = DaviTextStyles.BodySmall,
            color = TextSecondary,
        )
    }
}