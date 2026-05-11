import { pgTable, foreignKey, serial, smallint, varchar, unique, char, check, bigint, timestamp, numeric, integer, jsonb, uuid, uniqueIndex, index, text, boolean, date, doublePrecision, primaryKey, pgSequence, pgEnum } from "drizzle-orm/pg-core"
import { sql } from "drizzle-orm"

export const AddressType = pgEnum("AddressType", ['DELIVERY', 'INVOICE', 'STORE'])
export const DeliveryStatus = pgEnum("DeliveryStatus", ['PENDING', 'IN_PROGRESS', 'COMPLETED'])
export const ProductStatus = pgEnum("ProductStatus", ['ACTIVE', 'INACTIVE'])
export const SaleVariant = pgEnum("SaleVariant", ['UNIT', 'BOX', 'PACK'])
export const UserRole = pgEnum("UserRole", ['WHOLESALER', 'RETAILER', 'SUPPORT', 'DELIVERY', 'WAREHOUSE', 'ADMIN', 'SUPERADMIN'])
export const UserStatus = pgEnum("UserStatus", ['PENDING_VERIFICATION', 'INACTIVE', 'ACTIVE', 'PENDING_REVIEW', 'APPROVED', 'BANNED'])

export const seq_deliveryman_id = pgSequence("seq_deliveryman_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })
export const seq_retailer_id = pgSequence("seq_retailer_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })
export const seq_support_id = pgSequence("seq_support_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })
export const seq_warehouse_id = pgSequence("seq_warehouse_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })
export const seq_wholesaler_id = pgSequence("seq_wholesaler_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })
export const seq_admin_id = pgSequence("seq_admin_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })
export const seq_superadmin_id = pgSequence("seq_superadmin_id", {  startWith: "1", increment: "1", minValue: "1", maxValue: "9223372036854775807", cache: "1", cycle: false })

export const provinces = pgTable("provinces", {
	id: serial().primaryKey().notNull(),
	country_iso: smallint().notNull(),
	name: varchar({ length: 100 }).notNull(),
	name_local: varchar({ length: 100 }).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.country_iso],
			foreignColumns: [countries.iso_numeric],
			name: "provinces_country_iso_fkey"
		}).onUpdate("cascade").onDelete("cascade"),
]);

export const countries = pgTable("countries", {
	iso_alpha2: char({ length: 2 }).notNull(),
	iso_alpha3: char({ length: 3 }).notNull(),
	iso_numeric: smallint().primaryKey().notNull(),
	name: varchar({ length: 100 }).notNull(),
	name_local: varchar({ length: 100 }).notNull(),
	currency_id: smallint(),
}, (table) => [
	foreignKey({
			columns: [table.currency_id],
			foreignColumns: [currencies.iso_numeric],
			name: "countries_currency_id_fkey"
		}).onUpdate("cascade").onDelete("restrict"),
	unique("countries_iso_alpha2_key").on(table.iso_alpha2),
	unique("countries_iso_alpha3_key").on(table.iso_alpha3),
	unique("countries_iso_numeric_key").on(table.iso_numeric),
]);

export const variant_products = pgTable("variant_products", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "variant_products_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	product_id: bigint({ mode: "number" }).notNull(),
	type_sale: SaleVariant().notNull(),
	price: numeric({ precision: 10, scale:  2 }).notNull(),
	price_iva: numeric({ precision: 10, scale:  2 }).notNull(),
	available_stock: integer().notNull(),
	sort: smallint().notNull(),
	attributes: jsonb(),
	status: ProductStatus().default('ACTIVE').notNull(),
	product_code: varchar({ length: 50 }).notNull(),
	reserved_stock: integer().default(0).notNull(),
	low_stock_threshold: integer().default(0).notNull(),
	sale_unit_qty: integer().default(1).notNull(),
	min_order_qty: integer().default(1).notNull(),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	created_by: uuid().notNull(),
	updated_by: uuid(),
}, (table) => [
	foreignKey({
			columns: [table.created_by],
			foreignColumns: [users.id],
			name: "variant_products_created_by_fkey"
		}),
	foreignKey({
			columns: [table.product_id],
			foreignColumns: [products.id],
			name: "variant_products_product_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.updated_by],
			foreignColumns: [users.id],
			name: "variant_products_updated_by_fkey"
		}),
	check("variant_available_stock_check", sql`available_stock >= 0`),
	check("variant_low_stock_threshold_check", sql`low_stock_threshold >= 0`),
	check("variant_min_order_qty_check", sql`min_order_qty >= 1`),
	check("variant_price_check", sql`price >= (0)::numeric`),
	check("variant_price_iva_check", sql`price_iva >= (0)::numeric`),
	check("variant_reserved_stock_check", sql`reserved_stock >= 0`),
	check("variant_sale_unit_qty_check", sql`sale_unit_qty >= 1`),
	check("variant_sort_check", sql`sort >= 0`),
]);

export const cities = pgTable("cities", {
	id: serial().primaryKey().notNull(),
	province_id: integer().notNull(),
	name: varchar({ length: 100 }).notNull(),
	name_local: varchar({ length: 100 }).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.province_id],
			foreignColumns: [provinces.id],
			name: "cities_province_id_fkey"
		}).onDelete("cascade"),
]);

