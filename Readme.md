# 国际化 JSON 动态加载方案 (已弃用)

---

本分支保留了 Kotlin Multiplatform 项目中曾使用的一种国际化方案，该方案通过**运行时加载 JSON** 并**嵌套访问语言键**。此方案现已弃用，作为技术演进的记录。

## 📦 实现概览

* **语言包文件**：每种语言对应一个 JSON 文件，例如 `zh-CN.json`，存储在 `commonMain/resources/files/` 路径下。
* **加载器**：`LocalizationManager` 在应用初始化时加载相应语言包，默认语言为 `zh-CN`。
* **访问方式**：通过 `SharedLanguage.login.background.content_description.get()` 方式获取文本。

该方案借助 **KSP** (Kotlin Symbol Processing) 实现了在构建时**自动生成** `LocalizationManager` 的 `getString` 方法调用。这能有效**避免键值拼写错误**，提高开发和测试效率。为了解决 `commonMain` 模块无法直接访问生成代码的问题，生成的 `SharedLanguage` 类会被**复制**到 `commonMain` 资源目录下。

同时，它**支持运行时语言切换**，只需调用 `LocalizationManager.setLocale(...)` 即可。

---

## ⚠️ 弃用原因

该方案存在以下限制：

* **初始化复杂**：需要等待语言包加载完毕才能使用。
* **性能不佳**：每次文本访问都需要通过 `Map` 查找键，无法利用编译器优化，且从 JSON 加载（尤其是在缓存丢失时）可能导致性能瓶颈和潜在错误。

---

## 🆕 替代方案

后续方案将采用 **KSP + 代码生成**，在**编译期**将 JSON 内容转换为 Kotlin 对象结构，从而实现：

* **全 IDE 支持**：享受 IDE 提供的代码补全和类型检查。
* **零运行时开销**：无需运行时加载或查找，性能更优。
* **构建时语言校验**：在编译阶段捕获缺失的键或格式错误。
* **动态语言切换**：保留运行时切换语言的能力。