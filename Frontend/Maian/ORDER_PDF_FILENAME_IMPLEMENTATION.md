# 订单PDF文件名从后端获取功能实现

## 📋 实现概述

已成功实现从后端HTTP响应头中获取PDF文件名的功能，采用**智能降级策略**：
- **下载操作**：优先使用后端返回的文件名，如果没有则降级到前端生成
- **预览操作**：使用前端生成的文件名（临时文件，不重要）

---

## 🔧 修改的文件

### 1. **SharedOrderPdfApi.kt** 
路径：`shared/src/commonMain/kotlin/org/dsqrwym/shared/data/orders/pdf/SharedOrderPdfApi.kt`

#### 新增功能：
```kotlin
/**
 * 从HTTP响应头中提取文件名
 * 优先级：Content-Disposition > X-File-Name > X-Filename > null
 */
fun extractFileNameFromResponse(response: HttpResponse): String?
```

#### 提取策略（按优先级）：
1. **Content-Disposition 响应头**（标准HTTP方式）
   - 解析 `filename` 参数
   - 支持 `filename*` (RFC 5987 编码格式)
   
2. **自定义响应头**（备选方案）
   - `X-File-Name`
   - `X-Filename`
   - `X-File-Name-Encoded`

3. **降级方案**
   - 如果都没有，返回 `null`，使用前端生成的文件名

---

### 2. **SharedOrderPdfRepository.kt**
路径：`shared/src/commonMain/kotlin/org/dsqrwym/shared/data/orders/pdf/SharedOrderPdfRepository.kt`

#### 修改内容：

**预览PDF** - 使用前端生成的文件名：
```kotlin
suspend fun previewOrderPdf(orderId: String) =
    fetchAndHandle(
        // ...
        preferServerFileName = false, // 预览使用前端生成的文件名
    )
```

**下载PDF** - 优先使用后端返回的文件名：
```kotlin
suspend fun downloadOrderPdf(orderId: String) =
    fetchAndHandle(
        // ...
        preferServerFileName = true, // 下载优先使用后端返回的文件名
    )
```

**智能文件名选择逻辑**：
```kotlin
val fileName = if (preferServerFileName) {
    // 优先使用后端返回的文件名，如果没有则降级到前端生成
    api.extractFileNameFromResponse(response)
        ?.let { sanitizeOrderPdfFileName(it) }
        ?: buildOrderPdfFileName(normalizedOrderId)
} else {
    // 预览使用前端生成的文件名
    buildOrderPdfFileName(normalizedOrderId)
}
```

---

## 🎯 后端需要返回的响应头格式

### 方案一：Content-Disposition（推荐，标准方式）

```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="order_12345_20240517.pdf"
```

或者带UTF-8编码的文件名：
```http
Content-Disposition: attachment; filename*=UTF-8''order_%E8%AE%A2%E5%8D%95_12345.pdf
```

### 方案二：自定义响应头

```http
HTTP/1.1 200 OK
Content-Type: application/pdf
X-File-Name: order_12345_20240517.pdf
```

---

## 🔄 完整流程

```mermaid
graph TB
    A[用户点击下载PDF] --> B[OrderDetailViewModel.downloadPdf]
    B --> C[SharedOrderPdfRepository.downloadOrderPdf]
    C --> D[API请求: GET /order-file/{id}/download]
    D --> E[后端返回PDF + 响应头]
    E --> F{检查响应头}
    F -->|有Content-Disposition| G[提取filename]
    F -->|有X-File-Name| G
    F -->|都没有| H[使用前端生成: order-{id}.pdf]
    G --> I[清理文件名特殊字符]
    H --> I
    I --> J[平台服务处理下载]
    J --> K[显示成功提示]
```

---

## 📝 文件名处理流程

### 后端返回：`order_订单12345_20240517.pdf`

1. **提取文件名**
   ```kotlin
   extractFileNameFromResponse(response) 
   // 返回: "order_订单12345_20240517.pdf"
   ```

2. **清理特殊字符**
   ```kotlin
   sanitizeOrderPdfFileName("order_订单12345_20240517.pdf")
   // 替换 \/:*?"<>| 为 _
   // 返回: "order_订单12345_20240517.pdf"
   ```

3. **最终下载文件名**
   ```
   order_订单12345_20240517.pdf
   ```

### 后端没有返回文件名

1. **前端生成**
   ```kotlin
   buildOrderPdfFileName("ORD-12345")
   // 返回: "order-ORD-12345.pdf"
   ```

2. **清理特殊字符**
   ```kotlin
   sanitizeOrderPdfFileName("order-ORD-12345.pdf")
   // 返回: "order-ORD-12345.pdf"
   ```

---

## ✅ 优势

1. **向后兼容**：如果后端没有返回文件名，自动降级到前端生成
2. **灵活性**：支持多种响应头格式
3. **安全性**：文件名清理函数防止非法字符
4. **跨平台**：兼容 Android、iOS、Desktop、Web
5. **智能策略**：下载用后端文件名，预览用前端文件名

---

## 🔍 调试建议

如果需要查看后端返回的响应头，可以临时添加日志：

```kotlin
// 在 SharedOrderPdfApi.kt 的 extractFileNameFromResponse 方法中
println("=== PDF Response Headers ===")
println("Content-Disposition: ${response.headers[HttpHeaders.ContentDisposition]}")
println("X-File-Name: ${response.headers["X-File-Name"]}")
println("X-Filename: ${response.headers["X-Filename"]}")
```

---

## 📌 注意事项

1. **后端配合**：需要后端在响应头中返回文件名
2. **文件名编码**：如果包含非ASCII字符（如中文），建议使用 `filename*` 格式
3. **文件名长度**：建议后端返回的文件名不超过255字符
4. **文件扩展名**：后端应该返回带 `.pdf` 扩展名的完整文件名

---

## 🚀 测试步骤

1. **确认后端返回响应头**
   - 使用 Postman 或浏览器开发者工具检查响应头
   
2. **测试下载功能**
   - 点击下载按钮，查看保存的文件名是否为后端返回的
   
3. **测试降级方案**
   - 如果后端不返回文件名，确认使用前端生成的 `order-{orderId}.pdf`

4. **测试特殊字符**
   - 文件名包含特殊字符时，确认被正确清理

---

## 📞 需要后端确认

请和后端开发确认：
- ✅ 是否在PDF响应中返回 `Content-Disposition` 头？
- ✅ 文件名格式是什么？（例如：`order_{orderId}_{date}.pdf`）
- ✅ 是否支持UTF-8编码的文件名？（如果包含中文等特殊字符）
