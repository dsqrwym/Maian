"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.product_translations = exports.category_translations = exports.product_categories = exports.chat_participants = exports.user_uploads = exports.users = exports.orders = exports.verification_tokens = exports.user_sessions = exports.notifications = exports.messages = exports.order_details = exports.delivery_timeline = exports.directions = exports.currencies = exports.products = exports.deliveries = exports.configurations = exports.carts = exports.products_files = exports.message_files = exports.discounts = exports.chat_panels = exports.categories = exports.cart_details = exports.cities = exports.files = exports.variant_products = exports.countries = exports.provinces = exports.seq_superadmin_id = exports.seq_admin_id = exports.seq_wholesaler_id = exports.seq_warehouse_id = exports.seq_support_id = exports.seq_retailer_id = exports.seq_deliveryman_id = exports.UserStatus = exports.UserRole = exports.SaleVariant = exports.ProductStatus = exports.DeliveryStatus = exports.AddressType = void 0;
const pg_core_1 = require("drizzle-orm/pg-core");
const drizzle_orm_1 = require("drizzle-orm");
exports.AddressType = (0, pg_core_1.pgEnum)("AddressType", ['DELIVERY', 'INVOICE', 'STORE']);
exports.DeliveryStatus = (0, pg_core_1.pgEnum)("DeliveryStatus", ['PENDING', 'IN_PROGRESS', 'COMPLETED']);
exports.ProductStatus = (0, pg_core_1.pgEnum)("ProductStatus", ['ACTIVE', 'INACTIVE']);
exports.SaleVariant = (0, pg_core_1.pgEnum)("SaleVariant", ['UNIT', 'BOX', 'PACK']);
exports.UserRole = (0, pg_core_1.pgEnum)("UserRole", ['WHOLESALER', 'RETAILER', 'SUPPORT', 'DELIVERY', 'WAREHOUSE', 'ADMIN', 'SUPERADMIN']);
exports.UserStatus = (0, pg_core_1.pgEnum)("UserStatus", ['PENDING_VERIFICATION', 'INACTIVE', 'ACTIVE', 'PENDING_REVIEW', 'APPROVED', 'BANNED']);
exports.seq_deliveryman_id = (0, pg_core_1.pgSequence)("seq_deliveryman_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.seq_retailer_id = (0, pg_core_1.pgSequence)("seq_retailer_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.seq_support_id = (0, pg_core_1.pgSequence)("seq_support_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.seq_warehouse_id = (0, pg_core_1.pgSequence)("seq_warehouse_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.seq_wholesaler_id = (0, pg_core_1.pgSequence)("seq_wholesaler_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.seq_admin_id = (0, pg_core_1.pgSequence)("seq_admin_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.seq_superadmin_id = (0, pg_core_1.pgSequence)("seq_superadmin_id", { startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false });
exports.provinces = (0, pg_core_1.pgTable)("provinces", {
    id: (0, pg_core_1.serial)().primaryKey().notNull(),
    country_iso: (0, pg_core_1.smallint)().notNull(),
    name: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
    name_local: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.country_iso],
        foreignColumns: [exports.countries.iso_numeric],
        name: "provinces_country_iso_fkey"
    }).onUpdate("cascade").onDelete("cascade"),
]);
exports.countries = (0, pg_core_1.pgTable)("countries", {
    iso_alpha2: (0, pg_core_1.char)({ length: 2 }).notNull(),
    iso_alpha3: (0, pg_core_1.char)({ length: 3 }).notNull(),
    iso_numeric: (0, pg_core_1.smallint)().primaryKey().notNull(),
    name: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
    name_local: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
    currency_id: (0, pg_core_1.smallint)(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.currency_id],
        foreignColumns: [exports.currencies.iso_numeric],
        name: "countries_currency_id_fkey"
    }).onUpdate("cascade").onDelete("restrict"),
    (0, pg_core_1.unique)("countries_iso_alpha2_key").on(table.iso_alpha2),
    (0, pg_core_1.unique)("countries_iso_alpha3_key").on(table.iso_alpha3),
    (0, pg_core_1.unique)("countries_iso_numeric_key").on(table.iso_numeric),
]);
exports.variant_products = (0, pg_core_1.pgTable)("variant_products", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "variant_products_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    product_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    type_sale: (0, exports.SaleVariant)().notNull(),
    price: (0, pg_core_1.numeric)({ precision: 10, scale: 2 }).notNull(),
    price_iva: (0, pg_core_1.numeric)({ precision: 10, scale: 2 }).notNull(),
    available_stock: (0, pg_core_1.integer)().notNull(),
    sort: (0, pg_core_1.smallint)().notNull(),
    attributes: (0, pg_core_1.jsonb)(),
    status: (0, exports.ProductStatus)().default('ACTIVE').notNull(),
    product_code: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    reserved_stock: (0, pg_core_1.integer)().default(0).notNull(),
    low_stock_threshold: (0, pg_core_1.integer)().default(0).notNull(),
    sale_unit_qty: (0, pg_core_1.integer)().default(1).notNull(),
    min_order_qty: (0, pg_core_1.integer)().default(1).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    created_by: (0, pg_core_1.uuid)().notNull(),
    updated_by: (0, pg_core_1.uuid)(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.created_by],
        foreignColumns: [exports.users.id],
        name: "variant_products_created_by_fkey"
    }),
    (0, pg_core_1.foreignKey)({
        columns: [table.product_id],
        foreignColumns: [exports.products.id],
        name: "variant_products_product_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.updated_by],
        foreignColumns: [exports.users.id],
        name: "variant_products_updated_by_fkey"
    }),
    (0, pg_core_1.check)("variant_available_stock_check", (0, drizzle_orm_1.sql) `available_stock >= 0`),
    (0, pg_core_1.check)("variant_low_stock_threshold_check", (0, drizzle_orm_1.sql) `low_stock_threshold >= 0`),
    (0, pg_core_1.check)("variant_min_order_qty_check", (0, drizzle_orm_1.sql) `min_order_qty >= 1`),
    (0, pg_core_1.check)("variant_price_check", (0, drizzle_orm_1.sql) `price >= (0)::numeric`),
    (0, pg_core_1.check)("variant_price_iva_check", (0, drizzle_orm_1.sql) `price_iva >= (0)::numeric`),
    (0, pg_core_1.check)("variant_reserved_stock_check", (0, drizzle_orm_1.sql) `reserved_stock >= 0`),
    (0, pg_core_1.check)("variant_sale_unit_qty_check", (0, drizzle_orm_1.sql) `sale_unit_qty >= 1`),
    (0, pg_core_1.check)("variant_sort_check", (0, drizzle_orm_1.sql) `sort >= 0`),
]);
exports.files = (0, pg_core_1.pgTable)("files", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "files_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    file_name: (0, pg_core_1.varchar)().notNull(),
    file_hash: (0, pg_core_1.varchar)({ length: 64 }).notNull(),
    mime_type: (0, pg_core_1.varchar)({ length: 128 }).notNull(),
    file_size: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    storage_key: (0, pg_core_1.text)().notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    to_delete: (0, pg_core_1.boolean)().default(false).notNull(),
}, (table) => [
    (0, pg_core_1.index)("idx_files_created_at").using("btree", table.created_at.asc().nullsLast().op("timestamp_ops")),
    (0, pg_core_1.unique)("files_file_hash_key").on(table.file_hash),
    (0, pg_core_1.check)("files_file_name_check", (0, drizzle_orm_1.sql) `(file_name)::text ~* '^[^\\/:\*\?"<>\|]{1,255}\.[a-z0-9]+$'::text`),
]);
exports.cities = (0, pg_core_1.pgTable)("cities", {
    id: (0, pg_core_1.serial)().primaryKey().notNull(),
    province_id: (0, pg_core_1.integer)().notNull(),
    name: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
    name_local: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.province_id],
        foreignColumns: [exports.provinces.id],
        name: "cities_province_id_fkey"
    }).onDelete("cascade"),
]);
exports.cart_details = (0, pg_core_1.pgTable)("cart_details", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "cart_details_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    cart_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    variant_products_id: (0, pg_core_1.bigint)({ mode: "number" }),
    quantity: (0, pg_core_1.integer)().notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.cart_id],
        foreignColumns: [exports.carts.id],
        name: "cart_details_cart_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.variant_products_id],
        foreignColumns: [exports.variant_products.id],
        name: "cart_details_variant_products_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.check)("cart_details_quantity_check", (0, drizzle_orm_1.sql) `quantity > 0`),
]);
exports.categories = (0, pg_core_1.pgTable)("categories", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "categories_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    user_id: (0, pg_core_1.uuid)(),
    name: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    iva: (0, pg_core_1.numeric)({ precision: 5, scale: 2 }),
    parent_id: (0, pg_core_1.bigint)({ mode: "number" }),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    level: (0, pg_core_1.smallint)().notNull(),
    name_unaccent: (0, pg_core_1.text)().generatedAlwaysAs((0, drizzle_orm_1.sql) `immutable_unaccent((name)::text)`),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    created_by: (0, pg_core_1.uuid)(),
    updated_by: (0, pg_core_1.uuid)(),
    version: (0, pg_core_1.bigint)({ mode: "number" }).default(1).notNull(),
    deleted_at: (0, pg_core_1.timestamp)({ mode: 'string' }),
}, (table) => [
    (0, pg_core_1.uniqueIndex)("categories_name_unique_public").using("btree", table.name.asc().nullsLast().op("text_ops")).where((0, drizzle_orm_1.sql) `((user_id IS NULL) AND (deleted_at IS NULL))`),
    (0, pg_core_1.uniqueIndex)("categories_user_name_unique_private").using("btree", table.user_id.asc().nullsLast().op("uuid_ops"), table.name.asc().nullsLast().op("text_ops")).where((0, drizzle_orm_1.sql) `((user_id IS NOT NULL) AND (deleted_at IS NULL))`),
    (0, pg_core_1.index)("idx_categories_name_unaccent").using("btree", (0, drizzle_orm_1.sql) `lower(name_unaccent)`),
    (0, pg_core_1.foreignKey)({
        columns: [table.created_by],
        foreignColumns: [exports.users.id],
        name: "categories_created_by_fkey"
    }).onDelete("set null"),
    (0, pg_core_1.foreignKey)({
        columns: [table.parent_id],
        foreignColumns: [table.id],
        name: "categories_parent_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.updated_by],
        foreignColumns: [exports.users.id],
        name: "categories_updated_by_fkey"
    }).onDelete("set null"),
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "categories_user_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.check)("categories_iva_check", (0, drizzle_orm_1.sql) `iva >= (0)::numeric`),
    (0, pg_core_1.check)("categories_level_check", (0, drizzle_orm_1.sql) `(level >= 1) AND (level <= 3)`),
]);
exports.chat_panels = (0, pg_core_1.pgTable)("chat_panels", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "chat_panel_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    name: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
});
exports.discounts = (0, pg_core_1.pgTable)("discounts", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "discounts_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    user_id: (0, pg_core_1.uuid)().notNull(),
    name: (0, pg_core_1.varchar)().notNull(),
    type_value: (0, pg_core_1.jsonb)().notNull(),
    applies_to_all: (0, pg_core_1.boolean)().default(false).notNull(),
    start_date: (0, pg_core_1.date)().notNull(),
    end_date: (0, pg_core_1.date)().notNull(),
    status: (0, pg_core_1.smallint)().default((0, drizzle_orm_1.sql) `'1'`).notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "discounts_user_id_fkey"
    }).onDelete("cascade"),
]);
exports.message_files = (0, pg_core_1.pgTable)("message_files", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "message_files_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    message_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    file_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    sort: (0, pg_core_1.smallint)().notNull(),
}, (table) => [
    (0, pg_core_1.index)("idx_message_files_file_id").using("btree", table.file_id.asc().nullsLast().op("int8_ops")),
    (0, pg_core_1.foreignKey)({
        columns: [table.file_id],
        foreignColumns: [exports.files.id],
        name: "message_files_file_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.message_id],
        foreignColumns: [exports.messages.id],
        name: "message_files_message_id_fkey"
    }).onDelete("cascade"),
]);
exports.products_files = (0, pg_core_1.pgTable)("products_files", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "products_files_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    product_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    file_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    sort: (0, pg_core_1.smallint)().notNull(),
}, (table) => [
    (0, pg_core_1.index)("idx_products_files_file_id").using("btree", table.file_id.asc().nullsLast().op("int8_ops")),
    (0, pg_core_1.index)("idx_products_files_product_id").using("btree", table.product_id.asc().nullsLast().op("int8_ops")),
    (0, pg_core_1.foreignKey)({
        columns: [table.file_id],
        foreignColumns: [exports.files.id],
        name: "products_files_file_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.product_id],
        foreignColumns: [exports.products.id],
        name: "products_files_product_id_fkey"
    }).onDelete("cascade"),
]);
exports.carts = (0, pg_core_1.pgTable)("carts", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "cart_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    user_id: (0, pg_core_1.uuid)().notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "carts_user_id_fkey"
    }).onDelete("cascade"),
]);
exports.configurations = (0, pg_core_1.pgTable)("configurations", {
    user_id: (0, pg_core_1.uuid)().primaryKey().notNull(),
    language: (0, pg_core_1.varchar)({ length: 10 }).default('en').notNull(),
    timezone: (0, pg_core_1.varchar)({ length: 32 }).default('UTC').notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "configurations_user_id_fkey"
    }).onDelete("cascade"),
]);
exports.deliveries = (0, pg_core_1.pgTable)("deliveries", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "deliveries_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    order_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    delivery_person: (0, pg_core_1.uuid)(),
    status: (0, exports.DeliveryStatus)().default('PENDING').notNull(),
    start_time: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    end_time: (0, pg_core_1.timestamp)({ mode: 'string' }),
    notes: (0, pg_core_1.text)().notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }),
    latitude: (0, pg_core_1.doublePrecision)().notNull(),
    longitude: (0, pg_core_1.doublePrecision)().notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.delivery_person],
        foreignColumns: [exports.users.id],
        name: "deliveries_delivery_person_fkey"
    }).onDelete("set null"),
]);
exports.products = (0, pg_core_1.pgTable)("products", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "products_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    user_id: (0, pg_core_1.uuid)().notNull(),
    name: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    title: (0, pg_core_1.varchar)({ length: 100 }),
    description: (0, pg_core_1.text)(),
    iva: (0, pg_core_1.numeric)({ precision: 5, scale: 2 }).notNull(),
    status: (0, exports.ProductStatus)().default('ACTIVE').notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    product_code: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    created_by: (0, pg_core_1.uuid)().notNull(),
    updated_by: (0, pg_core_1.uuid)(),
    name_unaccent: (0, pg_core_1.text)().generatedAlwaysAs((0, drizzle_orm_1.sql) `immutable_unaccent((name)::text)`),
    title_unaccent: (0, pg_core_1.text)().generatedAlwaysAs((0, drizzle_orm_1.sql) `immutable_unaccent((title)::text)`),
    version: (0, pg_core_1.bigint)({ mode: "number" }).default(1).notNull(),
    deleted_at: (0, pg_core_1.timestamp)({ mode: 'string' }),
}, (table) => [
    (0, pg_core_1.index)("idx_products_name_unaccent").using("btree", (0, drizzle_orm_1.sql) `lower(name_unaccent)`),
    (0, pg_core_1.index)("idx_products_title_unaccent").using("btree", (0, drizzle_orm_1.sql) `lower(title_unaccent)`),
    (0, pg_core_1.foreignKey)({
        columns: [table.created_by],
        foreignColumns: [exports.users.id],
        name: "products_created_by_fkey"
    }),
    (0, pg_core_1.foreignKey)({
        columns: [table.updated_by],
        foreignColumns: [exports.users.id],
        name: "products_updated_by_fkey"
    }),
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "products_user_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.check)("products_iva_check", (0, drizzle_orm_1.sql) `iva >= (0)::numeric`),
]);
exports.currencies = (0, pg_core_1.pgTable)("currencies", {
    iso_numeric: (0, pg_core_1.smallint)().primaryKey().notNull(),
    iso_alpha3: (0, pg_core_1.char)({ length: 3 }).notNull(),
    symbol: (0, pg_core_1.varchar)({ length: 5 }).notNull(),
    decimal_digits: (0, pg_core_1.smallint)().notNull(),
}, (table) => [
    (0, pg_core_1.unique)("currencies_iso_alpha3_key").on(table.iso_alpha3),
]);
exports.directions = (0, pg_core_1.pgTable)("directions", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "directions_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    user_id: (0, pg_core_1.uuid)().notNull(),
    type: (0, exports.AddressType)().default('STORE').notNull(),
    country_iso: (0, pg_core_1.smallint)().notNull(),
    province_id: (0, pg_core_1.integer)().notNull(),
    city_id: (0, pg_core_1.integer)().notNull(),
    street: (0, pg_core_1.varchar)({ length: 200 }).notNull(),
    zip_code: (0, pg_core_1.varchar)({ length: 10 }).notNull(),
    latitude: (0, pg_core_1.doublePrecision)(),
    longitude: (0, pg_core_1.doublePrecision)(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.city_id],
        foreignColumns: [exports.cities.id],
        name: "directions_city_id_fkey"
    }).onDelete("restrict"),
    (0, pg_core_1.foreignKey)({
        columns: [table.country_iso],
        foreignColumns: [exports.countries.iso_numeric],
        name: "directions_country_iso_fkey"
    }).onUpdate("cascade").onDelete("restrict"),
    (0, pg_core_1.foreignKey)({
        columns: [table.province_id],
        foreignColumns: [exports.provinces.id],
        name: "directions_province_id_fkey"
    }).onDelete("restrict"),
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "directions_user_id_fkey"
    }).onDelete("cascade"),
]);
exports.delivery_timeline = (0, pg_core_1.pgTable)("delivery_timeline", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "delivery_timeline_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    delivery_id: (0, pg_core_1.bigint)({ mode: "number" }),
    status: (0, exports.DeliveryStatus)().notNull(),
    notes: (0, pg_core_1.text)(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    latitude: (0, pg_core_1.doublePrecision)().notNull(),
    longitude: (0, pg_core_1.doublePrecision)().notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.delivery_id],
        foreignColumns: [exports.deliveries.id],
        name: "delivery_timeline_delivery_id_fkey"
    }).onDelete("cascade"),
]);
exports.order_details = (0, pg_core_1.pgTable)("order_details", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "order_details_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    order_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    product_code: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    variant_product_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    quantity: (0, pg_core_1.integer)().notNull(),
    unit_price: (0, pg_core_1.numeric)({ precision: 10, scale: 2 }).notNull(),
    unit_price_iva: (0, pg_core_1.numeric)({ precision: 10, scale: 2 }).notNull(),
    subtotal: (0, pg_core_1.numeric)({ precision: 12, scale: 2 }).notNull(),
    iva: (0, pg_core_1.numeric)({ precision: 10, scale: 2 }).notNull(),
    discount_applied: (0, pg_core_1.numeric)({ precision: 10, scale: 2 }).notNull(),
    attributes: (0, pg_core_1.jsonb)().notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.order_id],
        foreignColumns: [exports.orders.id],
        name: "order_details_order_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.variant_product_id],
        foreignColumns: [exports.variant_products.id],
        name: "order_details_variant_product_id_fkey"
    }).onDelete("cascade"),
]);
exports.messages = (0, pg_core_1.pgTable)("messages", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "messages_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    chat_panel_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    sender_id: (0, pg_core_1.uuid)().default((0, drizzle_orm_1.sql) `'00000000-0000-0000-0000-000000000000'`),
    reply_to: (0, pg_core_1.bigint)({ mode: "number" }).default((0, drizzle_orm_1.sql) `'-1'`),
    content: (0, pg_core_1.text)(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    is_read: (0, pg_core_1.boolean)().default(false).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.chat_panel_id],
        foreignColumns: [exports.chat_panels.id],
        name: "messages_chat_panel_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.reply_to],
        foreignColumns: [table.id],
        name: "messages_reply_to_fkey"
    }).onDelete("set default"),
    (0, pg_core_1.foreignKey)({
        columns: [table.sender_id],
        foreignColumns: [exports.users.id],
        name: "messages_sender_id_fkey"
    }).onDelete("set default"),
]);
exports.notifications = (0, pg_core_1.pgTable)("notifications", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "notifications_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    user_id: (0, pg_core_1.uuid)().notNull(),
    title: (0, pg_core_1.varchar)({ length: 100 }).notNull(),
    message: (0, pg_core_1.text)().notNull(),
    type: (0, pg_core_1.smallint)().notNull(),
    is_read: (0, pg_core_1.boolean)().default(false).notNull(),
    click_action: (0, pg_core_1.varchar)({ length: 255 }).notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "notifications_user_id_fkey"
    }).onDelete("cascade"),
]);
exports.user_sessions = (0, pg_core_1.pgTable)("user_sessions", {
    session_id: (0, pg_core_1.uuid)().default((0, drizzle_orm_1.sql) `uuid_generate_v4()`).primaryKey().notNull(),
    user_id: (0, pg_core_1.uuid)().notNull(),
    device_name: (0, pg_core_1.varchar)({ length: 150 }).notNull(),
    device_finger: (0, pg_core_1.varchar)({ length: 255 }).notNull(),
    user_agent: (0, pg_core_1.text)().notNull(),
    revoked: (0, pg_core_1.boolean)().default(false).notNull(),
    last_ip: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    refresh_token: (0, pg_core_1.text)(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    last_active: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "fk_user_sessions_user_id"
    }).onDelete("cascade"),
    (0, pg_core_1.unique)("unique_device_finger_user").on(table.user_id, table.device_finger),
]);
exports.verification_tokens = (0, pg_core_1.pgTable)("verification_tokens", {
    id: (0, pg_core_1.uuid)().default((0, drizzle_orm_1.sql) `uuid_generate_v4()`).primaryKey().notNull(),
    user_id: (0, pg_core_1.uuid)().notNull(),
    token: (0, pg_core_1.varchar)({ length: 255 }).notNull(),
    expires_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    is_used: (0, pg_core_1.boolean)().default(false).notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    attempts: (0, pg_core_1.smallint)().default(0).notNull(),
}, (table) => [
    (0, pg_core_1.index)("password_reset_tokens_token_idx").using("btree", table.token.asc().nullsLast().op("text_ops")),
    (0, pg_core_1.index)("password_reset_tokens_user_id_idx").using("btree", table.user_id.asc().nullsLast().op("uuid_ops")),
    (0, pg_core_1.index)("verification_tokens_user_id_token_idx").using("btree", table.user_id.asc().nullsLast().op("text_ops"), table.token.asc().nullsLast().op("text_ops")),
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "password_reset_tokens_user_id_fkey"
    }).onDelete("cascade"),
]);
exports.orders = (0, pg_core_1.pgTable)("orders", {
    id: (0, pg_core_1.bigint)({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "orders_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
    retailer_id: (0, pg_core_1.uuid)(),
    wholesaler_id: (0, pg_core_1.uuid)(),
    status: (0, pg_core_1.smallint)().default((0, drizzle_orm_1.sql) `'1'`).notNull(),
    payment_method: (0, pg_core_1.smallint)().notNull(),
    shipping_address: (0, pg_core_1.bigint)({ mode: "number" }),
    notes: (0, pg_core_1.varchar)({ length: 500 }),
    discount_total: (0, pg_core_1.numeric)({ precision: 12, scale: 2 }).notNull(),
    subtotal: (0, pg_core_1.numeric)({ precision: 12, scale: 2 }).notNull(),
    total: (0, pg_core_1.numeric)({ precision: 12, scale: 2 }).notNull(),
    iva_total: (0, pg_core_1.numeric)({ precision: 12, scale: 2 }).notNull(),
    discount_log: (0, pg_core_1.jsonb)().notNull(),
    estimated_date: (0, pg_core_1.timestamp)({ mode: 'string' }),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.retailer_id],
        foreignColumns: [exports.users.id],
        name: "orders_retailer_id_fkey"
    }).onDelete("set null"),
    (0, pg_core_1.foreignKey)({
        columns: [table.shipping_address],
        foreignColumns: [exports.directions.id],
        name: "orders_shipping_address_fkey"
    }).onUpdate("restrict").onDelete("restrict"),
    (0, pg_core_1.foreignKey)({
        columns: [table.wholesaler_id],
        foreignColumns: [exports.users.id],
        name: "orders_wholesaler_id_fkey"
    }).onDelete("set null"),
]);
exports.users = (0, pg_core_1.pgTable)("users", {
    id: (0, pg_core_1.uuid)().default((0, drizzle_orm_1.sql) `uuid_generate_v4()`).primaryKey().notNull(),
    user_id: (0, pg_core_1.text)(),
    first_name: (0, pg_core_1.varchar)({ length: 50 }),
    last_name: (0, pg_core_1.varchar)({ length: 60 }),
    username: (0, pg_core_1.varchar)({ length: 50 }),
    password: (0, pg_core_1.text)().notNull(),
    email: (0, pg_core_1.varchar)({ length: 255 }).notNull(),
    telephone: (0, pg_core_1.varchar)({ length: 25 }),
    status: (0, exports.UserStatus)().default('PENDING_VERIFICATION').notNull(),
    profile: (0, pg_core_1.jsonb)(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    role: (0, exports.UserRole)().notNull(),
    cif: (0, pg_core_1.varchar)({ length: 20 }),
    updated_by: (0, pg_core_1.uuid)(),
}, (table) => [
    (0, pg_core_1.unique)("users_user_id_key").on(table.user_id),
    (0, pg_core_1.unique)("users_username_key").on(table.username),
    (0, pg_core_1.unique)("users_email_key").on(table.email),
]);
exports.user_uploads = (0, pg_core_1.pgTable)("user_uploads", {
    user_id: (0, pg_core_1.uuid)().notNull(),
    file_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.file_id],
        foreignColumns: [exports.files.id],
        name: "user_uploads_file_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "user_uploads_user_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.primaryKey)({ columns: [table.user_id, table.file_id], name: "user_uploads_pkey" }),
]);
exports.chat_participants = (0, pg_core_1.pgTable)("chat_participants", {
    user_id: (0, pg_core_1.uuid)().notNull(),
    chat_panel_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.chat_panel_id],
        foreignColumns: [exports.chat_panels.id],
        name: "chat_participants_chat_panel_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.user_id],
        foreignColumns: [exports.users.id],
        name: "chat_participants_user_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.primaryKey)({ columns: [table.user_id, table.chat_panel_id], name: "chat_participants_pkey" }),
]);
exports.product_categories = (0, pg_core_1.pgTable)("product_categories", {
    product_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    category_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    is_primary: (0, pg_core_1.boolean)().default(false).notNull(),
}, (table) => [
    (0, pg_core_1.foreignKey)({
        columns: [table.category_id],
        foreignColumns: [exports.categories.id],
        name: "product_categories_category_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.product_id],
        foreignColumns: [exports.products.id],
        name: "product_categories_product_id_fkey"
    }).onDelete("cascade"),
    (0, pg_core_1.primaryKey)({ columns: [table.product_id, table.category_id], name: "product_categories_pkey" }),
]);
exports.category_translations = (0, pg_core_1.pgTable)("category_translations", {
    category_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    lang_code: (0, pg_core_1.varchar)({ length: 10 }).notNull(),
    name: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    name_unaccent: (0, pg_core_1.text)().generatedAlwaysAs((0, drizzle_orm_1.sql) `immutable_unaccent((name)::text)`),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    updated_by: (0, pg_core_1.uuid)(),
}, (table) => [
    (0, pg_core_1.index)("idx_category_translations_lang_code").using("btree", table.lang_code.asc().nullsLast().op("text_ops")),
    (0, pg_core_1.index)("idx_category_translations_name_unaccent").using("btree", (0, drizzle_orm_1.sql) `lower(name_unaccent)`),
    (0, pg_core_1.foreignKey)({
        columns: [table.updated_by],
        foreignColumns: [exports.users.id],
        name: "category_translations_updated_by_fkey"
    }),
    (0, pg_core_1.foreignKey)({
        columns: [table.category_id],
        foreignColumns: [exports.categories.id],
        name: "fk_category_translations_category_id"
    }).onDelete("cascade"),
    (0, pg_core_1.primaryKey)({ columns: [table.category_id, table.lang_code], name: "pk_category_translations" }),
]);
exports.product_translations = (0, pg_core_1.pgTable)("product_translations", {
    product_id: (0, pg_core_1.bigint)({ mode: "number" }).notNull(),
    lang_code: (0, pg_core_1.varchar)({ length: 10 }).notNull(),
    name: (0, pg_core_1.varchar)({ length: 50 }).notNull(),
    title: (0, pg_core_1.varchar)({ length: 100 }),
    description: (0, pg_core_1.text)(),
    name_unaccent: (0, pg_core_1.text)().generatedAlwaysAs((0, drizzle_orm_1.sql) `immutable_unaccent((name)::text)`),
    title_unaccent: (0, pg_core_1.text)().generatedAlwaysAs((0, drizzle_orm_1.sql) `immutable_unaccent((title)::text)`),
    created_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`).notNull(),
    updated_at: (0, pg_core_1.timestamp)({ mode: 'string' }).default((0, drizzle_orm_1.sql) `(now() AT TIME ZONE 'utc'::text)`),
    updated_by: (0, pg_core_1.uuid)(),
}, (table) => [
    (0, pg_core_1.index)("idx_product_translations_name_unaccent").using("btree", (0, drizzle_orm_1.sql) `lower(name_unaccent)`),
    (0, pg_core_1.index)("idx_product_translations_title_unaccent").using("btree", (0, drizzle_orm_1.sql) `lower(title_unaccent)`),
    (0, pg_core_1.foreignKey)({
        columns: [table.product_id],
        foreignColumns: [exports.products.id],
        name: "fk_product_translations_product_id"
    }).onDelete("cascade"),
    (0, pg_core_1.foreignKey)({
        columns: [table.updated_by],
        foreignColumns: [exports.users.id],
        name: "product_translations_updated_by_fkey"
    }),
    (0, pg_core_1.primaryKey)({ columns: [table.product_id, table.lang_code], name: "pk_product_translations" }),
]);
//# sourceMappingURL=schema.js.map