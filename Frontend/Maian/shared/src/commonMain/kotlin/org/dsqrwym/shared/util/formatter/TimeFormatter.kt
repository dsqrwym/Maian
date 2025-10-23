
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import maian.shared.generated.resources.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 格式化验证码过期时间，返回多语言友好的文本
 * - 剩余 < 60 秒 → 显示 "XX 秒内"
 * - 剩余 < 1 小时 → 显示 "XX 分钟 YY 秒内"
 * - 剩余 >= 1 小时 → 显示完整本地化时间（如 "2025-09-14 20:15"）
 *
 * @param expiresAt 验证码到期的 [kotlin.time.Instant]
 * @param formattedAbsolute 可选，传入平台已格式化的绝对时间字符串
 * @return 本地化文本
 */
@OptIn(ExperimentalTime::class)
suspend fun formatExpireDurationFromSeconds(
    expiresAt: kotlin.time.Instant,
    formattedAbsolute: String? = null,
): String {
    val seconds = (expiresAt.epochSeconds - Clock.System.now().epochSeconds).coerceAtLeast(0L)

    val result = when {
        seconds < 60L -> {
            val s = getPluralString(
                SharedRes.plurals.expire_seconds, // 生成的 plurals 访问器
                seconds.toInt(),            // 选择复数形式
                seconds                     // 格式化占位符 %d
            )

            getString(SharedRes.string.expire_prefix, s)
        }

        seconds < 3600L -> {
            val minutes = (seconds / 60L).toInt()
            val secs = (seconds % 60L).toInt()

            val minutesText = getPluralString(
                SharedRes.plurals.expire_minutes,
                minutes,
                minutes
            )

            if (secs > 0) {
                val secondsText = getPluralString(
                    SharedRes.plurals.expire_seconds,
                    secs,
                    secs
                )
                val combined = getString(
                    SharedRes.string.expire_minutes_seconds,
                    minutesText,
                    secondsText
                )

                getString(SharedRes.string.expire_prefix, combined)
            } else {

                getString(SharedRes.string.expire_prefix, minutesText)
            }
        }

        else -> {
            // 优先使用平台端传入的 formattedAbsolute（以确保 locale/format 都正确）
            if (!formattedAbsolute.isNullOrBlank()) {
                getString(SharedRes.string.expire_absolute, formattedAbsolute)
            } else {
                // 回退到“xx 分钟内”的复数表示
                val mins = (seconds / 60L).toInt()
                val minutesText = getPluralString(SharedRes.plurals.expire_minutes, mins, mins)
                getString(SharedRes.string.expire_prefix, minutesText)
            }
        }
    }
    return result
}