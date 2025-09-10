package org.dsqrwym.shared.ui.components.graphics

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * 绘制 SVG (ImageVector) 并带有逐渐绘制的动画效果。
 *该组件会在首次进入时触发动画，逐步描绘整个 ImageVector 的路径。
 *
 * @param imageVector 要绘制的 SVG 图像向量。
 * @param durationMillis 动画的总持续时间（毫秒）。
 * @param tint 默认的路径颜色（当 VectorPath 没有指定 stroke/fill 或禁用原色时使用）。
 * @param strokeWidth 路径描边的线宽，null代表使用imageVector默认的。
 * @param useOriginalStrokeColor 是否使用原始 SVG 中定义的 stroke 或 fill 颜色来作为线条颜色。
 * @param drawFillAfter 动画结束后是否绘制原始填充色，保持与 SVG 最终外观一致。
 * @param modifier Modifier 用于 Canvas。
 */
@Composable
fun AnimatedImgVector(
    imageVector: ImageVector,
    durationMillis: Int = 2000,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float? = null,
    useOriginalStrokeColor: Boolean = true,
    drawFillAfter: Boolean = true,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    var calculatedScaleFactor by remember { mutableStateOf(1f) }
    var translationX by remember { mutableStateOf(0f) }
    var translationY by remember { mutableStateOf(0f) }
    // 动画进度
    var animationTrigger by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis, easing = LinearEasing),
        label = "stroke_draw_progress"
    )

    val fillProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(
            durationMillis = (durationMillis * 1.3).toInt(),
            delayMillis = durationMillis, // 在描边动画结束后启动
            easing = LinearEasing
        ),
        label = "fill_alpha_progress"
    )

    // 创建并记住 PathMeasure 实例以提高性能
    val pathMeasure = remember { PathMeasure() }

    val semantics =
        if (contentDescription != null) {
            Modifier.semantics {
                this.contentDescription = contentDescription
                this.role = Role.Image
            }
        } else {
            Modifier
        }

    Canvas(
        modifier = modifier.onSizeChanged { intSize ->
            // 在 Canvas 的尺寸确定后，在这里计算缩放和平移
            val canvasWidth = intSize.width.toFloat()
            val canvasHeight = intSize.height.toFloat()

            val vectorIntrinsicWidth = imageVector.viewportWidth
            val vectorIntrinsicHeight = imageVector.viewportHeight

            val scaleToFit = minOf(
                canvasWidth / vectorIntrinsicWidth,
                canvasHeight / vectorIntrinsicHeight
            )
            // 更新状态，这将触发重组和重绘
            calculatedScaleFactor = scaleToFit
        }.scale(calculatedScaleFactor).then(semantics)
    ) {
        val vectorIntrinsicWidth = imageVector.viewportWidth
        val vectorIntrinsicHeight = imageVector.viewportHeight

        val canvasWidth = size.width
        val canvasHeight = size.height

        translationX = (canvasWidth - vectorIntrinsicWidth) / 2
        translationY = (canvasHeight - vectorIntrinsicHeight) / 2
        withTransform({
            translate(translationX, translationY)
        }) {
            drawVectorGroup(
                group = imageVector.root,
                pathMeasure = pathMeasure,
                progress = animationProgress,
                fillAlpha = fillProgress,
                tint = tint,
                strokeWidth = strokeWidth,
                useOriginalStrokeColor = useOriginalStrokeColor,
                drawFillAfter = drawFillAfter
            )
        }
    }

    LaunchedEffect(Unit) {
        animationTrigger = true
    }
}

/**
 * 一个私有的递归辅助函数，用于在 DrawScope 中绘制 VectorGroup。
 */
private fun DrawScope.drawVectorGroup(
    group: VectorGroup,
    pathMeasure: PathMeasure,
    progress: Float,
    fillAlpha: Float,
    tint: Color,
    strokeWidth: Float?,
    useOriginalStrokeColor: Boolean = true,
    drawFillAfter: Boolean = true
) {
    // 遍历组内的所有子节点
    for (node in group) {
        when (node) {
            is VectorPath -> {
                // 如果是路径节点，则绘制它
                drawVectorPath(
                    path = node,
                    pathMeasure = pathMeasure,
                    progress = progress,
                    fillAlpha = fillAlpha,
                    tint = tint,
                    strokeWidth = strokeWidth,
                    useOriginalStrokeColor = useOriginalStrokeColor
                )
            }

            is VectorGroup -> {
                // 如果是另一个组，则递归调用
                drawVectorGroup(
                    group = node,
                    pathMeasure = pathMeasure,
                    progress = progress,
                    fillAlpha = fillAlpha,
                    tint = tint,
                    strokeWidth = strokeWidth,
                    useOriginalStrokeColor = useOriginalStrokeColor,
                    drawFillAfter = drawFillAfter
                )
            }
        }
    }
}


/**
 * 一个私有的辅助函数，用于绘制带有动画效果的单个 VectorPath。
 * 使用路径自身颜色
 **/
private fun DrawScope.drawVectorPath(
    path: VectorPath,
    pathMeasure: PathMeasure,
    progress: Float,
    fillAlpha: Float,
    tint: Color,
    strokeWidth: Float?,
    useOriginalStrokeColor: Boolean = true,
    drawFillAfter: Boolean = true
) {
    // 1. 将 VectorPath 的 pathData 转换成一个 Path 对象
    val composePath = path.pathData.toPath()

    // 2. 使用 PathMeasure 设置路径以获取其属性
    pathMeasure.setPath(composePath, false)
    val length = pathMeasure.length

    // 如果路径长度为0，则无需绘制
    if (length == 0f) return

    // 3. 计算 PathEffect 获取图片颜色
    val colorForStroke = if (useOriginalStrokeColor) {
        when (val brush = path.stroke ?: path.fill) {
            is SolidColor -> brush.value
            else -> tint
        }
    } else {
        tint
    }
    // phase (偏移量) 从 length 变为 0
    val phase = (1f - progress) * length
    // dashArray (虚线模式) 设置为 [画, 不画]，长度都为路径总长
    // 这样通过改变 phase 就能控制可见部分的长度
    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(length, length),
        phase = phase
    )
    val effectiveStrokeWidth = strokeWidth ?: path.strokeLineWidth

    // 4. 使用计算出的 PathEffect 绘制路径
    drawPath(
        path = composePath,
        color = colorForStroke,
        style = Stroke(
            width = effectiveStrokeWidth,
            pathEffect = pathEffect
        )
    )

    // 当动画接近完成时，绘制原始的填充效果
    // 这样做可以确保最终图像与原始 SVG 完全一致
    if (drawFillAfter) {
        val fillBrush = when {
            !useOriginalStrokeColor -> SolidColor(tint)
            path.fill != null -> path.fill
            else -> null
        }
        fillBrush?.let { fillBrush ->
            drawPath(
                path = composePath,
                brush = fillBrush,
                style = Fill,
                alpha = fillAlpha // <- 控制填充透明度
            )
        }
    }
}