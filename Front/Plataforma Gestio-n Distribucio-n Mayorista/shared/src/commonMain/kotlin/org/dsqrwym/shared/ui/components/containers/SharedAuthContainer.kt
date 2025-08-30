package org.dsqrwym.shared.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedImages

@Composable
fun SharedAuthContainer(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val notMobile = maxWidth > 600.dp
        val blurRadius = if (notMobile) 12.dp else 0.dp
        BackgroundImage(SharedImages.background(), blurRadius) {
            // 居中内容，宽度限制仅非手机端
            val transparency = 0.85f
            val contentModifier = if (notMobile) {
                Modifier
                    .widthIn(max = 600.dp)
                    .heightIn(min = 658.dp, max = 820.dp)
                    .fillMaxHeight(0.8f)
                    .graphicsLayer { // 加alpha保证不会和shadow一样出现边缘更透的情况
                        shadowElevation = 20.dp.toPx()
                        shape = RoundedCornerShape(18.dp)
                        clip = true
                        alpha = transparency // 保证不会边缘更透的情况
                    }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = transparency))
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            Box(modifier = contentModifier) {
                content()
            }
        }
    }
}