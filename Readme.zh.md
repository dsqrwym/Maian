# MaiAn

<p align="center">
  <img src="docs/demo/brand-logo.png" width="110" alt="MaiAn">
</p>

> *本 README 由 **Antigravity**（Google DeepMind AI）根据仓库分析和项目文档生成。*

**切换语言：** [English](./Readme.md) · [Español](./Readme.es.md)

---

MaiAn 是一个面向批发商与零售商关系管理的 **B2B 多平台项目**，将针对不同用户角色的原生客户端应用与统一的、安全可扩展的后台服务相结合，设计为可持续增长的业务平台。

项目由 **dsqrwym**（技术标识）/ **MaiAn**（品牌名）开发。

---

## 目录

- [界面演示](#界面演示)
- [当前状态](#当前状态)
- [后端](#后端)
- [数据库](#数据库)
- [前端](#前端)
- [基础设施与部署](#基础设施与部署)
- [依赖参考 — 后端](#依赖参考--后端)
- [依赖参考 — 前端](#依赖参考--前端)

---

## 在线演示

可使用以下测试账号体验 Web 客户端：

| 客户端 | 演示地址 | 邮箱 | 用户名 | 密码 |
|---|---|---|---|---|
| 零售商端（`standard`） | [打开 Standard](https://maian.dsqrwym.es/standard/) | `standard@maian.com` | `standard` | `Standard123` |
| 批发商端（`enterprise`） | [打开 Enterprise](https://maian.dsqrwym.es/enterprise/) | `enterprise@maian.com` | `enterprise` | `Enterprise123` |

> 这些共享账号仅用于演示。请勿修改其凭据或删除共享演示数据。

---

## 界面演示

> 下列截图取自项目交付文档 **《MaiAn_Memoria_YU》**（第 5 章「实现与测试」、第 6 章「部署」），全部为应用的真实运行界面，而非设计稿。
> 为便于在 README 中流畅加载，图片统一按 1400 px 宽度等比压缩（共 34 张，约 3.8 MB）；原始高分辨率版本见交付文档。

### 总览

![MaiAn 主界面（浅色主题）](docs/demo/hero-app-light.png)

<p align="center"><sub>图 5.96 主界面：全局商品目录、分类筛选、搜索与排序、批发商店铺模式入口、主题与语言切换</sub></p>

### 零售端（`standard`）—— 浏览、加购与下单

| | | | |
|---|---|---|---|
| ![全局商品目录](docs/demo/retailer-catalog-mobile.png)<br><sub>图 5.5 全局商品目录</sub> | ![店铺模式](docs/demo/retailer-store-mode.png)<br><sub>图 5.6 指定批发商的店铺模式</sub> | ![分类层级导航](docs/demo/retailer-category-nav.png)<br><sub>图 5.7 分类层级导航</sub> | ![商品详情与变体](docs/demo/retailer-product-detail.png)<br><sub>图 5.8 商品详情与变体选择</sub> |
| ![购物车按批发商分组](docs/demo/retailer-cart-grouped.png)<br><sub>图 5.9 购物车按批发商自动分组</sub> | ![订单历史](docs/demo/retailer-order-history.png)<br><sub>图 5.10 订单历史</sub> | ![订单跟踪与操作](docs/demo/retailer-order-tracking.png)<br><sub>图 5.70 订单跟踪与操作（取消订单、下载 PDF）</sub> | |

### 批发商端（`enterprise`）—— 目录运营与订单处理

![批发商数据仪表盘](docs/demo/enterprise-dashboard.png)

<p align="center"><sub>图 5.23 数据仪表盘：订单状态分布、收入趋势、热销商品与每日订单</sub></p>

| | |
|---|---|
| ![分类管理](docs/demo/enterprise-category-management.png)<br><sub>图 5.11 分类管理（层级、税率、多语言翻译）</sub> | ![商品表格视图](docs/demo/enterprise-product-table.png)<br><sub>图 5.14 桌面端商品表格视图</sub> |
| ![商品创建表单](docs/demo/enterprise-product-form.png)<br><sub>图 5.79 商品创建表单</sub> | ![销售变体与库存](docs/demo/enterprise-product-variants.png)<br><sub>图 5.80 销售变体、价格与库存</sub> |
| <img src="docs/demo/enterprise-rich-editor.png" width="340" alt="富文本编辑器"><br><sub>图 5.81 富文本内容编辑器</sub> | ![商品详情预览](docs/demo/enterprise-product-preview.png)<br><sub>图 5.16 保存前的商品详情预览</sub> |
| ![订单管理](docs/demo/enterprise-order-management.png)<br><sub>图 5.18 收到的订单管理</sub> | ![订单筛选](docs/demo/enterprise-order-filters.png)<br><sub>图 5.21 订单筛选工具</sub> |
| ![拒单原因](docs/demo/enterprise-order-reject.png)<br><sub>图 5.93 拒单时必须填写原因</sub> | ![员工管理](docs/demo/enterprise-employee-management.png)<br><sub>图 5.22 员工账号管理（客服、配送、仓库）</sub> |

### 认证与账户

| | |
|---|---|
| ![批发商登录](docs/demo/auth-login-wholesaler.png)<br><sub>图 5.31 批发商登录</sub> | ![员工登录](docs/demo/auth-login-employee.png)<br><sub>图 5.32 员工登录（员工需先通过邮件激活账号）</sub> |
| ![零售商登录](docs/demo/auth-login-retailer.png)<br><sub>图 5.30 零售商登录</sub> | ![注册](docs/demo/auth-register.png)<br><sub>图 5.34 注册（零售商 / 批发商）</sub> |
| ![找回密码](docs/demo/auth-password-recovery.png)<br><sub>图 5.33 找回密码</sub> | **账户与会话安全**<br><br>· 邮箱验证码激活，未激活账号无法登录<br>· 按角色登录：`standard` / `enterprise` / `admin`<br>· Web 端 refresh token 存放于 httpOnly Cookie 并轮换；按设备的会话查看与注销由后端接口提供，前端暂无对应界面<br>· 零售商与批发商前端相互隔离，越权访问直接拒绝 |

### 主题、国际化与无障碍

| | |
|---|---|
| ![浅色主题](docs/demo/theme-light.png)<br><sub>图 5.2 浅色主题（Material Design 3 调色板）</sub> | ![深色主题](docs/demo/theme-dark.png)<br><sub>图 5.3 深色主题</sub> |
| ![深色模式下的商品目录](docs/demo/app-dark-mode.png)<br><sub>图 5.97 深色模式下的商品目录</sub> | ![语言切换](docs/demo/i18n-language-switch.png)<br><sub>图 5.99 语言切换（ca-ES / en / es-ES / fr-FR / pt-PT / zh-CN / zh-TW）</sub> |
| <img src="docs/demo/a11y-large-font.png" width="230" alt="字体放大"><br><sub>图 5.98 系统字体放大到最大时的布局</sub> | **无障碍与自适应**<br><br>· 系统字体放大到最大时，表格与表单自动重排，无文字截断或元素重叠<br>· 手机、平板、桌面分别使用底部导航、侧边导航与顶部导航（图 5.24–5.26）<br>· 深浅色主题均按 Material Design 对比度建议校验 |

### 自动化通知

| | |
|---|---|
| ![订单取消通知邮件](docs/demo/notify-order-cancelled.png)<br><sub>图 5.72 订单取消后自动发送给批发商的邮件</sub> | ![低库存告警邮件](docs/demo/notify-low-stock.png)<br><sub>图 5.86 库存低于阈值时自动触发的告警邮件</sub> |

### 部署与运维

| | |
|---|---|
| ![Swagger 文档](docs/demo/ops-swagger.png)<br><sub>图 6.5 OpenAPI / Swagger 交互式接口文档</sub> | ![克隆仓库](docs/demo/ops-git-clone.png)<br><sub>图 6.1 克隆仓库并安装依赖</sub> |

---

## 当前状态

### 已实现并可用

- 基于 NestJS 11 + Fastify 的模块化后端，支持 `single`、`cluster`、`pm2` 三种进程模式。
- 完整认证能力：注册、邮箱验证码验证、按角色登录（`standard` / `enterprise` / `admin`）、refresh token 轮换、按设备会话管理、密码重置。
- PostgreSQL 17 作为主数据库。两个 Redis 实例：一个用于缓存 / 会话 / 限流，一个专用于 BullMQ 队列。
- 基于 CASL 的属性级权限控制。
- 分类全 CRUD：层级结构、公开/私有可见性、多语言翻译。
- 商品全 CRUD：销售变体、多语言翻译、关联文件。
- 文件上传与读取，支持本地存储驱动和 Cloudflare R2 / S3 兼容驱动。`sharp` 图片处理，`pdfmake` PDF 生成。
- 地理位置 API：国家、省份、城市、ISO 数值货币代码。
- 为批发商创建内部员工账号（客服、配送、仓库）。
- `SUPERADMIN` 创建管理员账号。
- 完整 Docker Compose 编排：后端、PostgreSQL 17、两个 Redis、可选 Cloudflare Tunnel。
- Kotlin Multiplatform 前端，模块：`shared`、`standard`、`enterprise`、`admin`、`business`、`iosApp`。
- 前后端已在认证、分类、商品、文件、地理位置方面完成真实集成。
- 数据库模型已覆盖购物车、订单、消息、通知、配送和企业关系等领域。

### 部分实现或进行中

- **`standard` 模块**：认证与主结构已有，但业务功能覆盖明显少于 `enterprise`。
- **`business` 模块**：复用型业务组件层（分类、媒体、富文本编辑），并非独立的闭合客户端。

### 尚未完成

- 完整订单端到端流程（API + 前端客户端）。
- 前端聊天 / 消息系统完整打通。
- 成熟的推送通知系统成品。
- 销售统计与分析看板。
- 地图与地理定位扩展（计划未来版本）。

---

## 后端

位于 `Backend/backend-api`，采用 NestJS 模块化设计，Fastify 作为 HTTP 适配器。

### `AppModule` 中已接入的模块

`AuthModule` · `LocationsModule` · `CaslModule` · `UserModule` · `EnterpriseModule` · `AdminModule` · `CategoryModule` · `ProductsModule` · `FilesModule` · `MailModule` · `CacheRedisModule` · `ScheduleTaskModule` · `MyI18nModule` · `MyThrottlerModule`

此外还有全局异常过滤器、统一响应拦截器、Pino 结构化日志和 JWT 统一配置。支持三种进程模式：`single`、Node 原生 `cluster`、`pm2`。

### 功能列表

#### 认证与会话

- 零售商与批发商注册。
- 邮件验证码验证。
- 按角色登录：`standard`、`enterprise`、`admin`。
- Web 流程下的 httpOnly cookie refresh token，带轮换与 CSRF 保护。
- 密码重置。
- 登出与删除指定会话。
- 按设备管理会话。

#### 用户与企业管理

- 邮箱 / 用户名可用性检查。
- 带筛选与分页的用户查询。
- `SUPERADMIN` 创建管理员。
- 批发商创建员工：客服、配送、仓库。

#### 商品目录

- **分类**：创建、列表、搜索、编辑、删除、层级结构、公开/私有可见性、多语言翻译。
- **商品**：创建、列表、详情、编辑、删除、销售变体、多语言翻译、关联文件。

#### 文件

- `multipart` 上传。
- 真实 MIME 类型校验。
- 安全文件名生成。
- 本地存储驱动。
- Cloudflare R2 / S3 兼容存储驱动（`@aws-sdk`）。
- `sharp` 高性能图片处理。
- `pdfmake` PDF 文档生成。

#### 地理位置

- 国家、国家下省份、省份下城市。
- 根据 ISO 数值代码查询货币。

### 技术栈

| 技术 | 用途 |
|---|---|
| NestJS 11 | 应用框架 |
| Fastify 5 | 高性能 HTTP 适配器 |
| Drizzle ORM | 主 ORM 与数据库迁移 |
| PostgreSQL 17 | 主数据库 |
| Redis 7 | 缓存、会话、限流、BullMQ 队列 |
| JWT / Passport | 认证 |
| CASL | 属性级访问控制 |
| Swagger | API 文档 |
| Pino | 结构化日志 |
| BullMQ | 异步任务队列 |
| Nodemailer | 邮件发送 |
| nestjs-i18n | 国际化 |
| typia / nestia | 类型安全校验与序列化（已完全从 class-validator 迁移完成） |
| sharp | 图片处理 |
| pdfmake | PDF 生成 |

### 校验与数据清洗规范

后端明确区分两类字段：

- **用户输入字段**（`name`、`companyName`、`description` 等）：持久化前需进行语义清洗 + 严格校验。
- **系统字段**（`deviceName`、`langCode`、`timezone` 等）：不应被激进改写；校验策略更宽松，不做语义归一化。

无论输入如何错误，都不能让业务逻辑、持久化或授权流程崩溃。

---

## 数据库

主要通过 **Drizzle ORM** 管理。完整 SQL 架构位于 `Base_de_datos/schema.sql`，Docker Compose 启动 PostgreSQL 容器时会自动导入。

### 主要实体

用户 · 配置 · 地址 · 用户会话 · 验证 token · 分类（含翻译） · 商品（含变体） · 商品分类关系 · 文件 · 购物车 · 订单及订单明细 · 折扣 · 配送（含时间线） · 聊天 · 消息 · 通知 · 国家 · 省份 · 城市 · 货币

### 技术特性

- 多张表启用了 Row Level Security。
- 索引、约束和关联关系较完整。
- 根据角色自动生成 `user_id`。
- 预置参考数据：货币、国家、省份、城市、基础分类。
- 数据库模型的推进程度领先于部分 API 和客户端层。

---

## 前端

位于 `Frontend/Maian`，使用 **Kotlin Multiplatform** 与 **Compose Multiplatform**。

### 目标平台

| 平台 | 说明 |
|---|---|
| Android | minSdk 24，compileSdk 37 |
| iOS | 通过 `iosApp` 入口（SwiftUI wrapper） |
| Desktop | JVM / Swing |
| Web | Kotlin/Wasm |

### 模块

| 模块 | 说明 |
|---|---|
| `shared` | 公共基础：Ktor HTTP 客户端、各平台 token 存储、共享仓库（auth / category / products / file / location / user）、主题、i18n、时区、可复用 UI 组件、文件上传支持 |
| `standard` | 零售端客户端：登录、注册、主页、Koin 依赖注入、导航 |
| `enterprise` | 批发端客户端（最成熟）：登录、注册、分类 CRUD、商品 CRUD、表格/瀑布流视图 |
| `admin` | 管理员面板：登录、分类管理、用户仓库、独立 DI 与导航 |
| `business` | 复用型业务层：分类表单与列表、富文本编辑器、媒体选择与媒体管理 |
| `iosApp` | iOS 入口（SwiftUI wrapper） |

### 资源

- **图标**：Compose Material Icons（Core + Extended）。
- **字体**：MiSans（作为字体资源内嵌）。

### 关键版本

| 依赖 | 版本 |
|---|---|
| Kotlin | `2.4.20` |
| Compose Multiplatform | `1.12.1` |
| Android Gradle Plugin | `9.2.1` |
| Android compileSdk | `37` |
| Android minSdk | `24` |
| Ktor | `3.6.0` |
| Koin | `4.2.2` |
| Kotlinx Coroutines | `1.11.0` |
| Kotlinx Serialization JSON | `1.11.0` |
| Kotlinx DateTime | `0.8.0` |
| Coil 3 | `3.6.3` |
| Haze | `2.0.1` |

---

## 基础设施与部署

### Docker Compose

`docker-compose.yml` 通过**可选 profile** 控制启动哪些服务：

| 服务 | Profile | 说明 |
|---|---|---|
| `backend` | *（始终启动）* | NestJS API；支持 `single`、`cluster`、`pm2` 模式 |
| `postgres` | `postgres`、`local-infra` | PostgreSQL 17；自动导入 `Base_de_datos/` 下的全部 SQL 种子数据 |
| `redis-cache` | `redis`、`local-infra` | Redis 7，用于缓存、会话和限流 |
| `redis-bull` | `redis`、`local-infra` | Redis 7，专用于 BullMQ 队列 |
| `cloudflared` | `cloudflared` | Cloudflare Tunnel，用于安全公网暴露 |

本地完整启动（不含隧道）：

```bash
COMPOSE_PROFILES=postgres,redis docker compose up -d
```

### 环境变量配置

配置分为两个文件：

| 文件 | 内容 |
|---|---|
| `.env`（项目根目录） | Compose profile、端口、进程模式、本地 PostgreSQL 和 Redis 凭据 |
| `Backend/backend-api/.env` | 应用级密钥：JWT 密钥、S3/R2 凭据、SMTP 配置等 |

请分别参照对应的 `.env.example` 文件进行配置。

### 云端兼容

- **PostgreSQL**：Supabase 或其他托管 PostgreSQL —— 配置 `MAIAN_DATABASE_URL` 并从 `COMPOSE_PROFILES` 中移除 `postgres`。
- **Redis**：任意外部 Redis —— 配置 `MAIAN_REDIS_CACHE_URL` 和 `MAIAN_REDIS_BULL_URL`。
- **文件存储**：Cloudflare R2 或任意 S3 兼容服务。
- **公网暴露**：Cloudflare Tunnel（`cloudflared` profile）。

---

## 依赖参考 — 后端

### 生产依赖

| 包名 | 版本 |
|---|---|
| `@aws-sdk/client-s3` | `^3.1146.0` |
| `@aws-sdk/lib-storage` | `^3.1146.0` |
| `@casl/ability` | `^6.8.1` |
| `@fastify/cookie` | `^11.1.2` |
| `@fastify/helmet` | `^13.1.1` |
| `@fastify/multipart` | `^10.1.2` |
| `@fastify/secure-session` | `8.2.0` |
| `@fastify/static` | `^10.1.5` |
| `@keyv/redis` | `^5.1.6` |
| `@nest-lab/throttler-storage-redis` | `^1.2.0` |
| `@nestia/core` | `^11.3.4` |
| `@nestia/e2e` | `^11.3.4` |
| `@nestia/fetcher` | `^11.3.4` |
| `@nestjs-modules/mailer` | `^2.3.10` |
| `@nestjs/bullmq` | `^11.0.5` |
| `@nestjs/cache-manager` | `^3.1.3` |
| `@nestjs/common` | `^11.2.7` |
| `@nestjs/config` | `^4.0.4` |
| `@nestjs/core` | `^11.2.7` |
| `@nestjs/jwt` | `^11.0.2` |
| `@nestjs/mapped-types` | `^2.1.1` |
| `@nestjs/passport` | `^11.0.5` |
| `@nestjs/platform-express` | `^11.2.7` |
| `@nestjs/platform-fastify` | `^11.2.7` |
| `@nestjs/schedule` | `^6.1.3` |
| `@nestjs/swagger` | `^11.4.7` |
| `@nestjs/throttler` | `^6.7.1` |
| `bcrypt` | `^6.0.0` |
| `bullmq` | `^5.81.5` |
| `cache-manager` | `^7.2.9` |
| `cache-manager-redis-store` | `^3.0.1` |
| `cross-env` | `^10.1.0` |
| `decimal.js` | `^10.6.0` |
| `drizzle-orm` | `^0.45.3` |
| `fastify` | `5.11.3` |
| `file-type` | `^22.1.1` |
| `handlebars` | `^4.7.10` |
| `ioredis` | `^5.11.1` |
| `keyv` | `^5.6.0` |
| `libphonenumber-js` | `^1.13.14` |
| `lru-cache` | `^11.5.3` |
| `mime-types` | `^3.0.2` |
| `nestjs-i18n` | `^10.8.5` |
| `nestjs-pino` | `^4.6.1` |
| `nodemailer` | `10.0.9` |
| `passport` | `0.7.0` |
| `passport-custom` | `^1.2.1` |
| `passport-jwt` | `^4.0.1` |
| `pdfmake` | `^0.3.11` |
| `pg` | `^8.23.1` |
| `pino` | `^9.14.0` |
| `pino-pretty` | `^13.2.0` |
| `piscina` | `^5.3.2` |
| `postgres` | `^3.4.9` |
| `reflect-metadata` | `^0.2.2` |
| `rxjs` | `^7.8.2` |
| `sharp` | `0.35.4` |
| `typia` | `^12.2.1` |

### 开发依赖

| 包名 | 版本 |
|---|---|
| `@nestia/benchmark` | `^11.3.4` |
| `@nestia/sdk` | `^11.3.4` |
| `@nestjs/cli` | `^11.0.24` |
| `@nestjs/schematics` | `^11.1.0` |
| `@nestjs/testing` | `^11.2.7` |
| `@swc/cli` | `^0.7.10` |
| `@swc/core` | `^1.16.13` |
| `@types/bcrypt` | `^6.0.0` |
| `@types/express` | `^5.0.6` |
| `@types/jest` | `^30.0.0` |
| `@types/mime-types` | `^3.0.1` |
| `@types/node` | `^24.19.1` |
| `@types/nodemailer` | `^7.0.12` |
| `@types/passport` | `^1.0.17` |
| `@types/passport-jwt` | `^4.0.1` |
| `@types/pdfmake` | `^0.3.3` |
| `@types/pg` | `^8.23.1` |
| `@types/supertest` | `^6.0.3` |
| `@typescript-eslint/eslint-plugin` | `^8.71.1` |
| `@typescript-eslint/parser` | `^8.71.1` |
| `dotenv` | `^17.4.2` |
| `drizzle-kit` | `^0.31.11` |
| `eslint` | `^9.39.5` |
| `eslint-config-prettier` | `^10.1.8` |
| `eslint-plugin-prettier` | `^5.5.6` |
| `globals` | `^16.5.0` |
| `jest` | `^30.5.2` |
| `nestia` | `^11.3.4` |
| `prettier` | `^3.9.9` |
| `source-map-support` | `^0.5.21` |
| `supertest` | `^7.3.1` |
| `ts-jest` | `^29.4.14` |
| `ts-loader` | `^9.6.2` |
| `ts-node` | `^10.9.2` |
| `ts-patch` | `^3.3.0` |
| `tsc-alias` | `^1.9.7` |
| `tsconfig-paths` | `^4.2.0` |
| `tsx` | `^4.23.15` |
| `typescript` | `~6.0.3` |
| `typescript-eslint` | `^8.71.1` |

---

## 依赖参考 — 前端

所有版本均来自 `Frontend/Maian/gradle/libs.versions.toml`。

### Gradle 插件

| 别名 | Plugin ID | 版本 |
|---|---|---|
| `androidApplication` / `androidLibrary` | `com.android.application` / `com.android.library` | `9.2.1` |
| `composeMultiplatform` | `org.jetbrains.compose` | `1.12.1` |
| `composeCompiler` | `org.jetbrains.kotlin.plugin.compose` | `2.4.20` |
| `kotlinMultiplatform` | `org.jetbrains.kotlin.multiplatform` | `2.4.20` |
| `kotlinxSerialization` | `org.jetbrains.kotlin.plugin.serialization` | `2.4.20` |

### Android 与生命周期

| 包名 | 版本 |
|---|---|
| `androidx.activity:activity-compose` | `1.13.0` |
| `androidx.core:core-ktx` | `1.19.1` |
| `androidx.security:security-crypto` | `1.1.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` | `2.11.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` | `2.11.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | `2.11.0` |
| `org.jetbrains.androidx.savedstate:savedstate` | `1.4.0` |
| `org.jetbrains.androidx.window:window-core` | `1.5.1` |

### 网络 — Ktor

| 包名 | 版本 |
|---|---|
| `io.ktor:ktor-client-core` | `3.6.0` |
| `io.ktor:ktor-client-content-negotiation` | `3.6.0` |
| `io.ktor:ktor-serialization-kotlinx-json` | `3.6.0` |
| `io.ktor:ktor-client-auth` | `3.6.0` |
| `io.ktor:ktor-client-logging` | `3.6.0` |
| `io.ktor:ktor-client-okhttp`（Android） | `3.6.0` |
| `io.ktor:ktor-client-darwin`（iOS） | `3.6.0` |
| `io.ktor:ktor-client-cio`（Desktop） | `3.6.0` |
| `io.ktor:ktor-client-js`（Web） | `3.6.0` |

### 依赖注入 — Koin

| 包名 | 版本 |
|---|---|
| `io.insert-koin:koin-core` | `4.2.2` |
| `io.insert-koin:koin-compose-viewmodel` | `4.2.2` |

### Kotlinx 库

| 包名 | 版本 |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-swing` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-datetime` | `0.8.0` |
| `org.jetbrains.kotlinx:kotlinx-collections-immutable` | `0.5.2` |

### 图片、媒体与文件

| 包名 | 版本 |
|---|---|
| `io.coil-kt.coil3:coil-compose` | `3.6.3` |
| `io.coil-kt.coil3:coil-network-ktor3` | `3.6.3` |
| `io.github.vinceglb:filekit-core` | `0.16.0` |
| `io.github.vinceglb:filekit-dialogs-compose` | `0.16.0` |
| `io.github.vinceglb:filekit-coil` | `0.16.0` |
| `io.github.kdroidfilter:composemediaplayer` | `0.11.4` |
| `io.github.alexzhirkevich:compottie-lite` | `2.3.3` |

### UI 组件与体验

| 包名 | 版本 |
|---|---|
| `dev.chrisbanes.haze:haze` | `2.0.1` |
| `dev.chrisbanes.haze:haze-blur` | `2.0.1` |
| `com.eygraber:compose-placeholder-material3` | `1.0.12` |
| `dev.zt64.compose.pipette:compose-pipette` | `2.0.0` |
| `sh.calvin.reorderable:reorderable` | `3.1.0` |
| `io.github.dokar3:sonner` | `0.4.0` |
| `net.engawapg.lib:zoomable` | `2.16.0` |
| `io.github.khubaibkhan4:alert-kmp` | `2.0.0` |
| `com.patrykandpatrick.vico:compose` | `3.3.1` |
| `com.patrykandpatrick.vico:compose-m3` | `3.3.1` |
| `org.jetbrains.compose.material:material-icons-core` | `1.7.3` |
| `org.jetbrains.compose.material:material-icons-extended` | `1.7.3` |
| `org.jetbrains.compose.ui:ui-tooling` | `1.12.1` |
| `org.slf4j:slf4j-simple` | `2.0.20` |

### 导航

| 包名 | 版本 |
|---|---|
| `org.jetbrains.androidx.navigation:navigation-compose` | `2.9.2` |
| `androidx.navigation3:navigation3-runtime` | `1.2.0` |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | `1.1.2` |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` | `1.3.0` |

### 分页

| 包名 | 版本 |
|---|---|
| `androidx.paging:paging-common` | `3.5.1` |
| `androidx.paging:paging-compose` | `3.5.1` |

### 表格

| 包名 | 版本 |
|---|---|
| `ua.wwind.table-kmp:table-core` | `2.4.1` |

### 通用能力与领域工具

| 包名 | 版本 |
|---|---|
| `com.russhwolf:multiplatform-settings` | `1.3.0` |
| `io.github.luca992.libphonenumber-kotlin:libphonenumber` | `0.1.9` |
| `com.sanctionco.jmail:jmail` | `2.2.2` |
| `com.ionspin.kotlin:bignum` | `0.3.10` |

### 扫码与条码

| 包名 | 版本 |
|---|---|
| `io.github.ismai117:KScan` | `0.10.0` |
| `com.google.zxing:core` | `3.5.4` |
| `com.google.zxing:javase` | `3.5.4` |
| `com.journeyapps:zxing-android-embedded` | `4.3.0` |
| `com.github.sarxos:webcam-capture` | `0.3.12` |

### WebView

| 包名 | 版本 |
|---|---|
| `io.github.kevinnzou:compose-webview-multiplatform` | `2.0.3` |

### 富文本编辑

| 包名 | 版本 |
|---|---|
| `com.mohamedrejeb.richeditor:richeditor-compose` | `1.2.1` |
