package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.drawable.GoogleLogo
/*
@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val buttonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = Color.White,
        contentColor = Color(0xFF1F1F1F)
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                BorderStroke(1.dp, Color(0xFF747775)),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = buttonColors,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Google 图标
            Image(
                imageVector = GoogleLogo,
                contentDescription = "Google 图标",
                modifier = Modifier
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // 文字
            Text(
                "Sign in with Google",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
 */
@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF131314) else Color.White
    val contentColor = if (isDarkTheme) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)
    val borderColor = if (isDarkTheme) Color(0xFF8E918F) else Color(0xFF747775)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp),
        shadowElevation = if (isDarkTheme) 1.dp else 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = GoogleLogo,
                contentDescription = "Google logo",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Sign in with Google",
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