export const cart_details = pgTable("cart_details", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "cart_details_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	cart_id: bigint({ mode: "number" }).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	variant_products_id: bigint({ mode: "number" }),
	quantity: integer().notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.cart_id],
			foreignColumns: [carts.id],
			name: "cart_details_cart_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.variant_products_id],
			foreignColumns: [variant_products.id],
			name: "cart_details_variant_products_id_fkey"
		}).onDelete("cascade"),
	check("cart_details_quantity_check", sql`quantity > 0`),
]);

export const categories = pgTable("categories", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "categories_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	user_id: uuid(),
	name: varchar({ length: 50 }).notNull(),
	iva: numeric({ precision: 5, scale:  2 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	parent_id: bigint({ mode: "number" }),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	level: smallint().notNull(),
	name_unaccent: text().generatedAlwaysAs(sql`immutable_unaccent((name)::text)`),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	created_by: uuid(),
	updated_by: uuid(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	version: bigint({ mode: "number" }).default(1).notNull(),
	deleted_at: timestamp({ mode: 'string' }),
}, (table) => [
	uniqueIndex("categories_name_unique_public").using("btree", table.name.asc().nullsLast().op("text_ops")).where(sql`((user_id IS NULL) AND (deleted_at IS NULL))`),
	uniqueIndex("categories_user_name_unique_private").using("btree", table.user_id.asc().nullsLast().op("text_ops"), table.name.asc().nullsLast().op("text_ops")).where(sql`((user_id IS NOT NULL) AND (deleted_at IS NULL))`),
	index("idx_categories_name_unaccent").using("btree", sql`lower(name_unaccent)`),
	index("idx_categories_name_unaccent_trgm").using("gin", table.name_unaccent.asc().nullsLast().op("gin_trgm_ops")).where(sql`(deleted_at IS NULL)`),
	index("idx_categories_parent_id").using("btree", table.parent_id.asc().nullsLast().op("int8_ops")).where(sql`(deleted_at IS NULL)`),
	index("idx_categories_private_user_level_name").using("btree", table.user_id.asc().nullsLast().op("uuid_ops"), table.level.asc().nullsLast().op("int2_ops"), table.name.asc().nullsLast().op("uuid_ops")).where(sql`((user_id IS NOT NULL) AND (deleted_at IS NULL))`),
	index("idx_categories_private_user_parent_name").using("btree", table.user_id.asc().nullsLast().op("uuid_ops"), table.parent_id.asc().nullsLast().op("uuid_ops"), table.name.asc().nullsLast().op("text_ops")).where(sql`((user_id IS NOT NULL) AND (deleted_at IS NULL))`),
	index("idx_categories_public_level_name").using("btree", table.level.asc().nullsLast().op("int2_ops"), table.name.asc().nullsLast().op("text_ops")).where(sql`((user_id IS NULL) AND (deleted_at IS NULL))`),
	index("idx_categories_public_parent_level_name_all").using("btree", table.parent_id.asc().nullsLast().op("int8_ops"), table.level.asc().nullsLast().op("text_ops"), table.name.asc().nullsLast().op("text_ops")).where(sql`(user_id IS NULL)`),
	index("idx_categories_public_parent_name").using("btree", table.parent_id.asc().nullsLast().op("int8_ops"), table.name.asc().nullsLast().op("text_ops")).where(sql`((user_id IS NULL) AND (deleted_at IS NULL))`),
	index("idx_categories_user_parent_level_name").using("btree", table.user_id.asc().nullsLast().op("text_ops"), table.parent_id.asc().nullsLast().op("int8_ops"), table.level.asc().nullsLast().op("text_ops"), table.name.asc().nullsLast().op("uuid_ops")),
	foreignKey({
			columns: [table.created_by],
			foreignColumns: [users.id],
			name: "categories_created_by_fkey"
		}).onDelete("set null"),
	foreignKey({
			columns: [table.parent_id],
			foreignColumns: [table.id],
			name: "categories_parent_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.updated_by],
			foreignColumns: [users.id],
			name: "categories_updated_by_fkey"
		}).onDelete("set null"),
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "categories_user_id_fkey"
		}).onDelete("cascade"),
	check("categories_iva_check", sql`iva >= (0)::numeric`),
	check("categories_level_check", sql`(level >= 1) AND (level <= 3)`),
]);

