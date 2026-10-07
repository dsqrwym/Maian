# MaiAn

<p align="center">
  <img src="docs/demo/brand-logo.png" width="110" alt="MaiAn">
</p>

> *Este README se ha generado con **Antigravity** (Google DeepMind AI) a partir del análisis del repositorio y de la documentación del proyecto.*

**Leer en otro idioma:** [English](./Readme.md) · [中文](./Readme.zh.md)

---

MaiAn es una **plataforma B2B multiplataforma** que conecta distribuidores mayoristas con minoristas. Combina aplicaciones cliente nativas para distintos perfiles de usuario con un backend centralizado, seguro y escalable, diseñado para crecer con nuevas funciones de negocio.

El proyecto está desarrollado por **dsqrwym** (identificador técnico) / **MaiAn** (nombre de marca).

---

## Índice

- [Capturas de pantalla](#capturas-de-pantalla)
- [Estado actual](#estado-actual)
- [Backend](#backend)
- [Base de datos](#base-de-datos)
- [Frontend](#frontend)
- [Infraestructura y despliegue](#infraestructura-y-despliegue)
- [Referencia de dependencias — Backend](#referencia-de-dependencias--backend)
- [Referencia de dependencias — Frontend](#referencia-de-dependencias--frontend)

---

## Demos en línea

Puedes probar los clientes web con las siguientes cuentas de demostración:

| Cliente | Demo | Correo electrónico | Usuario | Contraseña |
|---|---|---|---|---|
| Minorista (`standard`) | [Abrir Standard](https://maian.dsqrwym.es/standard/) | `standard@maian.com` | `standard` | `Standard123` |
| Mayorista (`enterprise`) | [Abrir Enterprise](https://maian.dsqrwym.es/enterprise/) | `enterprise@maian.com` | `enterprise` | `Enterprise123` |

> Estas cuentas compartidas son solo para demostración. No modifiques sus credenciales ni elimines datos de demostración compartidos.

---

## Capturas de pantalla

> Las capturas siguientes proceden del documento de entrega **MaiAn_Memoria_YU** (capítulo 5 «Implementación y pruebas», capítulo 6 «Despliegue») y muestran la aplicación en ejecución real, no maquetas.
> Para que este README cargue con fluidez, todas las imágenes están escaladas a 1400 px de ancho (34 imágenes, ~3,8 MB en total); los originales a resolución completa están en el documento de entrega.

### Visión general

![Pantalla principal de MaiAn (tema claro)](docs/demo/hero-app-light.png)

<p align="center"><sub>Fig. 5.96 Pantalla principal: catálogo global, filtros por categoría, búsqueda y ordenación, modo tienda de un mayorista, selector de tema e idioma</sub></p>

### Minorista (`standard`) — catálogo, carrito y pedidos

| | | | |
|---|---|---|---|
| ![Catálogo global](docs/demo/retailer-catalog-mobile.png)<br><sub>Fig. 5.5 Catálogo global</sub> | ![Modo tienda](docs/demo/retailer-store-mode.png)<br><sub>Fig. 5.6 Modo tienda específico de un mayorista</sub> | ![Navegación por categorías](docs/demo/retailer-category-nav.png)<br><sub>Fig. 5.7 Navegación basada en categorías</sub> | ![Detalle de producto](docs/demo/retailer-product-detail.png)<br><sub>Fig. 5.8 Vista detallada de producto con selección de variantes</sub> |
| ![Carrito agrupado](docs/demo/retailer-cart-grouped.png)<br><sub>Fig. 5.9 Carrito agrupado automáticamente por mayorista</sub> | ![Historial de pedidos](docs/demo/retailer-order-history.png)<br><sub>Fig. 5.10 Historial de pedidos para minoristas</sub> | ![Seguimiento del pedido](docs/demo/retailer-order-tracking.png)<br><sub>Fig. 5.70 Historial del pedido y sus acciones (cancelar, descargar PDF)</sub> | |

### Mayorista (`enterprise`) — gestión del catálogo y de los pedidos

![Panel del mayorista](docs/demo/enterprise-dashboard.png)

<p align="center"><sub>Fig. 5.23 Dashboard general: estado de los pedidos, tendencia de ingresos, productos más vendidos y pedidos diarios</sub></p>

| | |
|---|---|
| ![Gestión de categorías](docs/demo/enterprise-category-management.png)<br><sub>Fig. 5.11 Gestión de categorías (jerarquía, IVA, traducciones)</sub> | ![Tabla de productos](docs/demo/enterprise-product-table.png)<br><sub>Fig. 5.14 Gestión de productos mediante tabla en escritorio</sub> |
| ![Formulario de producto](docs/demo/enterprise-product-form.png)<br><sub>Fig. 5.79 Formulario de creación de producto</sub> | ![Variantes y stock](docs/demo/enterprise-product-variants.png)<br><sub>Fig. 5.80 Variantes de venta, precios y stock</sub> |
| <img src="docs/demo/enterprise-rich-editor.png" width="340" alt="Editor de texto enriquecido"><br><sub>Fig. 5.81 Editor de contenido enriquecido</sub> | ![Vista previa del producto](docs/demo/enterprise-product-preview.png)<br><sub>Fig. 5.16 Vista previa del detalle de producto antes de guardar</sub> |
| ![Gestión de pedidos](docs/demo/enterprise-order-management.png)<br><sub>Fig. 5.18 Gestión de pedidos recibidos</sub> | ![Filtros de pedido](docs/demo/enterprise-order-filters.png)<br><sub>Fig. 5.21 Herramientas de filtrado de pedidos</sub> |
| ![Rechazo de pedido](docs/demo/enterprise-order-reject.png)<br><sub>Fig. 5.93 El rechazo del pedido exige un motivo</sub> | ![Gestión de empleados](docs/demo/enterprise-employee-management.png)<br><sub>Fig. 5.22 Gestión de empleados (soporte, reparto, almacén)</sub> |

### Autenticación y cuenta

| | |
|---|---|
| ![Inicio de sesión del mayorista](docs/demo/auth-login-wholesaler.png)<br><sub>Fig. 5.31 Inicio de sesión para mayorista</sub> | ![Inicio de sesión del empleado](docs/demo/auth-login-employee.png)<br><sub>Fig. 5.32 Inicio de sesión para empleado (el empleado debe activar la cuenta por correo)</sub> |
| ![Inicio de sesión del minorista](docs/demo/auth-login-retailer.png)<br><sub>Fig. 5.30 Inicio de sesión para minorista</sub> | ![Registro](docs/demo/auth-register.png)<br><sub>Fig. 5.34 Registro (minorista / mayorista)</sub> |
| ![Recuperación de contraseña](docs/demo/auth-password-recovery.png)<br><sub>Fig. 5.33 Recuperación de contraseña</sub> | **Cuenta y seguridad de sesión**<br><br>· Verificación por código enviado al correo; la cuenta no puede iniciar sesión hasta activarse<br>· Inicio de sesión por rol: `standard` / `enterprise` / `admin`<br>· En la web el refresh token se guarda en una cookie httpOnly y se rota; la consulta y el cierre de sesiones por dispositivo los ofrece la API del backend, todavía sin interfaz en el frontend<br>· Los frontends de minorista y mayorista están aislados: el acceso cruzado se rechaza |

### Temas, idioma y accesibilidad

| | |
|---|---|
| ![Tema claro](docs/demo/theme-light.png)<br><sub>Fig. 5.2 Tema claro (paleta Material Design 3)</sub> | ![Tema oscuro](docs/demo/theme-dark.png)<br><sub>Fig. 5.3 Tema oscuro</sub> |
| ![Catálogo en modo oscuro](docs/demo/app-dark-mode.png)<br><sub>Fig. 5.97 Catálogo en modo oscuro</sub> | ![Selector de idioma](docs/demo/i18n-language-switch.png)<br><sub>Fig. 5.99 Selector de idioma (ca-ES / en / es-ES / fr-FR / pt-PT / zh-CN / zh-TW)</sub> |
| <img src="docs/demo/a11y-large-font.png" width="230" alt="Tamaño de letra máximo"><br><sub>Fig. 5.98 Vistas con el tamaño de letra del sistema al máximo</sub> | **Accesibilidad y adaptación**<br><br>· Con el tamaño de letra del sistema al máximo, las tablas y los formularios se reorganizan sin cortes de texto ni solapamientos<br>· Móvil, tablet y escritorio usan navegación inferior, lateral y superior respectivamente (figs. 5.24–5.26)<br>· Ambos temas siguen las recomendaciones de contraste de Material Design |

### Notificaciones automáticas

| | |
|---|---|
| ![Correo de cancelación](docs/demo/notify-order-cancelled.png)<br><sub>Fig. 5.72 Correo enviado automáticamente al mayorista tras la cancelación</sub> | ![Correo de stock bajo](docs/demo/notify-low-stock.png)<br><sub>Fig. 5.86 Correo de advertencia cuando el stock baja del umbral</sub> |

### Despliegue y operación

| | |
|---|---|
| ![Swagger UI](docs/demo/ops-swagger.png)<br><sub>Fig. 6.5 Documentación interactiva OpenAPI / Swagger de la API</sub> | ![Clonar el repositorio](docs/demo/ops-git-clone.png)<br><sub>Fig. 6.1 Clonar el repositorio e instalar dependencias</sub> |

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

`AuthModule` · `LocationsModule` · `CaslModule` · `UserModule` · `EnterpriseModule` · `AdminModule` · `CategoryModule` · `ProductsModule` · `FilesModule` · `MailModule` · `CacheRedisModule` · `ScheduleTaskModule` · `MyI18nModule` · `MyThrottlerModule`

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
| Drizzle ORM | ORM principal y migraciones |
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

Gestionada con **Drizzle ORM**. El esquema SQL completo está en `Base_de_datos/schema.sql` y Docker Compose lo importa automáticamente al arrancar el contenedor de PostgreSQL.

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

### Dependencias de desarrollo

| Paquete | Versión |
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

## Referencia de dependencias — Frontend

Todas las versiones proceden de `Frontend/Maian/gradle/libs.versions.toml`.

### Plugins de Gradle

| Alias | Plugin ID | Versión |
|---|---|---|
| `androidApplication` / `androidLibrary` | `com.android.application` / `com.android.library` | `9.2.1` |
| `composeMultiplatform` | `org.jetbrains.compose` | `1.12.1` |
| `composeCompiler` | `org.jetbrains.kotlin.plugin.compose` | `2.4.20` |
| `kotlinMultiplatform` | `org.jetbrains.kotlin.multiplatform` | `2.4.20` |
| `kotlinxSerialization` | `org.jetbrains.kotlin.plugin.serialization` | `2.4.20` |

### Android y ciclo de vida

| Paquete | Versión |
|---|---|
| `androidx.activity:activity-compose` | `1.13.0` |
| `androidx.core:core-ktx` | `1.19.1` |
| `androidx.security:security-crypto` | `1.1.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` | `2.11.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` | `2.11.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | `2.11.0` |
| `org.jetbrains.androidx.savedstate:savedstate` | `1.4.0` |
| `org.jetbrains.androidx.window:window-core` | `1.5.1` |

### Red — Ktor

| Paquete | Versión |
|---|---|
| `io.ktor:ktor-client-core` | `3.6.0` |
| `io.ktor:ktor-client-content-negotiation` | `3.6.0` |
| `io.ktor:ktor-serialization-kotlinx-json` | `3.6.0` |
| `io.ktor:ktor-client-auth` | `3.6.0` |
| `io.ktor:ktor-client-logging` | `3.6.0` |
| `io.ktor:ktor-client-okhttp` (Android) | `3.6.0` |
| `io.ktor:ktor-client-darwin` (iOS) | `3.6.0` |
| `io.ktor:ktor-client-cio` (Desktop) | `3.6.0` |
| `io.ktor:ktor-client-js` (Web) | `3.6.0` |

### Inyección de dependencias — Koin

| Paquete | Versión |
|---|---|
| `io.insert-koin:koin-core` | `4.2.2` |
| `io.insert-koin:koin-compose-viewmodel` | `4.2.2` |

### Kotlinx

| Paquete | Versión |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-swing` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-datetime` | `0.8.0` |
| `org.jetbrains.kotlinx:kotlinx-collections-immutable` | `0.5.2` |

### Imagen, medios y ficheros

| Paquete | Versión |
|---|---|
| `io.coil-kt.coil3:coil-compose` | `3.6.3` |
| `io.coil-kt.coil3:coil-network-ktor3` | `3.6.3` |
| `io.github.vinceglb:filekit-core` | `0.16.0` |
| `io.github.vinceglb:filekit-dialogs-compose` | `0.16.0` |
| `io.github.vinceglb:filekit-coil` | `0.16.0` |
| `io.github.kdroidfilter:composemediaplayer` | `0.11.4` |
| `io.github.alexzhirkevich:compottie-lite` | `2.3.3` |

### Componentes UI y experiencia

| Paquete | Versión |
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

### Navegación

| Paquete | Versión |
|---|---|
| `org.jetbrains.androidx.navigation:navigation-compose` | `2.9.2` |
| `androidx.navigation3:navigation3-runtime` | `1.2.0` |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | `1.1.2` |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` | `1.3.0` |

### Paginación

| Paquete | Versión |
|---|---|
| `androidx.paging:paging-common` | `3.5.1` |
| `androidx.paging:paging-compose` | `3.5.1` |

### Tablas

| Paquete | Versión |
|---|---|
| `ua.wwind.table-kmp:table-core` | `2.4.1` |

### Utilidades y dominio

| Paquete | Versión |
|---|---|
| `com.russhwolf:multiplatform-settings` | `1.3.0` |
| `io.github.luca992.libphonenumber-kotlin:libphonenumber` | `0.1.9` |
| `com.sanctionco.jmail:jmail` | `2.2.2` |
| `com.ionspin.kotlin:bignum` | `0.3.10` |

### Escáner y código de barras

| Paquete | Versión |
|---|---|
| `io.github.ismai117:KScan` | `0.10.0` |
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
| `com.mohamedrejeb.richeditor:richeditor-compose` | `1.2.1` |
