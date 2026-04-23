package com.maximaaax.androidutils.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maximaaax.androidutils.presentation.theme.AndroidUtilsPalette

private val defaultStroke: Dp = 1.8.dp

@Composable
fun DashedFrame(
    modifier: Modifier = Modifier,
    color: Color,
    cornerRadius: Dp = 16.dp,
    dash: Dp = 6.dp,
    gap: Dp = 4.dp,
    strokeWidth: Dp = 1.2.dp,
) {
    val density = LocalDensity.current
    val dashPx = with(density) { dash.toPx() }
    val gapPx = with(density) { gap.toPx() }
    val wPx = with(density) { strokeWidth.toPx() }
    val rPx = with(density) { cornerRadius.toPx() }
    Canvas(
        modifier = modifier,
    ) {
        val path = Path()
        val w = size.width
        val h = size.height
        path.addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = wPx * 0.5f,
                top = wPx * 0.5f,
                right = w - wPx * 0.5f,
                bottom = h - wPx * 0.5f,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(rPx, rPx),
            ),
        )
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = wPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(dashPx, gapPx),
                    0f,
                ),
            ),
        )
    }
}

@Composable
fun HomeIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
    strokeWidth: Dp = defaultStroke,
) {
    val sw = with(LocalDensity.current) { strokeWidth.toPx() }
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Toit
        val roof = Path().apply {
            val mid = s * 0.5f
            moveTo(s * 0.1f, s * 0.5f)
            lineTo(mid, s * 0.22f)
            lineTo(s * 0.9f, s * 0.5f)
        }
        // Murs
        val wall = Path().apply {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    s * 0.22f, s * 0.5f, s * 0.78f, s * 0.9f,
                ),
            )
        }
        // Porte
        val door = Path().apply {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    s * 0.4f, s * 0.6f, s * 0.6f, s * 0.9f,
                ),
            )
        }
        drawPath(roof, color, style = style)
        drawPath(wall, color, style = style)
        drawPath(door, color, style = Stroke(sw * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun VideoIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
    strokeWidth: Dp = defaultStroke,
) {
    val sw = with(LocalDensity.current) { strokeWidth.toPx() }
    val style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val body = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = s * 0.1f,
                    top = s * 0.28f,
                    right = s * 0.62f,
                    bottom = s * 0.72f,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.1f, s * 0.1f),
                ),
            )
        }
        val lens = Path().apply {
            val ax = s * 0.62f
            moveTo(ax, s * 0.36f)
            lineTo(s * 0.9f, s * 0.2f)
            lineTo(s * 0.9f, s * 0.8f)
            lineTo(ax, s * 0.64f)
            close()
        }
        drawPath(body, color, style = style)
        drawPath(lens, color, style = style)
    }
}

@Composable
fun ImageIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
    strokeWidth: Dp = defaultStroke,
) {
    val sw = with(LocalDensity.current) { strokeWidth.toPx() }
    val style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val frame = Path().apply {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    s * 0.08f, s * 0.15f, s * 0.92f, s * 0.9f,
                ),
            )
        }
        drawPath(frame, color, style = style)
        drawCircle(
            color = color,
            center = Offset(s * 0.3f, s * 0.3f),
            radius = s * 0.05f,
        )
        val mtn = Path().apply {
            moveTo(s * 0.1f, s * 0.7f)
            lineTo(s * 0.35f, s * 0.48f)
            lineTo(s * 0.5f, s * 0.6f)
            lineTo(s * 0.68f, s * 0.42f)
            lineTo(s * 0.9f, s * 0.7f)
        }
        drawPath(mtn, color, style = Stroke(sw * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun SparkleIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val w = s * 0.18f
        val h = s * 0.42f
        val p1 = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = cx - w,
                    top = cy - h,
                    right = cx + w,
                    bottom = cy + h,
                ),
            )
        }
        val p2 = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = cx - h,
                    top = cy - w,
                    right = cx + h,
                    bottom = cy + w,
                ),
            )
        }
        drawPath(p1, color, style = Fill)
        drawPath(p2, color, style = Fill)
    }
}

@Composable
fun InfoIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
    strokeWidth: Dp = defaultStroke,
) {
    val sw = with(LocalDensity.current) { strokeWidth.toPx() }
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val c = this.size
        val cx = c.width / 2f
        val cy = c.height / 2f
        val pad = s * 0.38f
        drawCircle(
            color = color,
            style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round),
            center = Offset(cx, cy),
            radius = s * 0.38f,
        )
        drawCircle(
            color = color,
            center = Offset(cx, cy * 0.7f + pad * 0.15f),
            radius = s * 0.055f,
        )
        drawLine(
            color = color,
            start = Offset(cx, cy * 0.3f + pad * 0.1f),
            end = Offset(cx, cy * 0.5f + pad * 0.1f),
            strokeWidth = sw * 0.8f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun ArrowRightIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
    strokeWidth: Dp = defaultStroke,
) {
    val sw = with(LocalDensity.current) { strokeWidth.toPx() }
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.2f, s * 0.5f)
            lineTo(s * 0.75f, s * 0.5f)
            moveTo(s * 0.6f, s * 0.3f)
            lineTo(s * 0.78f, s * 0.5f)
            lineTo(s * 0.6f, s * 0.7f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun PlayIcon(
    color: Color = AndroidUtilsPalette.OnBackground,
    size: Dp = 18.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.3f, s * 0.2f)
            lineTo(s * 0.78f, s * 0.5f)
            lineTo(s * 0.3f, s * 0.8f)
            close()
        }
        drawPath(path, color, style = Fill)
    }
}

@Composable
fun PlayIcon(
    fillBrush: Brush,
    size: Dp = 18.dp,
) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.3f, s * 0.2f)
            lineTo(s * 0.78f, s * 0.5f)
            lineTo(s * 0.3f, s * 0.8f)
            close()
        }
        drawPath(path, brush = fillBrush, style = Fill)
    }
}