export const files = pgTable("files", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "files_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	file_name: varchar().notNull(),
	file_hash: varchar({ length: 64 }).notNull(),
	mime_type: varchar({ length: 128 }).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	file_size: bigint({ mode: "number" }).notNull(),
	storage_key: text().notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	to_delete: boolean().default(false).notNull(),
	cloud_synced: boolean().default(true).notNull(),
}, (table) => [
	index("idx_files_created_at").using("btree", table.created_at.asc().nullsLast().op("timestamp_ops")),
	unique("files_file_hash_key").on(table.file_hash),
	check("files_file_name_check", sql`(file_name)::text ~* '^[^\\/:\*\?"<>\|]{1,255}\.[a-z0-9]+$'::text`),
]);

export const chat_panels = pgTable("chat_panels", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "chat_panel_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	name: varchar({ length: 50 }).notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
});

export const discounts = pgTable("discounts", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "discounts_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	user_id: uuid().notNull(),
	name: varchar().notNull(),
	type_value: jsonb().notNull(),
	applies_to_all: boolean().default(false).notNull(),
	start_date: date().notNull(),
	end_date: date().notNull(),
	status: smallint().default(sql`'1'`).notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "discounts_user_id_fkey"
		}).onDelete("cascade"),
]);

export const message_files = pgTable("message_files", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "message_files_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	message_id: bigint({ mode: "number" }).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	file_id: bigint({ mode: "number" }).notNull(),
	sort: smallint().notNull(),
}, (table) => [
	index("idx_message_files_file_id").using("btree", table.file_id.asc().nullsLast().op("int8_ops")),
	foreignKey({
			columns: [table.file_id],
			foreignColumns: [files.id],
			name: "message_files_file_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.message_id],
			foreignColumns: [messages.id],
			name: "message_files_message_id_fkey"
		}).onDelete("cascade"),
]);

export const products_files = pgTable("products_files", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "products_files_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	product_id: bigint({ mode: "number" }).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	file_id: bigint({ mode: "number" }).notNull(),
	sort: smallint().notNull(),
}, (table) => [
	index("idx_products_files_file_id").using("btree", table.file_id.asc().nullsLast().op("int8_ops")),
	index("idx_products_files_product_id").using("btree", table.product_id.asc().nullsLast().op("int8_ops")),
	foreignKey({
			columns: [table.file_id],
			foreignColumns: [files.id],
			name: "products_files_file_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.product_id],
			foreignColumns: [products.id],
			name: "products_files_product_id_fkey"
		}).onDelete("cascade"),
]);

