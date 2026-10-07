# MaiAn

<p align="center">
  <img src="docs/demo/brand-logo.png" width="110" alt="MaiAn">
</p>

> *This README was generated with **Antigravity** (Google DeepMind AI) from repository analysis and the project documentation.*

**Read this in another language:** [Español](./Readme.es.md) · [中文](./Readme.zh.md)

---

MaiAn is a **B2B multiplatform application** that connects wholesale distributors with retailers. It combines native client apps for different user profiles with a centralized, secure, and scalable backend built to grow with new business features.

The project is developed by **dsqrwym** (technical identifier) / **MaiAn** (brand name).

---

## Table of Contents

- [Screenshots](#screenshots)
- [Current Status](#current-status)
- [Backend](#backend)
- [Database](#database)
- [Frontend](#frontend)
- [Infrastructure & Deployment](#infrastructure--deployment)
- [Dependency Reference — Backend](#dependency-reference--backend)
- [Dependency Reference — Frontend](#dependency-reference--frontend)

---

## Live Demos

Try the web clients with the following test accounts:

| Client | Demo | Email | Username | Password |
|---|---|---|---|---|
| Retailer (`standard`) | [Open Standard](https://maian.dsqrwym.es/standard/) | `standard@maian.com` | `standard` | `Standard123` |
| Wholesaler (`enterprise`) | [Open Enterprise](https://maian.dsqrwym.es/enterprise/) | `enterprise@maian.com` | `enterprise` | `Enterprise123` |

> These shared accounts are intended for demonstration only. Please do not change their credentials or delete shared demo data.

---

## Screenshots

> The screenshots below come from the project delivery document **MaiAn_Memoria_YU** (chapter 5 “Implementation and testing”, chapter 6 “Deployment”). They show the application as it actually runs, not mock-ups.
> To keep this README fast to load, every image is scaled to 1400 px wide (34 images, ~3.8 MB in total); the full-resolution originals are in the delivery document.

### Overview

![MaiAn main screen (light theme)](docs/demo/hero-app-light.png)

<p align="center"><sub>Fig. 5.96 Main screen: global catalog, category filters, search and sorting, wholesaler store mode, theme and language switchers</sub></p>

### Retailer (`standard`) — browsing, cart and ordering

| | | | |
|---|---|---|---|
| ![Global catalog](docs/demo/retailer-catalog-mobile.png)<br><sub>Fig. 5.5 Global product catalog</sub> | ![Store mode](docs/demo/retailer-store-mode.png)<br><sub>Fig. 5.6 Store mode for a specific wholesaler</sub> | ![Category hierarchy](docs/demo/retailer-category-nav.png)<br><sub>Fig. 5.7 Category hierarchy navigation</sub> | ![Product detail](docs/demo/retailer-product-detail.png)<br><sub>Fig. 5.8 Product detail with variant selection</sub> |
| ![Cart grouped by wholesaler](docs/demo/retailer-cart-grouped.png)<br><sub>Fig. 5.9 Cart grouped automatically by wholesaler</sub> | ![Order history](docs/demo/retailer-order-history.png)<br><sub>Fig. 5.10 Order history</sub> | ![Order tracking](docs/demo/retailer-order-tracking.png)<br><sub>Fig. 5.70 Order tracking and actions (cancel order, download PDF)</sub> | |

### Wholesaler (`enterprise`) — catalog operations and order handling

![Wholesaler dashboard](docs/demo/enterprise-dashboard.png)

<p align="center"><sub>Fig. 5.23 Dashboard: order status split, revenue trend, best-selling products and daily orders</sub></p>

| | |
|---|---|
| ![Category management](docs/demo/enterprise-category-management.png)<br><sub>Fig. 5.11 Category management (hierarchy, tax rate, translations)</sub> | ![Product table](docs/demo/enterprise-product-table.png)<br><sub>Fig. 5.14 Product table view on desktop</sub> |
| ![Product creation form](docs/demo/enterprise-product-form.png)<br><sub>Fig. 5.79 Product creation form</sub> | ![Variants and stock](docs/demo/enterprise-product-variants.png)<br><sub>Fig. 5.80 Sale variants, pricing and stock</sub> |
| <img src="docs/demo/enterprise-rich-editor.png" width="340" alt="Rich text editor"><br><sub>Fig. 5.81 Rich text content editor</sub> | ![Product detail preview](docs/demo/enterprise-product-preview.png)<br><sub>Fig. 5.16 Product detail preview before saving</sub> |
| ![Order management](docs/demo/enterprise-order-management.png)<br><sub>Fig. 5.18 Incoming order management</sub> | ![Order filters](docs/demo/enterprise-order-filters.png)<br><sub>Fig. 5.21 Order filtering tools</sub> |
| ![Order rejection](docs/demo/enterprise-order-reject.png)<br><sub>Fig. 5.93 Rejecting an order requires a reason</sub> | ![Employee management](docs/demo/enterprise-employee-management.png)<br><sub>Fig. 5.22 Employee account management (support, delivery, warehouse)</sub> |

### Authentication and account

| | |
|---|---|
| ![Wholesaler sign-in](docs/demo/auth-login-wholesaler.png)<br><sub>Fig. 5.31 Wholesaler sign-in</sub> | ![Employee sign-in](docs/demo/auth-login-employee.png)<br><sub>Fig. 5.32 Employee sign-in (employees must activate the account by email first)</sub> |
| ![Retailer sign-in](docs/demo/auth-login-retailer.png)<br><sub>Fig. 5.30 Retailer sign-in</sub> | ![Registration](docs/demo/auth-register.png)<br><sub>Fig. 5.34 Registration (retailer / wholesaler)</sub> |
| ![Password recovery](docs/demo/auth-password-recovery.png)<br><sub>Fig. 5.33 Password recovery</sub> | **Account and session security**<br><br>· Email verification code — an account cannot sign in before activation<br>· Role-based sign-in: `standard` / `enterprise` / `admin`<br>· On the web the refresh token lives in an httpOnly cookie and is rotated; per-device session listing and revocation are provided by the backend API, with no frontend UI yet<br>· The retailer and wholesaler frontends are isolated — cross-app access is rejected |

### Themes, internationalisation and accessibility

| | |
|---|---|
| ![Light theme](docs/demo/theme-light.png)<br><sub>Fig. 5.2 Light theme (Material Design 3 palette)</sub> | ![Dark theme](docs/demo/theme-dark.png)<br><sub>Fig. 5.3 Dark theme</sub> |
| ![Catalog in dark mode](docs/demo/app-dark-mode.png)<br><sub>Fig. 5.97 Catalog in dark mode</sub> | ![Language switcher](docs/demo/i18n-language-switch.png)<br><sub>Fig. 5.99 Language switcher (ca-ES / en / es-ES / fr-FR / pt-PT / zh-CN / zh-TW)</sub> |
| <img src="docs/demo/a11y-large-font.png" width="230" alt="Maximum font size"><br><sub>Fig. 5.98 Layout with the system font size at its maximum</sub> | **Accessibility and adaptivity**<br><br>· With the system font at its maximum, tables and forms reflow with no clipped text or overlapping elements<br>· Mobile, tablet and desktop use bottom, side and top navigation respectively (figs. 5.24–5.26)<br>· Both themes follow the Material Design contrast guidance |

### Automated notifications

| | |
|---|---|
| ![Order cancelled email](docs/demo/notify-order-cancelled.png)<br><sub>Fig. 5.72 Email sent automatically to the wholesaler after a cancellation</sub> | ![Low stock email](docs/demo/notify-low-stock.png)<br><sub>Fig. 5.86 Warning email triggered when stock drops below the threshold</sub> |

### Deployment and operations

| | |
|---|---|
| ![Swagger UI](docs/demo/ops-swagger.png)<br><sub>Fig. 6.5 OpenAPI / Swagger interactive API reference</sub> | ![Clone the repository](docs/demo/ops-git-clone.png)<br><sub>Fig. 6.1 Cloning the repository and installing dependencies</sub> |

---

## Current Status

### Implemented and working

- Modular NestJS 11 + Fastify backend, deployable in `single`, `cluster`, or `pm2` mode.
- Full authentication: registration, email verification, role-based login (`standard`, `enterprise`, `admin`), refresh token with rotation, per-device session management, and password reset.
- PostgreSQL 17 as primary persistence. Two Redis instances: one for cache / sessions / rate-limit, one for BullMQ queues.
- Role-based access control with CASL.
- Full category management: hierarchical, public/private, with multilingual translations.
- Full product management: variants, multilingual translations, associated files.
- File upload and retrieval with local storage driver and Cloudflare R2 / S3-compatible driver. Image processing with `sharp`; PDF generation with `pdfmake`.
- Locations API: countries, provinces, cities, ISO numeric currencies.
- Internal employee account creation for wholesalers (support, delivery, warehouse).
- Administrator account creation by `SUPERADMIN`.
- Complete Docker Compose setup: backend, PostgreSQL 17, two Redis, optional Cloudflare Tunnel.
- Kotlin Multiplatform frontend with modules: `shared`, `standard`, `enterprise`, `admin`, `business`, `iosApp`.
- Frontend–backend integration for auth, categories, products, files, and locations.
- Database schema already covers carts, orders, messages, notifications, deliveries, and business relationships.

### Partially implemented

- **`standard` module**: authentication and main structure are present, but business feature coverage is less complete than `enterprise`.
- **`business` module**: reusable business component layer (categories, media, rich text editor), not a fully standalone client.

### Not yet completed

- End-to-end order flow (API + client apps).
- In-app chat / messaging system fully connected in the frontend.
- Production-ready push notification system.
- Sales statistics and analytics dashboards.
- Geolocation and maps (planned for a future release).

---

## Backend

Located at `Backend/backend-api`. Uses a modular NestJS architecture with Fastify as the HTTP adapter.

### Active modules in `AppModule`

`AuthModule` · `LocationsModule` · `CaslModule` · `UserModule` · `EnterpriseModule` · `AdminModule` · `CategoryModule` · `ProductsModule` · `FilesModule` · `MailModule` · `CacheRedisModule` · `ScheduleTaskModule` · `MyI18nModule` · `MyThrottlerModule`

The backend also includes global exception filters, a unified response interceptor, structured logging with Pino, and centralized JWT configuration. Supports three process modes: `single`, native Node `cluster`, and `pm2`.

### Features

#### Authentication & sessions

- Retailer and wholesaler registration.
- Email verification code.
- Role-based login: `standard`, `enterprise`, `admin`.
- httpOnly cookie for refresh token (web flow) with rotation and CSRF protection.
- Password reset.
- Session deletion and per-device session management.

#### Users & organization

- Email / username availability check.
- Paginated user search with filters.
- Admin creation by `SUPERADMIN`.
- Wholesaler employee creation: support, delivery, warehouse.

#### Catalog

- **Categories**: create, list, search, edit, delete, hierarchies, public/private visibility, multilingual translations.
- **Products**: create, list, detail, edit, delete, sale variants, multilingual translations, associated files.

#### Files

- `multipart` upload.
- Real MIME-type validation.
- Safe filename generation.
- Local storage driver.
- Cloudflare R2 / S3-compatible driver (`@aws-sdk`).
- Image processing via `sharp`.
- PDF document generation via `pdfmake`.

#### Locations

- Countries, provinces per country, cities per province.
- Currencies by ISO numeric code.

### Technology stack

| Technology | Role |
|---|---|
| NestJS 11 | Application framework |
| Fastify 5 | High-performance HTTP adapter |
| Drizzle ORM | Primary ORM and migrations |
| PostgreSQL 17 | Primary database |
| Redis 7 | Cache, sessions, rate-limit, BullMQ queues |
| JWT / Passport | Authentication |
| CASL | Attribute-based access control |
| Swagger | API documentation |
| Pino | Structured logging |
| BullMQ | Async job queues |
| Nodemailer | Email delivery |
| nestjs-i18n | Internationalisation |
| typia / nestia | Type-safe validation and serialisation (fully migrated from class-validator) |
| sharp | Image processing |
| pdfmake | PDF generation |

### Validation and sanitisation policy

The backend distinguishes between two field types:

- **User input fields** (`name`, `companyName`, `description`, etc.): require semantic sanitisation + strict validation before storage.
- **System fields** (`deviceName`, `langCode`, `timezone`, etc.): must not be aggressively rewritten; validation is flexible or omitted for semantic normalisation.

A malformed value must never break business logic, persistence, or authorisation.

---

## Database

Managed via **Drizzle ORM**. The full SQL schema is in `Base_de_datos/schema.sql` and is automatically imported by Docker Compose when the PostgreSQL container starts.

### Entities

Users · Configurations · Addresses · User sessions · Verification tokens · Categories (with translations) · Products (with variants) · Product–category relations · Files · Carts · Orders and order lines · Discounts · Deliveries (with timeline) · Chats · Messages · Notifications · Countries · Provinces · Cities · Currencies

### Technical highlights

- Row Level Security enabled on multiple tables.
- Comprehensive indexes, constraints, and foreign-key relations.
- Automatic `user_id` generation per role.
- Preloaded reference data: currencies, countries, provinces, cities, and base categories.
- The database schema is ahead of some API and client layers.

---

## Frontend

Located at `Frontend/Maian`. Built with **Kotlin Multiplatform** and **Compose Multiplatform**.

### Target platforms

| Platform | Details |
|---|---|
| Android | minSdk 24, compileSdk 37 |
| iOS | Native via iosApp entry point |
| Desktop | JVM / Swing |
| Web | Kotlin/Wasm |

### Modules

| Module | Description |
|---|---|
| `shared` | Common base: Ktor HTTP client, token storage, repositories (auth, category, products, file, location, user), shared theme, i18n, timezone, reusable UI components, file upload support |
| `standard` | Retailer client: login, registration, home screen, Koin DI, navigation |
| `enterprise` | Wholesaler client (most mature): login, registration, category CRUD, product CRUD, table/grid views |
| `admin` | Admin panel: login, category management, user repository, own DI and navigation |
| `business` | Reusable business layer: category forms and lists, rich text editor, media picker and manager |
| `iosApp` | iOS entry point (SwiftUI wrapper) |

### Assets

- **Icons**: Compose Material Icons (Core + Extended).
- **Font**: MiSans (bundled as a font resource).

### Key versions

| Dependency | Version |
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

## Infrastructure & Deployment

### Docker Compose

`docker-compose.yml` uses **optional profiles** to control which services start:

| Service | Profile | Description |
|---|---|---|
| `backend` | *(always active)* | NestJS API; supports `single`, `cluster`, `pm2` modes |
| `postgres` | `postgres`, `local-infra` | PostgreSQL 17; auto-imports all SQL seeds from `Base_de_datos/` |
| `redis-cache` | `redis`, `local-infra` | Redis 7 for cache, sessions, and rate-limit |
| `redis-bull` | `redis`, `local-infra` | Redis 7 dedicated to BullMQ queues |
| `cloudflared` | `cloudflared` | Cloudflare Tunnel for secure public exposure |

Quick start (full local stack, no tunnel):

```bash
COMPOSE_PROFILES=postgres,redis docker compose up -d
```

### Environment configuration

Configuration is split into two files:

| File | Contents |
|---|---|
| `.env` (project root) | Compose profiles, ports, process mode, local PostgreSQL and Redis credentials |
| `Backend/backend-api/.env` | Application secrets: JWT keys, S3/R2 keys, SMTP credentials, etc. |

Use the corresponding `.env.example` files as templates.

### Cloud-compatible services

- **PostgreSQL**: Supabase or any managed PostgreSQL — set `MAIAN_DATABASE_URL` and remove the `postgres` profile.
- **Redis**: any external Redis — set `MAIAN_REDIS_CACHE_URL` and `MAIAN_REDIS_BULL_URL`.
- **File storage**: Cloudflare R2 or any S3-compatible service.
- **Public exposure**: Cloudflare Tunnel (`cloudflared` profile).

---

## Dependency Reference — Backend

### Production dependencies

| Package | Version |
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

### Development dependencies

| Package | Version |
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

## Dependency Reference — Frontend

All versions sourced from `Frontend/Maian/gradle/libs.versions.toml`.

### Gradle plugins

| Alias | Plugin ID | Version |
|---|---|---|
| `androidApplication` / `androidLibrary` | `com.android.application` / `com.android.library` | `9.2.1` |
| `composeMultiplatform` | `org.jetbrains.compose` | `1.12.1` |
| `composeCompiler` | `org.jetbrains.kotlin.plugin.compose` | `2.4.20` |
| `kotlinMultiplatform` | `org.jetbrains.kotlin.multiplatform` | `2.4.20` |
| `kotlinxSerialization` | `org.jetbrains.kotlin.plugin.serialization` | `2.4.20` |

### Android & lifecycle

| Package | Version |
|---|---|
| `androidx.activity:activity-compose` | `1.13.0` |
| `androidx.core:core-ktx` | `1.19.1` |
| `androidx.security:security-crypto` | `1.1.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` | `2.11.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` | `2.11.0` |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | `2.11.0` |
| `org.jetbrains.androidx.savedstate:savedstate` | `1.4.0` |
| `org.jetbrains.androidx.window:window-core` | `1.5.1` |

### Networking — Ktor

| Package | Version |
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

### Dependency injection — Koin

| Package | Version |
|---|---|
| `io.insert-koin:koin-core` | `4.2.2` |
| `io.insert-koin:koin-compose-viewmodel` | `4.2.2` |

### Kotlinx libraries

| Package | Version |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-swing` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.11.0` |
| `org.jetbrains.kotlinx:kotlinx-datetime` | `0.8.0` |
| `org.jetbrains.kotlinx:kotlinx-collections-immutable` | `0.5.2` |

### Image, media & files

| Package | Version |
|---|---|
| `io.coil-kt.coil3:coil-compose` | `3.6.3` |
| `io.coil-kt.coil3:coil-network-ktor3` | `3.6.3` |
| `io.github.vinceglb:filekit-core` | `0.16.0` |
| `io.github.vinceglb:filekit-dialogs-compose` | `0.16.0` |
| `io.github.vinceglb:filekit-coil` | `0.16.0` |
| `io.github.kdroidfilter:composemediaplayer` | `0.11.4` |
| `io.github.alexzhirkevich:compottie-lite` | `2.3.3` |

### UI components & experience

| Package | Version |
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

### Navigation

| Package | Version |
|---|---|
| `org.jetbrains.androidx.navigation:navigation-compose` | `2.9.2` |
| `androidx.navigation3:navigation3-runtime` | `1.2.0` |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | `1.1.2` |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` | `1.3.0` |

### Paging

| Package | Version |
|---|---|
| `androidx.paging:paging-common` | `3.5.1` |
| `androidx.paging:paging-compose` | `3.5.1` |

### Tables

| Package | Version |
|---|---|
| `ua.wwind.table-kmp:table-core` | `2.4.1` |

### Utilities & domain

| Package | Version |
|---|---|
| `com.russhwolf:multiplatform-settings` | `1.3.0` |
| `io.github.luca992.libphonenumber-kotlin:libphonenumber` | `0.1.9` |
| `com.sanctionco.jmail:jmail` | `2.2.2` |
| `com.ionspin.kotlin:bignum` | `0.3.10` |

### Barcode & QR scanning

| Package | Version |
|---|---|
| `io.github.ismai117:KScan` | `0.10.0` |
| `com.google.zxing:core` | `3.5.4` |
| `com.google.zxing:javase` | `3.5.4` |
| `com.journeyapps:zxing-android-embedded` | `4.3.0` |
| `com.github.sarxos:webcam-capture` | `0.3.12` |

### WebView

| Package | Version |
|---|---|
| `io.github.kevinnzou:compose-webview-multiplatform` | `2.0.3` |

### Rich text editing

| Package | Version |
|---|---|
| `com.mohamedrejeb.richeditor:richeditor-compose` | `1.2.1` |
