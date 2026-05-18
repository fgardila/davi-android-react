package dev.code93.daviplata.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// Niveles de sombra mapeados a los tokens de design.md §5.
enum class ShadowLevel { None, Low, Medium, High, Brand }

fun Modifier.daviShadow(
    level: ShadowLevel,
    shape: Shape = RoundedCornerShape(0.dp),
): Modifier = when (level) {
    ShadowLevel.None -> this
    // shadow1: 0px 1px 3px rgba(0,0,0,0.08)
    ShadowLevel.Low -> shadow(
        elevation = 2.dp, shape = shape,
        ambientColor = Color(0x14000000), spotColor = Color(0x14000000),
    )
    // shadow2: 0px 4px 12px rgba(0,0,0,0.12)
    ShadowLevel.Medium -> shadow(
        elevation = 6.dp, shape = shape,
        ambientColor = Color(0x1F000000), spotColor = Color(0x1F000000),
    )
    // shadow3: 0px 8px 24px rgba(0,0,0,0.16)
    ShadowLevel.High -> shadow(
        elevation = 12.dp, shape = shape,
        ambientColor = Color(0x29000000), spotColor = Color(0x29000000),
    )
    // shadowBrand: 0px 4px 16px rgba(218,0,38,0.30)
    ShadowLevel.Brand -> shadow(
        elevation = 8.dp, shape = shape,
        ambientColor = Color(0x4DDA0026), spotColor = Color(0x4DDA0026),
    )
}