export const carts = pgTable("carts", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "cart_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	user_id: uuid().notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "carts_user_id_fkey"
		}).onDelete("cascade"),
]);

export const configurations = pgTable("configurations", {
	user_id: uuid().primaryKey().notNull(),
	language: varchar({ length: 10 }).default('en').notNull(),
	timezone: varchar({ length: 32 }).default('UTC').notNull(),
}, (table) => [
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "configurations_user_id_fkey"
		}).onDelete("cascade"),
]);

export const deliveries = pgTable("deliveries", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "deliveries_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	order_id: bigint({ mode: "number" }).notNull(),
	delivery_person: uuid(),
	status: DeliveryStatus().default('PENDING').notNull(),
	start_time: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	end_time: timestamp({ mode: 'string' }),
	notes: text().notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	updated_at: timestamp({ mode: 'string' }),
	latitude: doublePrecision().notNull(),
	longitude: doublePrecision().notNull(),
}, (table) => [
	foreignKey({
			columns: [table.delivery_person],
			foreignColumns: [users.id],
			name: "deliveries_delivery_person_fkey"
		}).onDelete("set null"),
]);

export const products = pgTable("products", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "products_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	user_id: uuid().notNull(),
	name: varchar({ length: 50 }).notNull(),
	title: varchar({ length: 100 }),
	description: text(),
	iva: numeric({ precision: 5, scale:  2 }).notNull(),
	status: ProductStatus().default('ACTIVE').notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	product_code: varchar({ length: 50 }).notNull(),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	created_by: uuid().notNull(),
	updated_by: uuid(),
	name_unaccent: text().generatedAlwaysAs(sql`immutable_unaccent((name)::text)`),
	title_unaccent: text().generatedAlwaysAs(sql`immutable_unaccent((title)::text)`),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	version: bigint({ mode: "number" }).default(1).notNull(),
	deleted_at: timestamp({ mode: 'string' }),
}, (table) => [
	index("idx_products_name_unaccent").using("btree", sql`lower(name_unaccent)`),
	index("idx_products_title_unaccent").using("btree", sql`lower(title_unaccent)`),
	index("idx_products_user_id_id").using("btree", table.user_id.asc().nullsLast().op("int8_ops"), table.id.asc().nullsLast().op("uuid_ops")),
	index("idx_products_user_status_id").using("btree", table.user_id.asc().nullsLast().op("uuid_ops"), table.status.asc().nullsLast().op("uuid_ops"), table.id.asc().nullsLast().op("enum_ops")),
	foreignKey({
			columns: [table.created_by],
			foreignColumns: [users.id],
			name: "products_created_by_fkey"
		}),
	foreignKey({
			columns: [table.updated_by],
			foreignColumns: [users.id],
			name: "products_updated_by_fkey"
		}),
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "products_user_id_fkey"
		}).onDelete("cascade"),
	check("products_iva_check", sql`iva >= (0)::numeric`),
]);

export const currencies = pgTable("currencies", {
	iso_numeric: smallint().primaryKey().notNull(),
	iso_alpha3: char({ length: 3 }).notNull(),
	symbol: varchar({ length: 5 }).notNull(),
	decimal_digits: smallint().notNull(),
}, (table) => [
	unique("currencies_iso_alpha3_key").on(table.iso_alpha3),
]);

