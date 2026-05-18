package dev.code93.daviplata.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.ui.theme.DaviPlataTheme
import dev.code93.daviplata.ui.theme.Radius
import dev.code93.daviplata.ui.theme.SurfaceMedium
import dev.code93.daviplata.ui.theme.rememberIsReduceMotion

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(Radius.sm),
) {
    val reduceMotion = rememberIsReduceMotion()

    if (reduceMotion) {
        Box(modifier = modifier.clip(shape).background(SurfaceMedium))
        return
    }

    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX",
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            SurfaceMedium,
            Color(0xFFE8E8E8),
            SurfaceMedium,
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush),
    )
}

@Preview(showBackground = true)
@Composable
fun SkeletonBlockPreview() {
    DaviPlataTheme {
        Box(Modifier.padding(24.dp)) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
        }
    }
}
