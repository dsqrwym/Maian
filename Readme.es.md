# MaiAn

> *Generado por **Antigravity** (Google DeepMind AI), basado en el análisis del repositorio y la memoria del proyecto.*

**Leer en otro idioma:** [English](./Readme.md) · [中文](./Readme.zh.md)

---

MaiAn es una **plataforma B2B multiplataforma** que conecta distribuidores mayoristas con minoristas. Combina aplicaciones cliente nativas para distintos perfiles de usuario con un backend centralizado, seguro y escalable, diseñado para crecer con nuevas funciones de negocio.

El proyecto está desarrollado por **dsqrwym** (identificador técnico) / **MaiAn** (nombre de marca).

---

## Índice

- [Estado actual](#estado-actual)
- [Backend](#backend)
- [Base de datos](#base-de-datos)
- [Frontend](#frontend)
- [Infraestructura y despliegue](#infraestructura-y-despliegue)
- [Referencia de dependencias — Backend](#referencia-de-dependencias--backend)
- [Referencia de dependencias — Frontend](#referencia-de-dependencias--frontend)

---

## Estado actual

### Ya implementado y operativo

- Backend modular con NestJS 11 y Fastify, desplegable en modo `single`, `cluster` o `pm2`.
- Autenticación completa: registro, verificación por correo, login por roles (`standard`, `enterprise`, `admin`), refresh token con rotación, gestión de sesión por dispositivo y reseteo de contraseña.
- PostgreSQL 17 como persistencia principal. Dos instancias Redis: una para caché / sesiones / rate-limit y otra para colas BullMQ.
- Control de acceso por roles con CASL.
- Gestión completa de categorías: jerárquicas, públicas/privadas, con traducciones multilingüe.
- Gestión completa de productos: variantes de venta, traducciones multilingüe, ficheros asociados.
- Subida y recuperación de archivos con driver local y driver compatible con Cloudflare R2 / S3. Procesamiento de imágenes con `sharp`; generación de documentos PDF con `pdfmake`.
- API de localización: países, provincias, ciudades y monedas ISO numéricas.
- Creación de empleados internos para mayoristas (soporte, reparto, almacén).
- Creación de administradores por `SUPERADMIN`.
- Infraestructura Docker Compose completa: backend, PostgreSQL 17, dos Redis y Cloudflare Tunnel opcional.
- Frontend Kotlin Multiplatform con módulos: `shared`, `standard`, `enterprise`, `admin`, `business`, `iosApp`.
- Integración frontend–backend en autenticación, categorías, productos, archivos y localización.
- Esquema de base de datos que ya contempla carritos, pedidos, mensajes, notificaciones, entregas y relaciones empresariales.

### Implementado de forma parcial o en consolidación

- **Módulo `standard`**: autenticación y estructura principal presentes, pero cobertura funcional visible menor que `enterprise`.
- **Módulo `business`**: capa de componentes de negocio reutilizables (categorías, media, editor enriquecido); no es un cliente independiente cerrado.

### Objetivos no cerrados en el repositorio

- Flujo completo de pedidos de extremo a extremo (API + clientes).
- Sistema de mensajería/chat plenamente conectado en el frontend.
- Sistema funcional de notificaciones a nivel de producto final.
- Paneles de estadísticas o históricos de ventas visibles en interfaz.
- Geolocalización y mapas (previsto para una ampliación futura).

---

## Backend

Ubicado en `Backend/backend-api`. Usa una arquitectura modular NestJS con Fastify como adaptador HTTP.

### Módulos activos en `AppModule`

`AuthModule` · `LocationsModule` · `CaslModule` · `UserModule` · `EnterpriseModule` · `AdminModule` · `CategoryModule` · `ProductsModule` · `FilesModule` · `MailModule` · `PrismaModule` · `CacheRedisModule` · `ScheduleTaskModule` · `MyI18nModule` · `MyThrottlerModule`

El backend incluye además filtros globales de excepciones, interceptor de respuesta unificado, logger estructurado con Pino y configuración JWT centralizada. Soporta tres modos de proceso: `single`, `cluster` nativo de Node y `pm2`.

### Funcionalidades

#### Autenticación y sesiones

- Registro de minoristas y mayoristas.
- Verificación por código enviado al correo.
- Login por perfil: `standard`, `enterprise`, `admin`.
- Cookie httpOnly para refresh token (flujo web) con rotación y CSRF.
- Reseteo de contraseña.
- Cierre de sesión y borrado de sesiones concretas.
- Gestión de sesión por dispositivo.

#### Gestión de usuarios y organización empresarial

- Comprobación de disponibilidad de email y username.
- Búsqueda de usuarios con filtros y paginación.
- Creación de administradores por `SUPERADMIN`.
- Creación de empleados de mayorista: soporte, reparto, almacén.

#### Catálogo

- **Categorías**: crear, listar, buscar, editar, eliminar, jerarquías, visibilidad pública/privada, traducciones multilingüe.
- **Productos**: crear, listar, ver detalle, editar, eliminar, variantes de venta, traducciones multilingüe, ficheros asociados.

#### Archivos

- Subida `multipart`.
- Validación del MIME real del fichero.
- Renombrado seguro.
- Driver de almacenamiento local.
- Driver compatible con Cloudflare R2 / S3 (`@aws-sdk`).
- Procesamiento de imágenes con `sharp`.
- Generación de documentos PDF con `pdfmake`.

#### Localización

- Países, provincias por país, ciudades por provincia.
- Monedas por código ISO numérico.

### Stack tecnológico

| Tecnología | Rol |
|---|---|
| NestJS 11 | Framework de aplicación |
| Fastify 5 | Adaptador HTTP de alto rendimiento |
| Prisma 7 | ORM principal y migraciones |
| Drizzle ORM | ORM alternativo (en uso parcial) |
| PostgreSQL 17 | Base de datos principal |
| Redis 7 | Caché, sesiones, rate-limit, colas BullMQ |
| JWT / Passport | Autenticación |
| CASL | Control de acceso por atributos |
| Swagger | Documentación de API |
| Pino | Logger estructurado |
| BullMQ | Colas de trabajos asíncronos |
| Nodemailer | Envío de correos |
| nestjs-i18n | Internacionalización |
| typia / nestia | Validación y serialización tipadas (migración desde class-validator completada) |
| sharp | Procesamiento de imágenes |
| pdfmake | Generación de documentos PDF |

### Norma de validación y saneamiento

El backend distingue dos tipos de campos:

- **Campos de usuario** (`name`, `companyName`, `description`, etc.): requieren saneamiento semántico + validación estricta antes de persistirse.
- **Campos de sistema** (`deviceName`, `langCode`, `timezone`, etc.): no deben reescribirse agresivamente; la validación es flexible o no semántica.

Un dato mal formado nunca debe romper la lógica de negocio, la persistencia ni la autorización.

---

## Base de datos

Gestionada principalmente con **Prisma 7** (ORM + migraciones) y **Drizzle ORM** en algunos módulos. El esquema SQL completo está en `Base_de_datos/schema.sql` y Docker Compose lo importa automáticamente al arrancar el contenedor de PostgreSQL.

### Entidades

Usuarios · Configuraciones · Direcciones · Sesiones de usuario · Tokens de verificación · Categorías (con traducciones) · Productos (con variantes) · Relaciones producto-categoría · Archivos · Carritos · Pedidos y detalle de pedidos · Descuentos · Entregas (con línea temporal) · Chats · Mensajes · Notificaciones · Países · Provincias · Ciudades · Monedas

### Aspectos técnicos

- Row Level Security habilitado en varias tablas.
- Índices, restricciones y relaciones bien definidas.
- Generación automática de `user_id` según rol.
- Datos de referencia precargados: monedas, países, provincias, ciudades y categorías base.
- El esquema de base de datos va por delante de algunas partes de la capa API y cliente.

---

## Frontend

Ubicado en `Frontend/Maian`. Construido con **Kotlin Multiplatform** y **Compose Multiplatform**.

### Plataformas objetivo

| Plataforma | Detalles |
|---|---|
| Android | minSdk 24, compileSdk 37 |
| iOS | Nativo a través del punto de entrada `iosApp` |
| Desktop | JVM / Swing |
| Web | Kotlin/Wasm |

### Módulos

| Módulo | Descripción |
|---|---|
| `shared` | Base común: cliente HTTP Ktor, almacenamiento de tokens, repositorios (auth, category, products, file, location, user), tema compartido, i18n, zona horaria, componentes reutilizables, subida de archivos |
| `standard` | Cliente minorista: login, registro, pantalla inicial, Koin DI, navegación |
| `enterprise` | Cliente mayorista (más maduro): login, registro, CRUD de categorías, CRUD de productos, vistas tabla/cascada |
| `admin` | Panel de administración: login, gestión de categorías, repositorio de usuarios, DI y navegación propias |
| `business` | Capa de negocio reutilizable: formularios y listas de categorías, editor de texto enriquecido, selector y gestor de medios |
| `iosApp` | Punto de entrada iOS (wrapper SwiftUI) |

### Recursos

- **Iconografía**: Compose Material Icons (Core + Extended).
- **Tipografía**: MiSans (incluida como recurso de fuente).

### Versiones principales

| Dependencia | Versión |
|---|---|
| Kotlin | `2.3.21` |
| Compose Multiplatform | `1.11.0` |
| Android Gradle Plugin | `9.2.1` |
| Android compileSdk | `37` |
| Android minSdk | `24` |
| Ktor | `3.5.0` |
| Koin | `4.2.1` |
| Kotlinx Coroutines | `1.11.0` |
| Kotlinx Serialization JSON | `1.11.0` |
| Kotlinx DateTime | `0.8.0` |
| Coil 3 | `3.4.0` |
| Haze | `2.0.0-alpha02` |

---

## Infraestructura y despliegue

### Docker Compose

`docker-compose.yml` usa **perfiles opcionales** para controlar qué servicios se inician:

| Servicio | Perfil | Descripción |
|---|---|---|
| `backend` | *(siempre activo)* | API NestJS; soporta modos `single`, `cluster`, `pm2` |
| `postgres` | `postgres`, `local-infra` | PostgreSQL 17; importa automáticamente todos los SQL de `Base_de_datos/` |
| `redis-cache` | `redis`, `local-infra` | Redis 7 para caché, sesiones y rate-limit |
| `redis-bull` | `redis`, `local-infra` | Redis 7 dedicado a colas BullMQ |
| `cloudflared` | `cloudflared` | Cloudflare Tunnel para exposición pública segura |

Inicio rápido (entorno local completo, sin túnel):

```bash
COMPOSE_PROFILES=postgres,redis docker compose up -d
```

### Configuración de entorno

La configuración se divide en dos ficheros:

| Fichero | Contenido |
|---|---|
| `.env` (raíz del proyecto) | Perfiles Compose, puertos, modo de proceso, credenciales locales de PostgreSQL y Redis |
| `Backend/backend-api/.env` | Secretos de la aplicación: claves JWT, claves S3/R2, credenciales SMTP, etc. |

Usa los ficheros `.env.example` correspondientes como plantilla.

### Compatibilidad cloud

- **PostgreSQL**: Supabase o cualquier PostgreSQL gestionado — configura `MAIAN_DATABASE_URL` y elimina el perfil `postgres`.
- **Redis**: cualquier Redis externo — configura `MAIAN_REDIS_CACHE_URL` y `MAIAN_REDIS_BULL_URL`.
- **Almacenamiento de archivos**: Cloudflare R2 o cualquier servicio compatible con S3.
- **Exposición pública**: Cloudflare Tunnel (perfil `cloudflared`).

---

## Referencia de dependencias — Backend

### Dependencias de producción

| Paquete | Versión |
|---|---|
| `@aws-sdk/client-s3` | `^3.1063.0` |
| `@aws-sdk/lib-storage` | `^3.1063.0` |
| `@casl/ability` | `^6.8.1` |
| `@fastify/cookie` | `^11.0.2` |
| `@fastify/helmet` | `^13.0.2` |
| `@fastify/multipart` | `^9.4.0` |
| `@fastify/secure-session` | `8.2.0` |
| `@fastify/static` | `^9.1.3` |
| `@keyv/redis` | `^5.1.6` |
| `@nest-lab/throttler-storage-redis` | `^1.2.0` |
| `@nestia/core` | `^11.2.1` |
| `@nestia/e2e` | `^11.2.1` |
| `@nestia/fetcher` | `^11.2.1` |
| `@nestjs-modules/mailer` | `^2.3.6` |
| `@nestjs/bullmq` | `^11.0.4` |
| `@nestjs/cache-manager` | `^3.1.2` |
| `@nestjs/common` | `^11.1.24` |
| `@nestjs/config` | `^4.0.4` |
| `@nestjs/core` | `^11.1.24` |
| `@nestjs/jwt` | `^11.0.2` |
| `@nestjs/mapped-types` | `^2.1.1` |
| `@nestjs/passport` | `^11.0.5` |
| `@nestjs/platform-express` | `^11.1.24` |
| `@nestjs/platform-fastify` | `^11.1.24` |
| `@nestjs/schedule` | `^6.1.3` |
| `@nestjs/swagger` | `^11.4.4` |
| `@nestjs/throttler` | `^6.5.0` |
| `@prisma/adapter-pg` | `^7.8.0` |
| `@prisma/client` | `^7.8.0` |
| `@prisma/client-runtime-utils` | `^7.8.0` |
| `bcrypt` | `^6.0.0` |
| `bullmq` | `^5.78.0` |
| `cache-manager` | `^7.2.8` |
| `cache-manager-redis-store` | `^3.0.1` |
| `cross-env` | `^10.1.0` |
| `decimal.js` | `^10.6.0` |
| `drizzle-orm` | `^0.45.2` |
| `fastify` | `^5.8.5` |
| `file-type` | `^22.0.1` |
| `handlebars` | `^4.7.9` |
| `ioredis` | `^5.11.1` |
| `keyv` | `^5.6.0` |
| `libphonenumber-js` | `^1.13.6` |
| `lru-cache` | `^11.5.1` |
| `mime-types` | `^3.0.2` |
| `nestjs-i18n` | `^10.8.4` |
| `nestjs-pino` | `^4.6.1` |
| `nodemailer` | `^8.0.10` |
| `passport` | `0.7.0` |
| `passport-custom` | `^1.1.1` |
| `passport-jwt` | `^4.0.1` |
| `pdfmake` | `^0.3.10` |
| `pg` | `^8.21.0` |
| `pino` | `^9.14.0` |
| `pino-pretty` | `^13.1.3` |
| `piscina` | `^5.1.4` |
| `postgres` | `^3.4.9` |
| `reflect-metadata` | `^0.2.2` |
| `rxjs` | `^7.8.2` |
| `sharp` | `^0.34.5` |
| `typia` | `^12.1.1` |

### Dependencias de desarrollo

| Paquete | Versión |
|---|---|
| `@nestia/benchmark` | `^11.2.1` |
| `@nestia/sdk` | `^11.2.1` |
| `@nestjs/cli` | `^11.0.21` |
| `@nestjs/schematics` | `^11.1.0` |
| `@nestjs/testing` | `^11.1.24` |
| `@swc/cli` | `^0.7.10` |
| `@swc/core` | `^1.15.40` |
| `@types/bcrypt` | `^6.0.0` |
| `@types/express` | `^5.0.6` |
| `@types/jest` | `^30.0.0` |
| `@types/mime-types` | `^3.0.1` |
| `@types/node` | `^24.13.1` |
| `@types/nodemailer` | `^7.0.11` |
| `@types/passport` | `^1.0.17` |
| `@types/passport-jwt` | `^4.0.1` |
| `@types/pdfmake` | `^0.3.3` |
| `@types/pg` | `^8.20.0` |
| `@types/supertest` | `^6.0.3` |
| `@typescript-eslint/eslint-plugin` | `^8.60.1` |
| `@typescript-eslint/parser` | `^8.60.1` |
| `dotenv` | `^17.4.2` |
| `drizzle-kit` | `^0.31.10` |
| `eslint` | `^9.39.4` |
| `eslint-config-prettier` | `^10.1.8` |
| `eslint-plugin-prettier` | `^5.5.6` |
| `globals` | `^16.5.0` |
| `jest` | `^30.4.2` |
| `nestia` | `^11.2.1` |
| `prettier` | `^3.8.3` |
| `prisma` | `^7.8.0` |
| `source-map-support` | `^0.5.21` |
| `supertest` | `^7.2.2` |
| `ts-jest` | `^29.4.11` |
| `ts-loader` | `^9.6.0` |
| `ts-node` | `^10.9.2` |
| `ts-patch` | `^3.3.0` |
| `tsc-alias` | `^1.8.17` |
| `tsconfig-paths` | `^4.2.0` |
| `tsx` | `^4.22.4` |
| `typescript` | `~6.0.3` |
| `typescript-eslint` | `^8.60.1` |

---

## Referencia de dependencias — Frontend

Todas las versiones proceden de `Frontend/Maian/gradle/libs.versions.toml`.

### Plugins de Gradle

| Alias | Plugin ID | Versión |
|---|---|---|
| `androidApplication` / `androidLibrary` | `com.android.application` / `com.android.library` | `9.2.1` |
| `composeMultiplatform` | `org.jetbrains.compose` | `1.11.0` |
| `composeCompiler` | `org.jetbrains.kotlin.plugin.compose` | `2.3.21` |
| `kotlinMultiplatform` | `org.jetbrains.kotlin.multiplatform` | `2.3.21` |
| `kotlinxSerialization` | `org.jetbrains.kotlin.plugin.serialization` | `2.3.21` |

### Android y ciclo de vida

| Paquete | Versión |
|---|---|
| `androidx.activity:activity-compose` | `1.13.0` |
| `androidx.core:core-ktx` | `1.18.0` |
| `androidx.security:security-crypto` | `1.1.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` | `2.10.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` | `2.10.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | `2.10.0` |
| `org.jetbrains.androidx.savedstate:savedstate` | `1.4.0` |
| `org.jetbrains.androidx.window:window-core` | `1.5.1` |

### Red — Ktor

| Paquete | Versión |
|---|---|
| `io.ktor:ktor-client-core` | `3.5.0` |
| `io.ktor:ktor-client-content-negotiation` | `3.5.0` |
| `io.ktor:ktor-serialization-kotlinx-json` | `3.5.0` |
| `io.ktor:ktor-client-auth` | `3.5.0` |
| `io.ktor:ktor-client-logging` | `3.5.0` |
| `io.ktor:ktor-client-okhttp` (Android) | `3.5.0` |
| `io.ktor:ktor-client-darwin` (iOS) | `3.5.0` |
| `io.ktor:ktor-client-cio` (Desktop) | `3.5.0` |
| `io.ktor:ktor-client-js` (Web) | `3.5.0` |

### Inyección de dependencias — Koin

| Paquete | Versión |
|---|---|
| `io.insert-koin:koin-core` | `4.2.1` |
| `io.insert-koin:koin-compose-viewmodel` | `4.2.1` |

### Kotlinx

| Paquete | Versión |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-swing` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-datetime` | `0.8.0` |
| `org.jetbrains.kotlinx:kotlinx-collections-immutable` | `0.4.0` |

### Imagen, medios y ficheros

| Paquete | Versión |
|---|---|
| `io.coil-kt.coil3:coil-compose` | `3.4.0` |
| `io.coil-kt.coil3:coil-network-ktor3` | `3.4.0` |
| `io.github.vinceglb:filekit-core` | `0.13.0` |
| `io.github.vinceglb:filekit-dialogs-compose` | `0.13.0` |
| `io.github.vinceglb:filekit-coil` | `0.13.0` |
| `io.github.kdroidfilter:composemediaplayer` | `0.10.0` |
| `io.github.alexzhirkevich:compottie-lite` | `2.2.0` |

### Componentes UI y experiencia

| Paquete | Versión |
|---|---|
| `dev.chrisbanes.haze:haze` | `2.0.0-alpha02` |
| `dev.chrisbanes.haze:haze-blur` | `2.0.0-alpha02` |
| `com.eygraber:compose-placeholder-material3` | `1.0.12` |
| `dev.zt64.compose.pipette:compose-pipette` | `2.0.0` |
| `sh.calvin.reorderable:reorderable` | `3.1.0` |
| `io.github.dokar3:sonner` | `0.3.9` |
| `net.engawapg.lib:zoomable` | `2.12.0` |
| `io.github.khubaibkhan4:alert-kmp` | `2.0.0` |
| `com.patrykandpatrick.vico:compose` | `3.2.1` |
| `com.patrykandpatrick.vico:compose-m3` | `3.2.1` |
| `org.jetbrains.compose.material:material-icons-core` | `1.7.3` |
| `org.jetbrains.compose.material:material-icons-extended` | `1.7.3` |
| `org.jetbrains.compose.ui:ui-tooling` | `1.11.0` |
| `org.slf4j:slf4j-simple` | `2.0.18` |

### Navegación

| Paquete | Versión |
|---|---|
| `org.jetbrains.androidx.navigation:navigation-compose` | `2.9.2` |
| `androidx.navigation3:navigation3-runtime` | `1.1.2` |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | `1.1.1` |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` | `1.3.0-beta01` |

### Paginación

| Paquete | Versión |
|---|---|
| `androidx.paging:paging-common` | `3.4.0-rc01` |
| `androidx.paging:paging-compose` | `3.4.0-rc01` |

### Tablas

| Paquete | Versión |
|---|---|
| `ua.wwind.table-kmp:table-core` | `1.9.0` |

### Utilidades y dominio

| Paquete | Versión |
|---|---|
| `com.russhwolf:multiplatform-settings` | `1.3.0` |
| `io.github.luca992.libphonenumber-kotlin:libphonenumber` | `0.1.9` |
| `com.sanctionco.jmail:jmail` | `2.1.0` |
| `com.ionspin.kotlin:bignum` | `0.3.10` |

### Escáner y código de barras

| Paquete | Versión |
|---|---|
| `io.github.ismai117:KScan` | `0.9.1` |
| `com.google.zxing:core` | `3.5.4` |
| `com.google.zxing:javase` | `3.5.4` |
| `com.journeyapps:zxing-android-embedded` | `4.3.0` |
| `com.github.sarxos:webcam-capture` | `0.3.12` |

### WebView

| Paquete | Versión |
|---|---|
| `io.github.kevinnzou:compose-webview-multiplatform` | `2.0.3` |

### Editor de texto enriquecido

| Paquete | Versión |
|---|---|
| `com.mohamedrejeb.richeditor:richeditor-compose` | `1.0.0-rc14` |