export const directions = pgTable("directions", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "directions_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	user_id: uuid().notNull(),
	type: AddressType().default('STORE').notNull(),
	country_iso: smallint().notNull(),
	province_id: integer().notNull(),
	city_id: integer().notNull(),
	street: varchar({ length: 200 }).notNull(),
	zip_code: varchar({ length: 10 }).notNull(),
	latitude: doublePrecision(),
	longitude: doublePrecision(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	updated_at: timestamp({ mode: 'string' }),
}, (table) => [
	foreignKey({
			columns: [table.city_id],
			foreignColumns: [cities.id],
			name: "directions_city_id_fkey"
		}).onDelete("restrict"),
	foreignKey({
			columns: [table.country_iso],
			foreignColumns: [countries.iso_numeric],
			name: "directions_country_iso_fkey"
		}).onUpdate("cascade").onDelete("restrict"),
	foreignKey({
			columns: [table.province_id],
			foreignColumns: [provinces.id],
			name: "directions_province_id_fkey"
		}).onDelete("restrict"),
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "directions_user_id_fkey"
		}).onDelete("cascade"),
]);

export const delivery_timeline = pgTable("delivery_timeline", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "delivery_timeline_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	delivery_id: bigint({ mode: "number" }),
	status: DeliveryStatus().notNull(),
	notes: text(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	latitude: doublePrecision().notNull(),
	longitude: doublePrecision().notNull(),
}, (table) => [
	foreignKey({
			columns: [table.delivery_id],
			foreignColumns: [deliveries.id],
			name: "delivery_timeline_delivery_id_fkey"
		}).onDelete("cascade"),
]);

export const order_details = pgTable("order_details", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "order_details_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	order_id: bigint({ mode: "number" }).notNull(),
	product_code: varchar({ length: 50 }).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	variant_product_id: bigint({ mode: "number" }).notNull(),
	quantity: integer().notNull(),
	unit_price: numeric({ precision: 10, scale:  2 }).notNull(),
	unit_price_iva: numeric({ precision: 10, scale:  2 }).notNull(),
	subtotal: numeric({ precision: 12, scale:  2 }).notNull(),
	iva: numeric({ precision: 10, scale:  2 }).notNull(),
	discount_applied: numeric({ precision: 10, scale:  2 }).notNull(),
	attributes: jsonb().notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.order_id],
			foreignColumns: [orders.id],
			name: "order_details_order_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.variant_product_id],
			foreignColumns: [variant_products.id],
			name: "order_details_variant_product_id_fkey"
		}).onDelete("cascade"),
]);

export const messages = pgTable("messages", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "messages_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	chat_panel_id: bigint({ mode: "number" }).notNull(),
	sender_id: uuid().default(sql`'00000000-0000-0000-0000-000000000000'`),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	reply_to: bigint({ mode: "number" }).default(sql`'-1'`),
	content: text(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	is_read: boolean().default(false).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.chat_panel_id],
			foreignColumns: [chat_panels.id],
			name: "messages_chat_panel_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.reply_to],
			foreignColumns: [table.id],
			name: "messages_reply_to_fkey"
		}).onDelete("set default"),
	foreignKey({
			columns: [table.sender_id],
			foreignColumns: [users.id],
			name: "messages_sender_id_fkey"
		}).onDelete("set default"),
]);

export const notifications = pgTable("notifications", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "notifications_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	user_id: uuid().notNull(),
	title: varchar({ length: 100 }).notNull(),
	message: text().notNull(),
	type: smallint().notNull(),
	is_read: boolean().default(false).notNull(),
	click_action: varchar({ length: 255 }).notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "notifications_user_id_fkey"
		}).onDelete("cascade"),
]);

export const user_sessions = pgTable("user_sessions", {
	session_id: uuid().default(sql`uuid_generate_v4()`).primaryKey().notNull(),
	user_id: uuid().notNull(),
	device_name: varchar({ length: 150 }).notNull(),
	device_finger: varchar({ length: 255 }).notNull(),
	user_agent: text().notNull(),
	revoked: boolean().default(false).notNull(),
	last_ip: varchar({ length: 50 }).notNull(),
	refresh_token: text(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	last_active: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "fk_user_sessions_user_id"
		}).onDelete("cascade"),
	unique("unique_device_finger_user").on(table.user_id, table.device_finger),
]);

