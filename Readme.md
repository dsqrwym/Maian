# Kotlin Multiplatform Frontend

本项目基于 Kotlin Multiplatform 和 Compose Multiplatform 构建，目标支持 Android, ios, 网页 与桌面前端平台，具备现代化响应式 UI 架构、模块化设计。

## 技术栈

- **Kotlin Multiplatform** (`org.jetbrains.kotlin.multiplatform`)
- **Compose Multiplatform** (`org.jetbrains.compose`)
- **Jetpack Compose for Android**（含 ViewModel、Activity Compose）
- **Koin** 依赖注入（适用于 Compose 和 Core）
- **KSP** 注解处理
- **Coroutines / Serialization / DateTime**：跨平台数据处理支持
- **WebView 多平台组件**
- **Haze 视觉效果库**
- **JMail 邮箱验证工具**
- **多平台设置存储支持**
- **Compose Icons & 小米 MiSans 字体**

---

## 依赖与仓库

以下为本项目所使用的核心依赖及其来源仓库与所有者信息（用于验证构建信任链）：

| 库 | 模块 | 仓库所有者                                                                                              |
|----|------|----------------------------------------------------------------------------------------------------|
| Kotlin | `org.jetbrains.kotlin:kotlin-test` | [JetBrains](https://kotlinlang.org/api/core/kotlin-test/)                                                          |
| Compose Multiplatform | `org.jetbrains.compose.*` | [JetBrains](https://github.com/JetBrains/compose-jb)                                               |
| Activity Compose | `androidx.activity:activity-compose` | [Google / AndroidX](https://developer.android.com/jetpack/androidx/releases/activity)              |
| Lifecycle Compose | `org.jetbrains.androidx.lifecycle:*` | [JetBrains / AndroidX](https://github.com/JetBrains/androidx)                                      |
| Navigation Compose | `org.jetbrains.androidx.navigation:navigation-compose` | [JetBrains / KMP](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation.html) |
| Security Crypto | `androidx.security:security-crypto` | [Google / AndroidX](https://developer.android.com/jetpack/androidx/releases/security)              |
| Kotlin Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-swing` | [JetBrains](https://github.com/Kotlin/kotlinx.coroutines)                                          |
| Kotlinx DateTime | `org.jetbrains.kotlinx:kotlinx-datetime` | [JetBrains](https://github.com/Kotlin/kotlinx-datetime)                                            |
| Kotlinx Serialization | `org.jetbrains.kotlinx:kotlinx-serialization-json` | [JetBrains](https://github.com/Kotlin/kotlinx.serialization)                                       |
| Compose WebView Multiplatform | `io.github.kevinnzou:compose-webview-multiplatform` | [kevinnzou](https://github.com/KevinnZou/compose-webview-multiplatform)                            |
| Koin DI | `io.insert-koin:*` | [InsertKoinIO](https://github.com/InsertKoinIO/koin)                                               |
| Haze | `dev.chrisbanes.haze:haze` | [chrisbanes](https://github.com/chrisbanes/haze)                                                   |
| JMail | `com.sanctionco.jmail:jmail` | [RohanNagar](https://github.com/RohanNagar/jmail)                                                  |
| Multiplatform Settings | `com.russhwolf:multiplatform-settings` | [russhwolf](https://github.com/russhwolf/multiplatform-settings)                                   |
| KSP Symbol Processing API | `com.google.devtools.ksp:symbol-processing-api` | [Google](https://github.com/google/ksp)                                                            |

---

## 图标与字体资源

- **Compose Icons 图标**：使用自 [ComposeIcons](https://composeicons.com) 提供的图标资源（SVG 向量图标），兼容 Jetpack Compose。
- **小米 MiSans 字体**：来自 [HyperOS 官方字体站点](https://hyperos.mi.com/font)，遵守其授权协议，用于 UI 字体美化。

---

## 构建信息

| 配置项 | 值 |
|--------|----|
| Kotlin | 2.1.21 |
| Compose Multiplatform | 1.8.2 |
| Android Compile SDK | 35 |
| Android Min SDK | 24 |
| Android Target SDK | 35 |
| Compose 热重载 | 1.0.0-alpha09 |
| Gradle Android Plugin | 8.1.0 |
| JVM 插件 | JetBrains Kotlin JVM |
| Serialization 插件 | Kotlinx Serialization |
| Compose Compiler 插件 | org.jetbrains.kotlin.plugin.compose |

---

## 运行方式

确保使用支持 Kotlin Multiplatform 和 JetBrains Compose 的 IDE（如 IntelliJ IDEA 或 Android Studio）。