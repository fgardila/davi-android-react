package dev.code93.daviplata.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

// Gradiente de marca: 135°, BrandRed → #8B0017. Usado en BalanceCardHero y Splash.
val brandGradient: Brush
    get() = Brush.linearGradient(
        colors = listOf(BrandRed, GradientBrandEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
