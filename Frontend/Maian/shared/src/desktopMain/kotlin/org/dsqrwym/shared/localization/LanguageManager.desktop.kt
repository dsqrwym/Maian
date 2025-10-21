package org.dsqrwym.shared.localization

import org.dsqrwym.shared.util.platform.MaiAnPlatformType
import java.util.Locale.getDefault

actual fun getLocaleLanguage(): String {
    return getDefault().toLanguageTag()
}

fun getAppDisplayName(maiAnPlatformType: MaiAnPlatformType = MaiAnPlatformType.STANDARD): String {
    return when (getLocaleLanguage()) {
        "zh-CN", "zh-hant" -> {
            when (maiAnPlatformType) {
                MaiAnPlatformType.STANDARD -> "卖安"
                MaiAnPlatformType.ENTERPRISE -> "卖安 - 企业版"
                MaiAnPlatformType.ADMIN -> "卖安 - 管理版"
            }
        }

        else -> {
            when (maiAnPlatformType) {
                MaiAnPlatformType.STANDARD -> "Maian"
                MaiAnPlatformType.ENTERPRISE -> "Maian - Enterprise"
                MaiAnPlatformType.ADMIN -> "Maian - Admin"
            }
        }
    }
}