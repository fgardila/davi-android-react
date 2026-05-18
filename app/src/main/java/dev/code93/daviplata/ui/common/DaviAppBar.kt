package dev.code93.daviplata.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.code93.daviplata.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaviAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    variant: AppBarVariant = AppBarVariant.White,
) {
    val containerColor = when (variant) {
        AppBarVariant.White -> White
        AppBarVariant.Red -> BrandRed
    }
    val contentColor = when (variant) {
        AppBarVariant.White -> TextPrimary
        AppBarVariant.Red -> White
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = DaviTextStyles.Heading3,
                color = contentColor,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = contentColor,
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
        ),
    )
}

enum class AppBarVariant { White, Red }

@Preview(showBackground = true, name = "White Variant")
@Composable
fun DaviAppBarWhitePreview() {
    DaviPlataTheme {
        DaviAppBar(
            title = "Título Blanco",
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Red Variant")
@Composable
fun DaviAppBarRedPreview() {
    DaviPlataTheme {
        DaviAppBar(
            title = "Título Rojo",
            onBack = {},
            variant = AppBarVariant.Red
        )
    }
}
