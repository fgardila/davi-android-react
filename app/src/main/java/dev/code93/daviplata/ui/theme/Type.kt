package dev.code93.daviplata.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Nunito Sans — humanista, legible, cálida. Fuentes embebidas opcionales:
// colocar nunito_sans_regular/medium/semibold/bold.ttf en res/font/ y reemplazar
// FontFamily.SansSerif por FontFamily(Font(R.font.xxx), ...).
val NunitoSans: FontFamily = FontFamily.SansSerif

// Roboto Mono — monoespaciado para valores monetarios. FontFamily.Monospace
// ya mapea a Roboto Mono en Android (correcto por defecto).
val RobotoMono: FontFamily = FontFamily.Monospace

object DaviTextStyles {
    val DisplayHero = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp
    )
    val Heading1 = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp
    )
    val Heading2 = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 28.sp
    )
    val Heading3 = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 26.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 22.sp
    )
    val BodySmall = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 18.sp
    )
    val LabelStrong = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp
    )
    val ButtonLabel = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Bold,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp
    )
    val Overline = TextStyle(
        fontFamily = NunitoSans, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.2.sp
    )
    val CurrencyDisplay = TextStyle(
        fontFamily = RobotoMono, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp
    )
    val CurrencyBody = TextStyle(
        fontFamily = RobotoMono, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp
    )
    val CurrencyInput = TextStyle(
        fontFamily = RobotoMono, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 48.sp
    )
}

val DaviTypography = Typography(
    displayLarge = DaviTextStyles.DisplayHero,
    headlineLarge = DaviTextStyles.Heading1,
    headlineMedium = DaviTextStyles.Heading2,
    headlineSmall = DaviTextStyles.Heading3,
    bodyLarge = DaviTextStyles.BodyLarge,
    bodyMedium = DaviTextStyles.BodyMedium,
    bodySmall = DaviTextStyles.BodySmall,
    labelMedium = DaviTextStyles.LabelStrong,
    labelSmall = DaviTextStyles.Overline,
)
