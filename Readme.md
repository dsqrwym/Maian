# README de Estado Actual de MaiAn

Este documento de presentacion del proyecto ha sido generado por Codex.

Sitio oficial de Codex/OpenAI: [https://openai.com/](https://openai.com/)

---

# Version en espanol

## 1. Introduccion

MaiAn es una plataforma B2B multiplataforma orientada a la relacion comercial entre distribuidores mayoristas y minoristas. Segun la propuesta del proyecto, la solucion debe combinar aplicaciones cliente para distintos perfiles con un backend centralizado, seguro, escalable y preparado para crecer con nuevas funciones de negocio.

## 2. Resumen ejecutivo

### Ya implementado o claramente operativo

- Backend modular con NestJS y Fastify.
- Autenticacion completa con registro, verificacion por correo, login por roles, refresh token, sesiones y reseteo de contrasena.
- Persistencia principal en PostgreSQL y soporte de Redis para cache y sesiones.
- Control de acceso por roles y CASL.
- Gestion de categorias.
- Gestion de productos, variantes y traducciones.
- Subida y recuperacion de archivos.
- API de localizaciones: paises, provincias, ciudades y monedas.
- Creacion de empleados internos para mayoristas.
- Creacion de administradores.
- Frontend Kotlin Multiplatform con modulos diferenciados para `standard`, `enterprise`, `admin`, `business` y `shared`.
- Integracion de frontend con API de autenticacion, categorias, productos, archivos y localizacion.
- Base de datos amplia que ya contempla carritos, pedidos, mensajes, notificaciones, entregas, archivos y relaciones empresariales.

### Implementado de forma parcial o en consolidacion

- Migracion de validaciones hacia `typia`: ya hay endpoints y DTOs usando `TypedRoute`, `TypedQuery`, `typia.createAssert(...)` y `MyTypedBody`, pero todavia conviven dependencias y trazas del enfoque con `class-validator`.
- Frontend `standard` con autenticacion y estructura principal, pero con menos cobertura funcional visible que `enterprise`.
- Modulo `business` con componentes y repositorios reutilizables, especialmente en categorias, media y edicion enriquecida, pero no como cliente cerrado independiente.
- Infraestructura cloud parcialmente reflejada en codigo y configuracion, aunque el `docker-compose.yml` del raiz actualmente solo declara `cloudflared`.

### Objetivos del proyecto no vistos como cerrados en este repositorio

- Flujo completo de pedidos de extremo a extremo en API y clientes.
- Sistema de mensajeria/chat plenamente expuesto y conectado en frontend.
- Sistema funcional de notificaciones a nivel de producto final.
- Paneles de estadisticas o historicos de venta visibles en la interfaz.
- Geolocalizacion y mapas, coherente con la propuesta de dejarlo para una ampliacion futura.

## 3. Estado actual del backend

## 3.1 Arquitectura general

El backend principal se encuentra en `Backend/backend-api` y utiliza una arquitectura modular basada en NestJS. El `AppModule` integra modulos reales y conectados entre si, entre ellos:

- `AuthModule`
- `LocationsModule`
- `CaslModule`
- `UserModule`
- `EnterpriseModule`
- `AdminModule`
- `CategoryModule`
- `ProductsModule`
- `FilesModule`
- `MailModule`
- `PrismaModule`
- `CacheRedisModule`
- `ScheduleTaskModule`
- `MyI18nModule`
- `MyThrottlerModule`

Tambien se observan filtros globales, interceptor de respuesta, logger estructurado y configuracion JWT centralizada.

## 3.2 Funcionalidades visibles del backend

### Autenticacion y sesiones

- Registro de minoristas.
- Registro de mayoristas.
- Verificacion por codigo enviado al correo.
- Login por perfiles:
  - `standard`
  - `enterprise`
  - `admin`
- Login web con cookie httpOnly para refresh token.
- Refresh token para cliente normal y para flujo web con rotacion y CSRF.
- Reseteo de contrasena.
- Cierre de sesion y borrado de sesiones concretas.
- Gestion de sesion por dispositivo.

### Gestion de usuarios y organizacion empresarial

- Comprobacion de email disponible.
- Comprobacion de username disponible.
- Busqueda de usuarios con filtros y paginacion.
- Creacion de administradores por `SUPERADMIN`.
- Creacion de empleados del mayorista:
  - soporte
  - reparto
  - almacen

### Catalogo y administracion comercial

- Gestion de categorias:
  - crear
  - listar
  - buscar
  - editar
  - eliminar
  - categorias jerarquicas
  - categorias publicas y privadas
  - traducciones
- Gestion de productos:
  - crear
  - listar
  - ver detalle
  - editar
  - eliminar
  - variantes de venta
  - traducciones
  - ficheros asociados

### Archivos

- Subida `multipart`.
- Validacion del MIME real.
- Renombrado seguro de archivo.
- Recuperacion de ficheros de producto.
- Driver de almacenamiento local.
- Driver compatible con Cloudflare R2 / S3.

### Localizacion

- Paises.
- Provincias por pais.
- Ciudades por provincia.
- Monedas por codigo ISO numerico.

## 3.3 Tecnologia y dependencias del backend

### Stack principal del backend

- NestJS 11
- Fastify
- Prisma
- PostgreSQL
- Redis
- JWT
- Passport
- CASL
- Swagger
- Pino
- BullMQ
- Nodemailer
- i18n
- typia
- nestia

### Dependencias relevantes observadas en `package.json`

- Framework y plataforma:
  - `@nestjs/common`
  - `@nestjs/core`
  - `@nestjs/platform-fastify`
  - `fastify`
- Configuracion y documentacion:
  - `@nestjs/config`
  - `@nestjs/swagger`
- Autenticacion y seguridad:
  - `@nestjs/jwt`
  - `@nestjs/passport`
  - `passport`
  - `passport-jwt`
  - `passport-custom`
  - `bcrypt`
  - `@fastify/cookie`
  - `@fastify/secure-session`
- Base de datos:
  - `@prisma/client`
  - `@prisma/adapter-pg`
  - `pg`
- Cache y Redis:
  - `@keyv/redis`
  - `keyv`
  - `ioredis`
  - `cache-manager`
  - `cache-manager-redis-store`
  - `@nest-lab/throttler-storage-redis`
- Validacion y serializacion:
  - `typia`
  - `@nestia/core`
  - `@nestia/sdk`
  - `@nestia/fetcher`
  - `@nestia/e2e`
  - `class-validator`
  - `class-transformer`
- Permisos:
  - `@casl/ability`
  - `@casl/prisma`
- Archivos y almacenamiento:
  - `@aws-sdk/client-s3`
  - `@aws-sdk/lib-storage`
  - `@fastify/multipart`
  - `file-type`
  - `mime-types`
- Correo y colas:
  - `@nestjs-modules/mailer`
  - `@nestjs/bullmq`
  - `bullmq`
  - `handlebars`
  - `nodemailer`
- Observabilidad y utilidades:
  - `nestjs-pino`
  - `pino`
  - `pino-pretty`
  - `nestjs-i18n`
  - `@nestjs/schedule`
  - `lru-cache`
  - `libphonenumber-js`
  - `piscina`

### Tooling de desarrollo del backend

- TypeScript
- ESLint
- Prettier
- Jest
- ts-jest
- SWC
- Prisma CLI
- ts-patch

## 3.4 Situacion de la migracion `class-validator -> typia`

La migracion esta realmente en curso, no solo planificada.

### Evidencias

- Presencia de `typia` y `nestia` en dependencias.
- Uso de `TypedRoute`, `TypedQuery` y `MyTypedBody`.
- Uso de validadores y asserts tipados en varios controladores.

### Estado

- Ya hay partes funcionales migradas.
- `class-validator` y `class-transformer` siguen presentes.
- El backend debe considerarse en transicion, no completamente migrado.

## 3.5 Norma de desarrollo del backend sobre validacion y saneamiento

Para mantener coherencia funcional y evitar efectos secundarios innecesarios, el backend sigue una distincion clara entre campos introducidos por personas usuarias y campos de tipo tecnico o de sistema.

### Campos de entrada de usuario

Los campos cuyo contenido depende directamente de texto libre o semilibre introducido por la persona usuaria deben recibir saneamiento de datos antes o durante su procesamiento. Esto aplica especialmente a campos como:

- `name`
- `companyName`
- `description`
- `title`
- otros campos equivalentes de contenido visible o editable por la persona usuaria

El objetivo del saneamiento es:

- reducir ruido en los datos
- evitar formatos inconsistentes innecesarios
- mejorar calidad de almacenamiento y presentacion
- disminuir problemas derivados de espacios, caracteres no deseados o entradas evidentemente defectuosas

### Campos de sistema o tecnicos

Los campos cuyo valor representa informacion tecnica, de dispositivo, localizacion tecnica o codigos internos no deben someterse al mismo tipo de saneamiento semantico de texto. Esto aplica a campos como:

- `deviceName`
- `langCode`
- `timezone`

Estos valores son tratados como datos de sistema o de protocolo y, por tanto:

- no deben reescribirse agresivamente
- no deben normalizarse como si fueran texto libre de usuario
- en muchos casos deben conservarse tal y como llegan para no romper trazabilidad ni compatibilidad

### Regla general de validacion

Los campos utilizados para crear recursos, cuentas o registros deben validarse siempre. Sin embargo, la validacion debe diseñarse para que una entrada incorrecta no provoque errores de logica interna del sistema.

Esto implica:

- validar obligatoriamente los datos de creacion
- rechazar o controlar entradas incorrectas sin comprometer la estabilidad del flujo
- evitar que un dato mal formado termine rompiendo logica de negocio, persistencia o autorizacion

### Regla para campos tecnicos del sistema

Los campos originados por el sistema, por el cliente tecnico o por metadata operativa deben validarse de forma mas flexible, o incluso no someterse al mismo nivel de validacion estricta que los campos de creacion orientados al usuario final.

La razon es que:

- suelen funcionar como metadata tecnica
- a menudo admiten variaciones legitimas entre plataformas
- una validacion excesivamente cerrada puede romper compatibilidad sin aportar valor real

En resumen:

- campos de usuario: saneamiento + validacion adecuada
- campos de creacion: validacion obligatoria
- campos tecnicos o de sistema: sin saneamiento semantico y con validacion mas flexible o relajada

## 4. Estado actual de la base de datos

La carpeta `Base_de_datos` y `prisma/schema.prisma` muestran una base de datos considerablemente avanzada.

### Entidades presentes

- usuarios
- configuraciones
- direcciones
- sesiones de usuario
- tokens de verificacion
- categorias y traducciones
- productos y variantes
- relaciones producto-categoria
- archivos
- carritos
- pedidos y detalle de pedidos
- descuentos
- entregas y linea temporal de entregas
- chats
- mensajes
- notificaciones
- paises
- provincias
- ciudades
- monedas

### Observaciones tecnicas

- Hay Row Level Security en varias tablas.
- Existen indices, restricciones y relaciones bien definidas.
- Hay generacion automatica de `user_id` segun rol.
- La base de datos va por delante de algunas partes de la capa cliente.

## 5. Estado actual del frontend

El frontend esta en `Frontend/Maian` y usa Kotlin Multiplatform + Compose Multiplatform.

### Modulos observados

- `shared`
- `standard`
- `enterprise`
- `admin`
- `business`
- `iosApp`

### Targets observados

- Android
- Desktop
- iOS
- Web
- Wasm

## 5.1 Estado funcional por modulo

### `shared`

Es la base comun del ecosistema cliente y contiene:

- cliente HTTP multiplataforma
- almacenamiento de tokens por plataforma
- repositorios compartidos de:
  - auth
  - category
  - products
  - file
  - location
  - user
- navegacion compartida
- tema visual compartido
- localizacion e idioma
- gestion de zona horaria
- componentes reutilizables de UI
- utilidades de validacion
- soporte de subida de archivos

### `standard`

Corresponde al perfil minorista o cliente base.

Lo visible actualmente:

- pantalla inicial
- login
- registro
- inyeccion de dependencias con Koin
- rutas de navegacion
- despliegue multiplataforma

### `enterprise`

Es el cliente mas maduro funcionalmente y encaja con el uso de distribuidor/mayorista.

Funciones visibles:

- login
- registro
- pantalla inicial
- listado de categorias
- creacion de categorias
- edicion de categorias
- listado de productos
- creacion de productos
- vistas tipo tabla y cascada para productos
- repositorios y APIs de auth, categorias y productos

### `admin`

Cliente de administracion con:

- login
- pantalla inicial
- categorias
- repositorio de usuarios
- DTOs orientados a mayoristas
- navegacion y DI propias

### `business`

Modulo de negocio reutilizable con:

- API y repositorio de categorias
- formularios de categoria
- lista de categorias
- editor de texto enriquecido
- gestion de medios y media picker

## 5.2 Tecnologia y dependencias del frontend

Aqui se integran tanto los hallazgos del codigo actual como el contenido del `Readme.md` del raiz.

### Stack principal del frontend

- Kotlin Multiplatform
- Compose Multiplatform
- Jetpack Compose para Android
- Koin para inyeccion de dependencias
- Kotlinx Serialization
- Kotlinx DateTime
- Kotlin Coroutines
- Ktor Client
- Multiplatform Settings
- Navigation Compose / Navigation 3
- WebView multiplataforma
- Coil 3
- FileKit
- Haze
- libphonenumber
- ZXing / KScan
- Rich Editor Compose
- tablas y componentes avanzados para escritorio/web

### Versiones principales declaradas actualmente en `gradle/libs.versions.toml`

- Kotlin: `2.3.20`
- Compose Multiplatform: `1.10.3`
- Android Gradle Plugin: `8.12.3`
- Android Compile SDK: `36`
- Android Min SDK: `24`
- Android Target SDK: `35`
- Ktor: `3.4.1`
- Koin: `4.2.0`
- Kotlinx Serialization JSON: `1.10.0`
- Kotlinx DateTime: `0.7.1`
- Coroutines: `1.10.2`

### Dependencias relevantes del frontend

- Base multiplataforma:
  - `org.jetbrains.kotlin.multiplatform`
  - `org.jetbrains.compose`
  - `org.jetbrains.kotlin.plugin.compose`
  - `org.jetbrains.kotlin.plugin.serialization`
- Android:
  - `androidx.activity:activity-compose`
  - lifecycle runtime y viewmodel
  - `androidx.security:security-crypto`
- Red:
  - `io.ktor:ktor-client-core`
  - `ktor-client-content-negotiation`
  - `ktor-serialization-kotlinx-json`
  - `ktor-client-auth`
  - `ktor-client-logging`
  - engines `okhttp`, `darwin`, `cio`, `js`
- DI y estado:
  - `io.insert-koin:koin-core`
  - `io.insert-koin:koin-compose-viewmodel`
- Multimedia y archivos:
  - `io.coil-kt.coil3:coil-compose`
  - `coil-network-ktor3`
  - `io.github.vinceglb:filekit-core`
  - `filekit-dialogs-compose`
  - `filekit-coil`
  - `network.chaintech:compose-multiplatform-media-player`
  - `io.github.kdroidfilter:composemediaplayer`
- UI y experiencia:
  - `dev.chrisbanes.haze:haze`
  - `com.eygraber:compose-placeholder-material3`
  - `dev.zt64.compose.pipette:compose-pipette`
  - `sh.calvin.reorderable:reorderable`
  - `com.seanproctor:datatable-material3`
  - `ua.wwind.table-kmp:table-core`
  - `io.github.dokar3:sonner`
  - `net.engawapg.lib:zoomable`
- Navegacion:
  - `org.jetbrains.androidx.navigation:navigation-compose`
  - `androidx.navigation3:navigation3-runtime`
  - `org.jetbrains.androidx.navigation3:navigation3-ui`
- Utilidades y dominio:
  - `org.jetbrains.kotlinx:kotlinx-datetime`
  - `org.jetbrains.kotlinx:kotlinx-serialization-json`
  - `org.jetbrains.kotlinx:kotlinx-collections-immutable`
  - `com.russhwolf:multiplatform-settings`
  - `io.github.luca992.libphonenumber-kotlin:libphonenumber`
  - `com.sanctionco.jmail:jmail`
- Escaneo y codigo de barras:
  - `io.github.ismai117:KScan`
  - `com.google.zxing:core`
  - `com.google.zxing:javase`
  - `com.journeyapps:zxing-android-embedded`
- WebView y web:
  - `io.github.kevinnzou:compose-webview-multiplatform`
  - recursos web y wasm propios del proyecto
- Edicion enriquecida:
  - `com.mohamedrejeb.richeditor:richeditor-compose`

## 5.3 Recursos y atribuciones integradas desde el `Readme.md` del raiz

El `Readme.md` del raiz documenta ademas varios puntos sobre recursos y origen de librerias del frontend:

- uso de Compose Icons como fuente de iconografia
- uso de la fuente MiSans para parte de la UI
- uso de librerias multiplataforma de JetBrains, AndroidX, Google y proyectos de terceros

En consecuencia, el frontend no solo tiene estructura multiplataforma, sino tambien una seleccion de librerias orientadas a:

- UI moderna
- multimedia
- navegacion adaptable
- almacenamiento seguro
- validacion
- internacionalizacion
- experiencia de escritorio y web

## 6. Infraestructura y despliegue

### Elementos visibles

- `docker-compose.yml`
- `cloudflared/`
- `config.yml`
- `Dockerfile` del backend
- archivos `.env` y ejemplos

### Estado observado

- El `docker-compose.yml` del raiz actualmente solo define `cloudflared`.
- El backend tiene estructura adecuada para despliegue cloud.
- La propuesta menciona Supabase, Redis, Northflank, Cloudflare R2 y posibles migraciones futuras. En el repositorio se observan piezas compatibles con esa direccion, pero no toda la infraestructura esta totalmente automatizada desde el compose local.

## 7. Relacion entre la propuesta original y el estado actual

### Objetivos con avance fuerte

- Plataforma B2B con separacion clara de perfiles.
- Backend escalable y modular con NestJS + Fastify.
- Persistencia principal en PostgreSQL.
- Uso de Redis para sesion y cache.
- Gestion de productos y categorias.
- Registro, login, seguridad y control de acceso.
- Almacenamiento multimedia.
- Soporte multiplataforma con Kotlin Multiplatform y Compose Multiplatform.

### Objetivos con avance parcial

- Gestion comercial completa entre distribuidores y minoristas.
- Administracion extendida de usuarios y empleados.
- Cliente minorista plenamente desarrollado.
- Integracion total de pedidos con interfaces cliente.

### Objetivos aun no cerrados o no claramente visibles

- Pedidos end-to-end completos.
- Mensajeria interna plenamente funcional.
- Notificaciones visibles y conectadas en toda la aplicacion.
- Estadisticas comerciales en interfaz final.

---

# 中文版本

## 1. 项目说明

MaiAn 是一个面向批发商与零售商关系管理的 B2B 多平台项目。按照项目原始提案，它需要同时具备多端客户端、统一的后台服务、安全的身份认证、商品目录管理、订单能力、消息与通知能力，以及可持续扩展的技术架构。

本文件基于对当前仓库的只读扫描整理而成。除 `README_ESTADO_ACTUAL.md` 外，没有修改任何现有项目文件。本次更新同样只修改了这个说明文件本身。

另外，需要特别说明：`Backend/backend-api` 当前正在进行从 `class-validator` 到 `typia` 的逐步迁移。

## 2. 当前整体状态概览

### 已实现或明显已经可用的部分

- 基于 NestJS + Fastify 的模块化后端。
- 完整的认证能力：注册、邮箱验证码验证、按角色登录、refresh token、会话管理、重置密码。
- PostgreSQL 作为主存储，Redis 用于缓存和会话相关场景。
- 基于角色与 CASL 的权限控制。
- 分类管理。
- 商品、商品变体与翻译管理。
- 文件上传与文件读取。
- 地理位置 API：国家、省、市、货币。
- 为批发商创建内部员工账号。
- 创建管理员账号。
- Kotlin Multiplatform + Compose Multiplatform 前端。
- 前后端已经在认证、分类、商品、文件、位置等方面有真实集成。
- 数据库模型已经覆盖购物车、订单、消息、通知、配送、文件等多个领域。

### 部分实现或仍在整理中的部分

- `typia` 迁移：项目里已经明显存在 `TypedRoute`、`TypedQuery`、`typia.createAssert(...)`、`MyTypedBody` 等新方式，但 `class-validator` 仍未完全移除。
- `standard` 前端模块已有认证与主结构，但业务覆盖明显少于 `enterprise`。
- `business` 更像复用的业务组件层，而不是一个完全独立收口的客户端。
- 云端部署方向已明确，但根目录 `docker-compose.yml` 当前只声明了 `cloudflared`。

### 目前仓库中还看不到完整闭环的目标

- 订单端到端完整流程。
- 内部聊天/消息系统在前后端的完整打通。
- 成熟的通知系统成品。
- 销售统计或分析看板。
- 地图与地理定位扩展功能。

## 3. 后端现状

## 3.1 后端总体架构

后端位于 `Backend/backend-api`，采用 NestJS 模块化设计。`AppModule` 中已经接入多个真实模块：

- `AuthModule`
- `LocationsModule`
- `CaslModule`
- `UserModule`
- `EnterpriseModule`
- `AdminModule`
- `CategoryModule`
- `ProductsModule`
- `FilesModule`
- `MailModule`
- `PrismaModule`
- `CacheRedisModule`
- `ScheduleTaskModule`
- `MyI18nModule`
- `MyThrottlerModule`

此外还能看到：

- 全局异常过滤器
- 统一响应拦截器
- Pino 日志
- JWT 统一配置

## 3.2 后端已经可见的功能

### 认证与会话

- 零售商注册
- 批发商注册
- 邮件验证码验证
- 按角色登录：
  - `standard`
  - `enterprise`
  - `admin`
- Web 登录下的 httpOnly refresh token cookie
- 普通客户端与 Web 端两套 refresh token 流程
- 密码重置
- 登出与删除指定会话
- 按设备管理会话

### 用户与企业组织能力

- 检查邮箱是否已存在
- 检查用户名是否已存在
- 带筛选与分页的用户查询
- `SUPERADMIN` 创建管理员
- 批发商创建员工：
  - 客服
  - 配送
  - 仓库

### 商品目录与商业后台能力

- 分类管理：
  - 创建
  - 列表
  - 搜索
  - 编辑
  - 删除
  - 层级分类
  - 公共/私有分类
  - 分类翻译
- 商品管理：
  - 创建
  - 列表
  - 详情
  - 编辑
  - 删除
  - 销售变体
  - 商品翻译
  - 商品文件关联

### 文件能力

- `multipart` 上传
- 真实 MIME 校验
- 安全文件名生成
- 商品文件读取
- 本地存储驱动
- Cloudflare R2 / S3 兼容存储驱动

### 地理位置能力

- 国家
- 国家下省份
- 省份下城市
- 根据 ISO 数值代码查询货币

## 3.3 后端技术栈与依赖

### 后端主技术栈

- NestJS 11
- Fastify
- Prisma
- PostgreSQL
- Redis
- JWT
- Passport
- CASL
- Swagger
- Pino
- BullMQ
- Nodemailer
- i18n
- typia
- nestia

### 在 `package.json` 中可见的核心依赖

- 框架与平台：
  - `@nestjs/common`
  - `@nestjs/core`
  - `@nestjs/platform-fastify`
  - `fastify`
- 配置与文档：
  - `@nestjs/config`
  - `@nestjs/swagger`
- 身份认证与安全：
  - `@nestjs/jwt`
  - `@nestjs/passport`
  - `passport`
  - `passport-jwt`
  - `passport-custom`
  - `bcrypt`
  - `@fastify/cookie`
  - `@fastify/secure-session`
- 数据库：
  - `@prisma/client`
  - `@prisma/adapter-pg`
  - `pg`
- 缓存与 Redis：
  - `@keyv/redis`
  - `keyv`
  - `ioredis`
  - `cache-manager`
  - `cache-manager-redis-store`
  - `@nest-lab/throttler-storage-redis`
- 校验与类型化：
  - `typia`
  - `@nestia/core`
  - `@nestia/sdk`
  - `@nestia/fetcher`
  - `@nestia/e2e`
  - `class-validator`
  - `class-transformer`
- 权限：
  - `@casl/ability`
  - `@casl/prisma`
- 文件与存储：
  - `@aws-sdk/client-s3`
  - `@aws-sdk/lib-storage`
  - `@fastify/multipart`
  - `file-type`
  - `mime-types`
- 邮件与队列：
  - `@nestjs-modules/mailer`
  - `@nestjs/bullmq`
  - `bullmq`
  - `handlebars`
  - `nodemailer`
- 日志与其它能力：
  - `nestjs-pino`
  - `pino`
  - `pino-pretty`
  - `nestjs-i18n`
  - `@nestjs/schedule`
  - `libphonenumber-js`
  - `piscina`

### 后端开发工具

- TypeScript
- ESLint
- Prettier
- Jest
- ts-jest
- SWC
- Prisma CLI
- ts-patch

## 3.4 `class-validator -> typia` 迁移状态

这个迁移不是计划阶段，而是已经进入实际执行阶段。

### 可见证据

- 依赖中存在 `typia` 与 `nestia`
- 控制器中出现 `TypedRoute`、`TypedQuery`
- 存在 `MyTypedBody`
- 多处使用 `typia.createAssert(...)`

### 当前判断

- 一部分接口已经迁移
- `class-validator` 与 `class-transformer` 仍然保留
- 因此当前应视为“迁移进行中”，而不是“迁移完成”

## 3.5 后端开发规范：校验与数据清洗

为了保证系统行为一致，同时避免不必要的副作用，后端需要明确区分“用户输入字段”和“系统输入字段”。

### 用户输入字段

凡是由用户直接填写、具有自由文本或半自由文本特征的字段，都应当在处理前或处理过程中进行数据清洗。典型例子包括：

- `name`
- `companyName`
- `description`
- `title`
- 其他类似的可见文本字段

对这类字段进行清洗的目的包括：

- 降低脏数据和无意义格式差异
- 提高存储一致性
- 提高展示质量
- 减少由于多余空格、异常字符、明显错误输入带来的问题

### 系统输入字段

凡是表示设备信息、技术元数据、协议字段或系统上下文字段的内容，不应按“用户自由文本”的方式进行语义清洗。典型例子包括：

- `deviceName`
- `langCode`
- `timezone`

这类字段应视为系统字段，因此：

- 不应被激进改写
- 不应像普通文本那样随意归一化
- 很多情况下应尽量保留原值，以保证可追踪性和兼容性

### 创建类字段的校验规则

凡是用于创建资源、账号、业务记录的字段，都必须进行校验。但校验设计必须保证：即使输入错误，也不能让系统内部逻辑出错。

这意味着：

- 创建类输入必须校验
- 错误输入应被安全拒绝或可控处理
- 不能因为输入错误而破坏业务逻辑、持久化逻辑或授权逻辑

### 系统字段的校验规则

对于由系统、客户端技术层或运行时环境提供的字段，应采用更宽松的校验策略，必要时可以不进行与用户文本同等级别的严格校验。

原因在于：

- 这些字段本质上更接近技术元数据
- 不同平台之间可能存在合理差异
- 过严校验容易破坏兼容性，却不一定带来实际收益

可以概括为：

- 用户字段：需要数据清洗 + 合理校验
- 创建字段：必须校验
- 系统字段：不做语义清洗，校验可以放宽或按兼容性处理

## 4. 数据库现状

`Base_de_datos` 与 `prisma/schema.prisma` 显示数据库设计已经非常完整。

### 已存在的主要实体

- 用户
- 配置
- 地址
- 用户会话
- 验证 token
- 分类与分类翻译
- 商品与商品变体
- 商品分类关系
- 文件
- 购物车
- 订单与订单明细
- 折扣
- 配送与配送时间线
- 聊天
- 消息
- 通知
- 国家
- 省份
- 城市
- 货币

### 技术观察

- 多张表启用了 Row Level Security
- 索引、约束和关联关系较完整
- 存在根据角色自动生成 `user_id` 的逻辑
- 数据库模型的推进程度甚至领先于部分前端或 API 暴露层

## 5. 前端现状

前端位于 `Frontend/Maian`，使用 Kotlin Multiplatform 与 Compose Multiplatform。

### 已发现的模块

- `shared`
- `standard`
- `enterprise`
- `admin`
- `business`
- `iosApp`

### 已发现的目标平台

- Android
- Desktop
- iOS
- Web
- Wasm

## 5.1 各模块功能状态

### `shared`

这是整个客户端体系的共享基础层，包含：

- 多平台 HTTP 客户端
- 各平台 token 存储
- 共享仓库：
  - auth
  - category
  - products
  - file
  - location
  - user
- 共享导航
- 共享主题
- 语言与本地化
- 时区管理
- 可复用 UI 组件
- 表单校验与工具
- 多平台文件上传支持

### `standard`

对应零售端或基础客户端。

目前能看到：

- 初始页
- 登录
- 注册
- Koin 依赖注入
- 导航结构
- 多平台入口

### `enterprise`

这是当前最成熟的业务客户端，明显偏向批发商/企业管理端。

可见功能：

- 登录
- 注册
- 初始页
- 分类列表
- 创建分类
- 编辑分类
- 商品列表
- 创建商品
- 商品表格/瀑布流视图
- 对接认证、分类、商品 API

### `admin`

管理员客户端目前可见：

- 登录
- 初始页
- 分类管理
- 用户仓库
- 面向批发商的数据对象
- 独立导航和依赖注入

### `business`

这是复用型业务层，包含：

- 分类 API 与仓库
- 分类表单
- 分类列表
- 富文本编辑器
- 媒体选择与媒体管理

## 5.2 前端技术栈与依赖

这一部分整合了当前代码中的实际依赖，以及根目录 `Readme.md` 中已有的前端说明内容。

### 前端主技术栈

- Kotlin Multiplatform
- Compose Multiplatform
- Jetpack Compose for Android
- Koin
- Kotlinx Serialization
- Kotlinx DateTime
- Kotlin Coroutines
- Ktor Client
- Multiplatform Settings
- Navigation Compose / Navigation 3
- 多平台 WebView
- Coil 3
- FileKit
- Haze
- libphonenumber
- ZXing / KScan
- Rich Editor Compose

### `gradle/libs.versions.toml` 中当前声明的重要版本

- Kotlin: `2.3.20`
- Compose Multiplatform: `1.10.3`
- Android Gradle Plugin: `8.12.3`
- Android Compile SDK: `36`
- Android Min SDK: `24`
- Android Target SDK: `35`
- Ktor: `3.4.1`
- Koin: `4.2.0`
- Kotlinx Serialization JSON: `1.10.0`
- Kotlinx DateTime: `0.7.1`
- Coroutines: `1.10.2`

### 前端关键依赖

- 多平台基础：
  - `org.jetbrains.kotlin.multiplatform`
  - `org.jetbrains.compose`
  - `org.jetbrains.kotlin.plugin.compose`
  - `org.jetbrains.kotlin.plugin.serialization`
- Android：
  - `androidx.activity:activity-compose`
  - lifecycle runtime / viewmodel
  - `androidx.security:security-crypto`
- 网络：
  - `io.ktor:ktor-client-core`
  - `ktor-client-content-negotiation`
  - `ktor-serialization-kotlinx-json`
  - `ktor-client-auth`
  - `ktor-client-logging`
  - `okhttp`
  - `darwin`
  - `cio`
  - `js`
- 依赖注入：
  - `io.insert-koin:koin-core`
  - `io.insert-koin:koin-compose-viewmodel`
- 图片、媒体与文件：
  - `io.coil-kt.coil3:coil-compose`
  - `coil-network-ktor3`
  - `io.github.vinceglb:filekit-core`
  - `filekit-dialogs-compose`
  - `filekit-coil`
  - `network.chaintech:compose-multiplatform-media-player`
  - `io.github.kdroidfilter:composemediaplayer`
- UI 与体验增强：
  - `dev.chrisbanes.haze:haze`
  - `compose-placeholder-material3`
  - `compose-pipette`
  - `reorderable`
  - `datatable-material3`
  - `table-core`
  - `sonner`
  - `zoomable`
- 导航：
  - `navigation-compose`
  - `navigation3-runtime`
  - `navigation3-ui`
- 通用能力：
  - `kotlinx-datetime`
  - `kotlinx-serialization-json`
  - `kotlinx-collections-immutable`
  - `multiplatform-settings`
  - `libphonenumber`
  - `jmail`
- 扫码与条码：
  - `KScan`
  - `zxing`
  - `zxing-android-embedded`
- WebView 与网页支持：
  - `compose-webview-multiplatform`
- 富文本：
  - `richeditor-compose`

## 5.3 从根目录 `Readme.md` 合并进来的补充信息

根目录原有 `Readme.md` 还提供了前端方面的补充说明，现已并入本文件，主要包括：

- 前端基于 Kotlin Multiplatform 与 Compose Multiplatform 构建
- 面向 Android、iOS、Web 与桌面平台
- 使用 Compose Icons 作为图标资源来源
- 使用 MiSans 字体作为 UI 字体资源的一部分
- 前端依赖覆盖 JetBrains、AndroidX、Google 以及多个第三方多平台库

换句话说，前端不只是“多平台框架已搭好”，而是已经具备：

- 现代 UI 架构
- 多平台网络访问
- 多平台存储
- 多平台媒体与文件能力
- 导航与可复用组件体系
- 本地化、多语言与时区支持

## 6. 基础设施与部署

### 仓库中能看到的内容

- `docker-compose.yml`
- `cloudflared/`
- `config.yml`
- backend 的 `Dockerfile`
- `.env` 与示例配置

### 当前判断

- 根目录 `docker-compose.yml` 目前只包含 `cloudflared`
- backend 具备云部署结构
- 项目提案中提到的 Supabase、Redis、Northflank、Cloudflare R2 等方向，与仓库结构是匹配的，但本地一键编排尚未完全体现

## 7. 与原始项目目标的对应关系

### 推进较强的目标

- B2B 多角色平台基础
- NestJS + Fastify 模块化后端
- PostgreSQL 主存储
- Redis 会话/缓存
- 商品与分类管理
- 认证与权限控制
- 多媒体文件能力
- Kotlin Multiplatform 多端客户端基础

### 部分推进的目标

- 批发商与零售商之间的完整商业流程
- 员工与组织管理的进一步完善
- 零售端客户端的业务完成度
- 订单在客户端与服务端的完整闭环

### 当前仓库中还未清晰闭合的目标

- 完整订单流程
- 内部消息系统
- 通知系统成品
- 销售统计看板

---

# Anexo / 附录 A - Dependencias del Backend

Formato:

- Nombre del paquete
- Version declarada
- Enlace de acceso
- Direccion del paquete

## A.1 Dependencias de runtime

- `@aws-sdk/client-s3` - `^3.1014.0` - [npm](https://www.npmjs.com/package/%40aws-sdk%2Fclient-s3) - `@aws-sdk/client-s3`
- `@aws-sdk/lib-storage` - `^3.1014.0` - [npm](https://www.npmjs.com/package/%40aws-sdk%2Flib-storage) - `@aws-sdk/lib-storage`
- `@casl/ability` - `^6.8.0` - [npm](https://www.npmjs.com/package/%40casl%2Fability) - `@casl/ability`
- `@casl/prisma` - `^1.6.1` - [npm](https://www.npmjs.com/package/%40casl%2Fprisma) - `@casl/prisma`
- `@fastify/cookie` - `^11.0.2` - [npm](https://www.npmjs.com/package/%40fastify%2Fcookie) - `@fastify/cookie`
- `@fastify/multipart` - `^9.4.0` - [npm](https://www.npmjs.com/package/%40fastify%2Fmultipart) - `@fastify/multipart`
- `@fastify/secure-session` - `8.2.0` - [npm](https://www.npmjs.com/package/%40fastify%2Fsecure-session) - `@fastify/secure-session`
- `@fastify/static` - `^8.3.0` - [npm](https://www.npmjs.com/package/%40fastify%2Fstatic) - `@fastify/static`
- `@keyv/redis` - `^5.1.6` - [npm](https://www.npmjs.com/package/%40keyv%2Fredis) - `@keyv/redis`
- `@nestia/core` - `^11.0.1` - [npm](https://www.npmjs.com/package/%40nestia%2Fcore) - `@nestia/core`
- `@nestia/e2e` - `^11.0.1` - [npm](https://www.npmjs.com/package/%40nestia%2Fe2e) - `@nestia/e2e`
- `@nestia/fetcher` - `^11.0.1` - [npm](https://www.npmjs.com/package/%40nestia%2Ffetcher) - `@nestia/fetcher`
- `@nestia/sdk` - `^11.0.1` - [npm](https://www.npmjs.com/package/%40nestia%2Fsdk) - `@nestia/sdk`
- `@nestjs-modules/mailer` - `^2.3.0` - [npm](https://www.npmjs.com/package/%40nestjs-modules%2Fmailer) - `@nestjs-modules/mailer`
- `@nestjs/bullmq` - `^11.0.4` - [npm](https://www.npmjs.com/package/%40nestjs%2Fbullmq) - `@nestjs/bullmq`
- `@nestjs/cache-manager` - `^3.1.0` - [npm](https://www.npmjs.com/package/%40nestjs%2Fcache-manager) - `@nestjs/cache-manager`
- `@nestjs/common` - `^11.1.17` - [npm](https://www.npmjs.com/package/%40nestjs%2Fcommon) - `@nestjs/common`
- `@nestjs/config` - `^4.0.3` - [npm](https://www.npmjs.com/package/%40nestjs%2Fconfig) - `@nestjs/config`
- `@nestjs/core` - `^11.1.17` - [npm](https://www.npmjs.com/package/%40nestjs%2Fcore) - `@nestjs/core`
- `@nestjs/jwt` - `^11.0.2` - [npm](https://www.npmjs.com/package/%40nestjs%2Fjwt) - `@nestjs/jwt`
- `@nestjs/mapped-types` - `^2.1.0` - [npm](https://www.npmjs.com/package/%40nestjs%2Fmapped-types) - `@nestjs/mapped-types`
- `@nestjs/passport` - `^11.0.5` - [npm](https://www.npmjs.com/package/%40nestjs%2Fpassport) - `@nestjs/passport`
- `@nestjs/platform-express` - `^11.1.17` - [npm](https://www.npmjs.com/package/%40nestjs%2Fplatform-express) - `@nestjs/platform-express`
- `@nestjs/platform-fastify` - `^11.1.17` - [npm](https://www.npmjs.com/package/%40nestjs%2Fplatform-fastify) - `@nestjs/platform-fastify`
- `@nestjs/schedule` - `^6.1.1` - [npm](https://www.npmjs.com/package/%40nestjs%2Fschedule) - `@nestjs/schedule`
- `@nestjs/swagger` - `^11.2.6` - [npm](https://www.npmjs.com/package/%40nestjs%2Fswagger) - `@nestjs/swagger`
- `@nestjs/throttler` - `^6.5.0` - [npm](https://www.npmjs.com/package/%40nestjs%2Fthrottler) - `@nestjs/throttler`
- `@nest-lab/throttler-storage-redis` - `^1.2.0` - [npm](https://www.npmjs.com/package/%40nest-lab%2Fthrottler-storage-redis) - `@nest-lab/throttler-storage-redis`
- `@prisma/adapter-pg` - `^7.5.0` - [npm](https://www.npmjs.com/package/%40prisma%2Fadapter-pg) - `@prisma/adapter-pg`
- `@prisma/client` - `^7.5.0` - [npm](https://www.npmjs.com/package/%40prisma%2Fclient) - `@prisma/client`
- `@prisma/client-runtime-utils` - `^7.5.0` - [npm](https://www.npmjs.com/package/%40prisma%2Fclient-runtime-utils) - `@prisma/client-runtime-utils`
- `bcrypt` - `^6.0.0` - [npm](https://www.npmjs.com/package/bcrypt) - `bcrypt`
- `bullmq` - `^5.71.0` - [npm](https://www.npmjs.com/package/bullmq) - `bullmq`
- `cache-manager` - `^7.2.8` - [npm](https://www.npmjs.com/package/cache-manager) - `cache-manager`
- `cache-manager-redis-store` - `^3.0.1` - [npm](https://www.npmjs.com/package/cache-manager-redis-store) - `cache-manager-redis-store`
- `class-transformer` - `^0.5.1` - [npm](https://www.npmjs.com/package/class-transformer) - `class-transformer`
- `class-validator` - `^0.14.4` - [npm](https://www.npmjs.com/package/class-validator) - `class-validator`
- `fastify` - `^5.8.2` - [npm](https://www.npmjs.com/package/fastify) - `fastify`
- `file-type` - `^21.3.3` - [npm](https://www.npmjs.com/package/file-type) - `file-type`
- `handlebars` - `^4.7.8` - [npm](https://www.npmjs.com/package/handlebars) - `handlebars`
- `ioredis` - `^5.10.1` - [npm](https://www.npmjs.com/package/ioredis) - `ioredis`
- `keyv` - `^5.6.0` - [npm](https://www.npmjs.com/package/keyv) - `keyv`
- `libphonenumber-js` - `^1.12.40` - [npm](https://www.npmjs.com/package/libphonenumber-js) - `libphonenumber-js`
- `lru-cache` - `^11.2.7` - [npm](https://www.npmjs.com/package/lru-cache) - `lru-cache`
- `mime-types` - `^3.0.2` - [npm](https://www.npmjs.com/package/mime-types) - `mime-types`
- `nestjs-i18n` - `^10.6.0` - [npm](https://www.npmjs.com/package/nestjs-i18n) - `nestjs-i18n`
- `nestjs-pino` - `^4.6.1` - [npm](https://www.npmjs.com/package/nestjs-pino) - `nestjs-pino`
- `nodemailer` - `7.0.11` - [npm](https://www.npmjs.com/package/nodemailer) - `nodemailer`
- `passport` - `0.7.0` - [npm](https://www.npmjs.com/package/passport) - `passport`
- `passport-custom` - `^1.1.1` - [npm](https://www.npmjs.com/package/passport-custom) - `passport-custom`
- `passport-jwt` - `^4.0.1` - [npm](https://www.npmjs.com/package/passport-jwt) - `passport-jwt`
- `pg` - `^8.20.0` - [npm](https://www.npmjs.com/package/pg) - `pg`
- `pino` - `^9.14.0` - [npm](https://www.npmjs.com/package/pino) - `pino`
- `pino-pretty` - `^13.1.3` - [npm](https://www.npmjs.com/package/pino-pretty) - `pino-pretty`
- `piscina` - `^5.1.4` - [npm](https://www.npmjs.com/package/piscina) - `piscina`
- `reflect-metadata` - `^0.2.2` - [npm](https://www.npmjs.com/package/reflect-metadata) - `reflect-metadata`
- `rxjs` - `^7.8.2` - [npm](https://www.npmjs.com/package/rxjs) - `rxjs`
- `typia` - `^12.0.1` - [npm](https://www.npmjs.com/package/typia) - `typia`

## A.2 Dependencias de desarrollo

- `@eslint/eslintrc` - `^3.3.5` - [npm](https://www.npmjs.com/package/%40eslint%2Feslintrc) - `@eslint/eslintrc`
- `@eslint/js` - `^9.39.4` - [npm](https://www.npmjs.com/package/%40eslint%2Fjs) - `@eslint/js`
- `@nestia/benchmark` - `^11.0.1` - [npm](https://www.npmjs.com/package/%40nestia%2Fbenchmark) - `@nestia/benchmark`
- `@nestjs/cli` - `^11.0.16` - [npm](https://www.npmjs.com/package/%40nestjs%2Fcli) - `@nestjs/cli`
- `@nestjs/schematics` - `^11.0.9` - [npm](https://www.npmjs.com/package/%40nestjs%2Fschematics) - `@nestjs/schematics`
- `@nestjs/testing` - `^11.1.17` - [npm](https://www.npmjs.com/package/%40nestjs%2Ftesting) - `@nestjs/testing`
- `@swc/cli` - `^0.7.10` - [npm](https://www.npmjs.com/package/%40swc%2Fcli) - `@swc/cli`
- `@swc/core` - `^1.15.21` - [npm](https://www.npmjs.com/package/%40swc%2Fcore) - `@swc/core`
- `@types/bcrypt` - `^6.0.0` - [npm](https://www.npmjs.com/package/%40types%2Fbcrypt) - `@types/bcrypt`
- `@types/express` - `^5.0.6` - [npm](https://www.npmjs.com/package/%40types%2Fexpress) - `@types/express`
- `@types/jest` - `^30.0.0` - [npm](https://www.npmjs.com/package/%40types%2Fjest) - `@types/jest`
- `@types/mime-types` - `^3.0.1` - [npm](https://www.npmjs.com/package/%40types%2Fmime-types) - `@types/mime-types`
- `@types/node` - `^24.12.0` - [npm](https://www.npmjs.com/package/%40types%2Fnode) - `@types/node`
- `@types/nodemailer` - `^7.0.11` - [npm](https://www.npmjs.com/package/%40types%2Fnodemailer) - `@types/nodemailer`
- `@types/passport` - `^1.0.17` - [npm](https://www.npmjs.com/package/%40types%2Fpassport) - `@types/passport`
- `@types/passport-jwt` - `^4.0.1` - [npm](https://www.npmjs.com/package/%40types%2Fpassport-jwt) - `@types/passport-jwt`
- `@types/supertest` - `^6.0.3` - [npm](https://www.npmjs.com/package/%40types%2Fsupertest) - `@types/supertest`
- `@typescript-eslint/eslint-plugin` - `^8.57.1` - [npm](https://www.npmjs.com/package/%40typescript-eslint%2Feslint-plugin) - `@typescript-eslint/eslint-plugin`
- `@typescript-eslint/parser` - `^8.57.1` - [npm](https://www.npmjs.com/package/%40typescript-eslint%2Fparser) - `@typescript-eslint/parser`
- `dotenv` - `^17.3.1` - [npm](https://www.npmjs.com/package/dotenv) - `dotenv`
- `eslint` - `^9.39.4` - [npm](https://www.npmjs.com/package/eslint) - `eslint`
- `eslint-config-prettier` - `^10.1.8` - [npm](https://www.npmjs.com/package/eslint-config-prettier) - `eslint-config-prettier`
- `eslint-plugin-prettier` - `^5.5.5` - [npm](https://www.npmjs.com/package/eslint-plugin-prettier) - `eslint-plugin-prettier`
- `globals` - `^16.5.0` - [npm](https://www.npmjs.com/package/globals) - `globals`
- `jest` - `^30.3.0` - [npm](https://www.npmjs.com/package/jest) - `jest`
- `nestia` - `^11.0.1` - [npm](https://www.npmjs.com/package/nestia) - `nestia`
- `prettier` - `^3.8.1` - [npm](https://www.npmjs.com/package/prettier) - `prettier`
- `prisma` - `^7.5.0` - [npm](https://www.npmjs.com/package/prisma) - `prisma`
- `source-map-support` - `^0.5.21` - [npm](https://www.npmjs.com/package/source-map-support) - `source-map-support`
- `supertest` - `^7.2.2` - [npm](https://www.npmjs.com/package/supertest) - `supertest`
- `ts-jest` - `^29.4.6` - [npm](https://www.npmjs.com/package/ts-jest) - `ts-jest`
- `ts-loader` - `^9.5.4` - [npm](https://www.npmjs.com/package/ts-loader) - `ts-loader`
- `ts-node` - `^10.9.2` - [npm](https://www.npmjs.com/package/ts-node) - `ts-node`
- `ts-patch` - `^3.3.0` - [npm](https://www.npmjs.com/package/ts-patch) - `ts-patch`
- `tsconfig-paths` - `^4.2.0` - [npm](https://www.npmjs.com/package/tsconfig-paths) - `tsconfig-paths`
- `typescript` - `~5.9.3` - [npm](https://www.npmjs.com/package/typescript) - `typescript`
- `typescript-eslint` - `^8.57.1` - [npm](https://www.npmjs.com/package/typescript-eslint) - `typescript-eslint`

---

# Anexo / 附录 B - Dependencias del Frontend

Formato:

- Alias o plugin
- Coordenada de libreria o id de plugin
- Version
- Enlace de acceso

## B.1 Librerias declaradas en `libs.versions.toml`

- `alert-kmp` - `io.github.khubaibkhan4:alert-kmp` - `2.0.0` - [Maven Central](https://search.maven.org/artifact/io.github.khubaibkhan4/alert-kmp)
- `androidx-navigation3-runtime` - `androidx.navigation3:navigation3-runtime` - `1.1.0-beta01` - [Maven Central](https://search.maven.org/artifact/androidx.navigation3/navigation3-runtime)
- `androidx-paging-common` - `androidx.paging:paging-common` - `3.4.2` - [Maven Central](https://search.maven.org/artifact/androidx.paging/paging-common)
- `androidx-paging-compose` - `androidx.paging:paging-compose` - `3.4.2` - [Maven Central](https://search.maven.org/artifact/androidx.paging/paging-compose)
- `androidx-security-crypto` - `androidx.security:security-crypto` - `1.1.0` - [AndroidX](https://developer.android.com/jetpack/androidx/releases/security)
- `coil-compose` - `io.coil-kt.coil3:coil-compose` - `3.4.0` - [Coil](https://github.com/coil-kt/coil)
- `coil-network-ktor3` - `io.coil-kt.coil3:coil-network-ktor3` - `3.4.0` - [Coil](https://github.com/coil-kt/coil)
- `compose-multiplatform-media-player` - `network.chaintech:compose-multiplatform-media-player` - `1.0.53` - [Maven Central](https://search.maven.org/artifact/network.chaintech/compose-multiplatform-media-player)
- `compose-placeholder-material3` - `com.eygraber:compose-placeholder-material3` - `1.0.12` - [GitHub](https://github.com/eygraber/compose-placeholder)
- `compose-webview-multiplatform` - `io.github.kevinnzou:compose-webview-multiplatform` - `2.0.3` - [GitHub](https://github.com/KevinnZou/compose-webview-multiplatform)
- `composePipette` - `dev.zt64.compose.pipette:compose-pipette` - `2.0.0` - [Maven Central](https://search.maven.org/artifact/dev.zt64.compose.pipette/compose-pipette)
- `composemediaplayer` - `io.github.kdroidfilter:composemediaplayer` - `0.8.7` - [Maven Central](https://search.maven.org/artifact/io.github.kdroidfilter/composemediaplayer)
- `compottie-lite` - `io.github.alexzhirkevich:compottie-lite` - `2.1.0` - [GitHub](https://github.com/alexzhirkevich/compottie)
- `datatable-material3` - `com.seanproctor:datatable-material3` - `0.12.0` - [GitHub](https://github.com/SeamProctor/compose-data-table)
- `filekit-coil` - `io.github.vinceglb:filekit-coil` - `0.13.0` - [GitHub](https://github.com/vinceglb/FileKit)
- `filekit-core` - `io.github.vinceglb:filekit-core` - `0.13.0` - [GitHub](https://github.com/vinceglb/FileKit)
- `filekit-dialogs-compose` - `io.github.vinceglb:filekit-dialogs-compose` - `0.13.0` - [GitHub](https://github.com/vinceglb/FileKit)
- `haze` - `dev.chrisbanes.haze:haze` - `1.7.2` - [GitHub](https://github.com/chrisbanes/haze)
- `jmail` - `com.sanctionco.jmail:jmail` - `2.1.0` - [GitHub](https://github.com/RohanNagar/jmail)
- `koin-compose-viewmodel` - `io.insert-koin:koin-compose-viewmodel` - `4.2.0` - [GitHub](https://github.com/InsertKoinIO/koin)
- `koin-core` - `io.insert-koin:koin-core` - `4.2.0` - [GitHub](https://github.com/InsertKoinIO/koin)
- `kotlin-test` - `org.jetbrains.kotlin:kotlin-test` - `2.3.20` - [Kotlin](https://kotlinlang.org/api/core/kotlin-test/)
- `androidx-activity-compose` - `androidx.activity:activity-compose` - `1.13.0` - [AndroidX](https://developer.android.com/jetpack/androidx/releases/activity)
- `androidx-lifecycle-viewmodel` - `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` - `2.10.0` - [JetBrains AndroidX](https://github.com/JetBrains/androidx)
- `androidx-lifecycle-runtimeCompose` - `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` - `2.10.0` - [JetBrains AndroidX](https://github.com/JetBrains/androidx)
- `kotlinx-collections-immutable` - `org.jetbrains.kotlinx:kotlinx-collections-immutable` - `0.4.0` - [GitHub](https://github.com/Kotlin/kotlinx.collections.immutable)
- `kotlinx-coroutinesSwing` - `org.jetbrains.kotlinx:kotlinx-coroutines-swing` - `1.10.2` - [GitHub](https://github.com/Kotlin/kotlinx.coroutines)
- `kotlinx-datetime` - `org.jetbrains.kotlinx:kotlinx-datetime` - `0.7.1` - [GitHub](https://github.com/Kotlin/kotlinx-datetime)
- `kotlinx-serialization-json` - `org.jetbrains.kotlinx:kotlinx-serialization-json` - `1.10.0` - [GitHub](https://github.com/Kotlin/kotlinx.serialization)
- `kmp-navigation-compose` - `org.jetbrains.androidx.navigation:navigation-compose` - `2.9.2` - [JetBrains KMP Navigation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation.html)
- `kscan` - `io.github.ismai117:KScan` - `0.8.1` - [GitHub](https://github.com/ismai117/KScan)
- `libphonenumber` - `io.github.luca992.libphonenumber-kotlin:libphonenumber` - `0.1.9` - [GitHub](https://github.com/luca992/libphonenumber-kotlin)
- `material-icons-core` - `org.jetbrains.compose.material:material-icons-core` - `1.7.3` - [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- `material-icons-extended` - `org.jetbrains.compose.material:material-icons-extended` - `1.7.3` - [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- `jetbrains-navigation3-ui` - `org.jetbrains.androidx.navigation3:navigation3-ui` - `1.0.0-alpha06` - [JetBrains AndroidX](https://github.com/JetBrains/androidx)
- `jetbrains-material3-adaptiveNavigation3` - `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` - `1.3.0-alpha06` - [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- `jetbrains-lifecycle-viewmodelNavigation3` - `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` - `2.10.0` - [JetBrains AndroidX](https://github.com/JetBrains/androidx)
- `journeyapps-zxing-android-embedded` - `com.journeyapps:zxing-android-embedded` - `4.3.0` - [GitHub](https://github.com/journeyapps/zxing-android-embedded)
- `reorderable` - `sh.calvin.reorderable:reorderable` - `3.0.0` - [GitHub](https://github.com/Calvin-LL/Reorderable)
- `richeditor-compose` - `com.mohamedrejeb.richeditor:richeditor-compose` - `1.0.0-rc13` - [GitHub](https://github.com/MohamedRejeb/compose-rich-editor)
- `russhwolf-multiplatform-settings` - `com.russhwolf:multiplatform-settings` - `1.3.0` - [GitHub](https://github.com/russhwolf/multiplatform-settings)
- `webcam-capture` - `com.github.sarxos:webcam-capture` - `0.3.12` - [GitHub](https://github.com/sarxos/webcam-capture)
- `zxing-core` - `com.google.zxing:core` - `3.5.4` - [GitHub](https://github.com/zxing/zxing)
- `zxing-javase` - `com.google.zxing:javase` - `3.5.4` - [GitHub](https://github.com/zxing/zxing)
- `ktor-client-core` - `io.ktor:ktor-client-core` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-content-negotiation` - `io.ktor:ktor-client-content-negotiation` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-serialization-kotlinx-json` - `io.ktor:ktor-serialization-kotlinx-json` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-logging` - `io.ktor:ktor-client-logging` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-auth` - `io.ktor:ktor-client-auth` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-okhttp` - `io.ktor:ktor-client-okhttp` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-darwin` - `io.ktor:ktor-client-darwin` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-cio` - `io.ktor:ktor-client-cio` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `ktor-client-js` - `io.ktor:ktor-client-js` - `3.4.1` - [Ktor](https://github.com/ktorio/ktor)
- `sonner` - `io.github.dokar3:sonner` - `0.3.9` - [GitHub](https://github.com/dokar3/sonner)
- `table-core` - `ua.wwind.table-kmp:table-core` - `1.7.15` - [Maven Central](https://search.maven.org/artifact/ua.wwind.table-kmp/table-core)
- `ui-tooling` - `org.jetbrains.compose.ui:ui-tooling` - `1.10.3` - [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- `zoomable` - `net.engawapg.lib:zoomable` - `2.11.1` - [GitHub](https://github.com/engawapg/zoomable)

## B.2 Plugins declarados

- `androidApplication` - `com.android.application` - `8.12.3` - [Android Gradle Plugin](https://developer.android.com/build/releases/gradle-plugin)
- `androidLibrary` - `com.android.library` - `8.12.3` - [Android Gradle Plugin](https://developer.android.com/build/releases/gradle-plugin)
- `composeMultiplatform` - `org.jetbrains.compose` - `1.10.3` - [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- `composeCompiler` - `org.jetbrains.kotlin.plugin.compose` - `2.3.20` - [Kotlin](https://kotlinlang.org/)
- `kotlinMultiplatform` - `org.jetbrains.kotlin.multiplatform` - `2.3.20` - [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- `kotlinxSerialization` - `org.jetbrains.kotlin.plugin.serialization` - `2.3.20` - [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization)

## B.3 Versiones globales importantes

- `agp` - `8.12.3`
- `android-compileSdk` - `36`
- `android-minSdk` - `24`
- `android-targetSdk` - `35`
- `kotlin` - `2.3.20`
- `composeMultiplatform` - `1.10.3`
- `ktor` - `3.4.1`
- `koinCore` - `4.2.0`
- `kotlinx-coroutines` - `1.10.2`
- `kotlinxDatetime` - `0.7.1`
- `kotlinxSerializationJson` - `1.10.0`