export const verification_tokens = pgTable("verification_tokens", {
	id: uuid().default(sql`uuid_generate_v4()`).primaryKey().notNull(),
	user_id: uuid().notNull(),
	token: varchar({ length: 255 }).notNull(),
	expires_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	is_used: boolean().default(false).notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	attempts: smallint().default(0).notNull(),
}, (table) => [
	index("password_reset_tokens_token_idx").using("btree", table.token.asc().nullsLast().op("text_ops")),
	index("password_reset_tokens_user_id_idx").using("btree", table.user_id.asc().nullsLast().op("uuid_ops")),
	index("verification_tokens_user_id_token_idx").using("btree", table.user_id.asc().nullsLast().op("text_ops"), table.token.asc().nullsLast().op("text_ops")),
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "password_reset_tokens_user_id_fkey"
		}).onDelete("cascade"),
]);

export const orders = pgTable("orders", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	id: bigint({ mode: "number" }).primaryKey().generatedByDefaultAsIdentity({ name: "orders_id_seq", startWith: 1, increment: 1, minValue: 1, maxValue: 9223372036854775807, cache: 1 }),
	retailer_id: uuid(),
	wholesaler_id: uuid(),
	status: smallint().default(sql`'1'`).notNull(),
	payment_method: smallint().notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	shipping_address: bigint({ mode: "number" }),
	notes: varchar({ length: 500 }),
	discount_total: numeric({ precision: 12, scale:  2 }).notNull(),
	subtotal: numeric({ precision: 12, scale:  2 }).notNull(),
	total: numeric({ precision: 12, scale:  2 }).notNull(),
	iva_total: numeric({ precision: 12, scale:  2 }).notNull(),
	discount_log: jsonb().notNull(),
	estimated_date: timestamp({ mode: 'string' }),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
}, (table) => [
	foreignKey({
			columns: [table.retailer_id],
			foreignColumns: [users.id],
			name: "orders_retailer_id_fkey"
		}).onDelete("set null"),
	foreignKey({
			columns: [table.shipping_address],
			foreignColumns: [directions.id],
			name: "orders_shipping_address_fkey"
		}).onUpdate("restrict").onDelete("restrict"),
	foreignKey({
			columns: [table.wholesaler_id],
			foreignColumns: [users.id],
			name: "orders_wholesaler_id_fkey"
		}).onDelete("set null"),
]);

export const users = pgTable("users", {
	id: uuid().default(sql`uuid_generate_v4()`).primaryKey().notNull(),
	user_id: text(),
	first_name: varchar({ length: 50 }),
	last_name: varchar({ length: 60 }),
	username: varchar({ length: 50 }),
	password: text().notNull(),
	email: varchar({ length: 255 }).notNull(),
	telephone: varchar({ length: 25 }),
	status: UserStatus().default('PENDING_VERIFICATION').notNull(),
	profile: jsonb(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	role: UserRole().notNull(),
	tax_id: varchar({ length: 20 }),
	updated_by: uuid(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	profile_image_file_id: bigint({ mode: "number" }),
}, (table) => [
	foreignKey({
			columns: [table.profile_image_file_id],
			foreignColumns: [files.id],
			name: "users_profile_image_file_id_fkey"
		}).onDelete("set null"),
	unique("users_user_id_key").on(table.user_id),
	unique("users_username_key").on(table.username),
	unique("users_email_key").on(table.email),
]);

export const user_uploads = pgTable("user_uploads", {
	user_id: uuid().notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	file_id: bigint({ mode: "number" }).notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
}, (table) => [
	foreignKey({
			columns: [table.file_id],
			foreignColumns: [files.id],
			name: "user_uploads_file_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "user_uploads_user_id_fkey"
		}).onDelete("cascade"),
	primaryKey({ columns: [table.user_id, table.file_id], name: "user_uploads_pkey"}),
]);

export const chat_participants = pgTable("chat_participants", {
	user_id: uuid().notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	chat_panel_id: bigint({ mode: "number" }).notNull(),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
}, (table) => [
	foreignKey({
			columns: [table.chat_panel_id],
			foreignColumns: [chat_panels.id],
			name: "chat_participants_chat_panel_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.user_id],
			foreignColumns: [users.id],
			name: "chat_participants_user_id_fkey"
		}).onDelete("cascade"),
	primaryKey({ columns: [table.user_id, table.chat_panel_id], name: "chat_participants_pkey"}),
]);

export const product_categories = pgTable("product_categories", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	product_id: bigint({ mode: "number" }).notNull(),
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	category_id: bigint({ mode: "number" }).notNull(),
	is_primary: boolean().default(false).notNull(),
}, (table) => [
	index("idx_product_categories_category_id").using("btree", table.category_id.asc().nullsLast().op("int8_ops")),
	index("idx_product_categories_category_product").using("btree", table.category_id.asc().nullsLast().op("int8_ops"), table.product_id.asc().nullsLast().op("int8_ops")),
	index("idx_product_categories_product_id").using("btree", table.product_id.asc().nullsLast().op("int8_ops")),
	foreignKey({
			columns: [table.category_id],
			foreignColumns: [categories.id],
			name: "product_categories_category_id_fkey"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.product_id],
			foreignColumns: [products.id],
			name: "product_categories_product_id_fkey"
		}).onDelete("cascade"),
	primaryKey({ columns: [table.product_id, table.category_id], name: "product_categories_pkey"}),
]);

export const category_translations = pgTable("category_translations", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	category_id: bigint({ mode: "number" }).notNull(),
	lang_code: varchar({ length: 10 }).notNull(),
	name: varchar({ length: 50 }).notNull(),
	name_unaccent: text().generatedAlwaysAs(sql`immutable_unaccent((name)::text)`),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	updated_by: uuid(),
}, (table) => [
	index("idx_category_translations_lang_code").using("btree", table.lang_code.asc().nullsLast().op("text_ops")),
	index("idx_category_translations_name_unaccent").using("btree", sql`lower(name_unaccent)`),
	index("idx_category_translations_name_unaccent_trgm").using("gin", table.name_unaccent.asc().nullsLast().op("gin_trgm_ops")),
	foreignKey({
			columns: [table.updated_by],
			foreignColumns: [users.id],
			name: "category_translations_updated_by_fkey"
		}),
	foreignKey({
			columns: [table.category_id],
			foreignColumns: [categories.id],
			name: "fk_category_translations_category_id"
		}).onDelete("cascade"),
	primaryKey({ columns: [table.category_id, table.lang_code], name: "pk_category_translations"}),
]);

export const product_translations = pgTable("product_translations", {
	// You can use { mode: "bigint" } if numbers are exceeding js number limitations
	product_id: bigint({ mode: "number" }).notNull(),
	lang_code: varchar({ length: 10 }).notNull(),
	name: varchar({ length: 50 }).notNull(),
	title: varchar({ length: 100 }),
	description: text(),
	name_unaccent: text().generatedAlwaysAs(sql`immutable_unaccent((name)::text)`),
	title_unaccent: text().generatedAlwaysAs(sql`immutable_unaccent((title)::text)`),
	created_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`).notNull(),
	updated_at: timestamp({ mode: 'string' }).default(sql`(now() AT TIME ZONE 'utc'::text)`),
	updated_by: uuid(),
}, (table) => [
	index("idx_product_translations_name_unaccent").using("btree", sql`lower(name_unaccent)`),
	index("idx_product_translations_title_unaccent").using("btree", sql`lower(title_unaccent)`),
	foreignKey({
			columns: [table.product_id],
			foreignColumns: [products.id],
			name: "fk_product_translations_product_id"
		}).onDelete("cascade"),
	foreignKey({
			columns: [table.updated_by],
			foreignColumns: [users.id],
			name: "product_translations_updated_by_fkey"
		}),
	primaryKey({ columns: [table.product_id, table.lang_code], name: "pk_product_translations"}),
]);
