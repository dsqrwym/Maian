"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.product_translationsRelations = exports.category_translationsRelations = exports.product_categoriesRelations = exports.chat_participantsRelations = exports.user_uploadsRelations = exports.verification_tokensRelations = exports.user_sessionsRelations = exports.notificationsRelations = exports.chat_panelsRelations = exports.ordersRelations = exports.order_detailsRelations = exports.delivery_timelineRelations = exports.directionsRelations = exports.deliveriesRelations = exports.configurationsRelations = exports.products_filesRelations = exports.messagesRelations = exports.filesRelations = exports.message_filesRelations = exports.discountsRelations = exports.categoriesRelations = exports.cartsRelations = exports.cart_detailsRelations = exports.citiesRelations = exports.productsRelations = exports.usersRelations = exports.variant_productsRelations = exports.currenciesRelations = exports.countriesRelations = exports.provincesRelations = void 0;
const relations_1 = require("drizzle-orm/relations");
const schema_1 = require("./schema");
exports.provincesRelations = (0, relations_1.relations)(schema_1.provinces, ({ one, many }) => ({
    country: one(schema_1.countries, {
        fields: [schema_1.provinces.country_iso],
        references: [schema_1.countries.iso_numeric]
    }),
    cities: many(schema_1.cities),
    directions: many(schema_1.directions),
}));
exports.countriesRelations = (0, relations_1.relations)(schema_1.countries, ({ one, many }) => ({
    provinces: many(schema_1.provinces),
    currency: one(schema_1.currencies, {
        fields: [schema_1.countries.currency_id],
        references: [schema_1.currencies.iso_numeric]
    }),
    directions: many(schema_1.directions),
}));
exports.currenciesRelations = (0, relations_1.relations)(schema_1.currencies, ({ many }) => ({
    countries: many(schema_1.countries),
}));
exports.variant_productsRelations = (0, relations_1.relations)(schema_1.variant_products, ({ one, many }) => ({
    user_created_by: one(schema_1.users, {
        fields: [schema_1.variant_products.created_by],
        references: [schema_1.users.id],
        relationName: "variant_products_created_by_users_id"
    }),
    product: one(schema_1.products, {
        fields: [schema_1.variant_products.product_id],
        references: [schema_1.products.id]
    }),
    user_updated_by: one(schema_1.users, {
        fields: [schema_1.variant_products.updated_by],
        references: [schema_1.users.id],
        relationName: "variant_products_updated_by_users_id"
    }),
    cart_details: many(schema_1.cart_details),
    order_details: many(schema_1.order_details),
}));
exports.usersRelations = (0, relations_1.relations)(schema_1.users, ({ many }) => ({
    variant_products_created_by: many(schema_1.variant_products, {
        relationName: "variant_products_created_by_users_id"
    }),
    variant_products_updated_by: many(schema_1.variant_products, {
        relationName: "variant_products_updated_by_users_id"
    }),
    categories_created_by: many(schema_1.categories, {
        relationName: "categories_created_by_users_id"
    }),
    categories_updated_by: many(schema_1.categories, {
        relationName: "categories_updated_by_users_id"
    }),
    categories_user_id: many(schema_1.categories, {
        relationName: "categories_user_id_users_id"
    }),
    discounts: many(schema_1.discounts),
    carts: many(schema_1.carts),
    configurations: many(schema_1.configurations),
    deliveries: many(schema_1.deliveries),
    products_created_by: many(schema_1.products, {
        relationName: "products_created_by_users_id"
    }),
    products_updated_by: many(schema_1.products, {
        relationName: "products_updated_by_users_id"
    }),
    products_user_id: many(schema_1.products, {
        relationName: "products_user_id_users_id"
    }),
    directions: many(schema_1.directions),
    messages: many(schema_1.messages),
    notifications: many(schema_1.notifications),
    user_sessions: many(schema_1.user_sessions),
    verification_tokens: many(schema_1.verification_tokens),
    orders_retailer_id: many(schema_1.orders, {
        relationName: "orders_retailer_id_users_id"
    }),
    orders_wholesaler_id: many(schema_1.orders, {
        relationName: "orders_wholesaler_id_users_id"
    }),
    user_uploads: many(schema_1.user_uploads),
    chat_participants: many(schema_1.chat_participants),
    category_translations: many(schema_1.category_translations),
    product_translations: many(schema_1.product_translations),
}));
exports.productsRelations = (0, relations_1.relations)(schema_1.products, ({ one, many }) => ({
    variant_products: many(schema_1.variant_products),
    products_files: many(schema_1.products_files),
    user_created_by: one(schema_1.users, {
        fields: [schema_1.products.created_by],
        references: [schema_1.users.id],
        relationName: "products_created_by_users_id"
    }),
    user_updated_by: one(schema_1.users, {
        fields: [schema_1.products.updated_by],
        references: [schema_1.users.id],
        relationName: "products_updated_by_users_id"
    }),
    user_user_id: one(schema_1.users, {
        fields: [schema_1.products.user_id],
        references: [schema_1.users.id],
        relationName: "products_user_id_users_id"
    }),
    product_categories: many(schema_1.product_categories),
    product_translations: many(schema_1.product_translations),
}));
exports.citiesRelations = (0, relations_1.relations)(schema_1.cities, ({ one, many }) => ({
    province: one(schema_1.provinces, {
        fields: [schema_1.cities.province_id],
        references: [schema_1.provinces.id]
    }),
    directions: many(schema_1.directions),
}));
exports.cart_detailsRelations = (0, relations_1.relations)(schema_1.cart_details, ({ one }) => ({
    cart: one(schema_1.carts, {
        fields: [schema_1.cart_details.cart_id],
        references: [schema_1.carts.id]
    }),
    variant_product: one(schema_1.variant_products, {
        fields: [schema_1.cart_details.variant_products_id],
        references: [schema_1.variant_products.id]
    }),
}));
exports.cartsRelations = (0, relations_1.relations)(schema_1.carts, ({ one, many }) => ({
    cart_details: many(schema_1.cart_details),
    user: one(schema_1.users, {
        fields: [schema_1.carts.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.categoriesRelations = (0, relations_1.relations)(schema_1.categories, ({ one, many }) => ({
    user_created_by: one(schema_1.users, {
        fields: [schema_1.categories.created_by],
        references: [schema_1.users.id],
        relationName: "categories_created_by_users_id"
    }),
    category: one(schema_1.categories, {
        fields: [schema_1.categories.parent_id],
        references: [schema_1.categories.id],
        relationName: "categories_parent_id_categories_id"
    }),
    categories: many(schema_1.categories, {
        relationName: "categories_parent_id_categories_id"
    }),
    user_updated_by: one(schema_1.users, {
        fields: [schema_1.categories.updated_by],
        references: [schema_1.users.id],
        relationName: "categories_updated_by_users_id"
    }),
    user_user_id: one(schema_1.users, {
        fields: [schema_1.categories.user_id],
        references: [schema_1.users.id],
        relationName: "categories_user_id_users_id"
    }),
    product_categories: many(schema_1.product_categories),
    category_translations: many(schema_1.category_translations),
}));
exports.discountsRelations = (0, relations_1.relations)(schema_1.discounts, ({ one }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.discounts.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.message_filesRelations = (0, relations_1.relations)(schema_1.message_files, ({ one }) => ({
    file: one(schema_1.files, {
        fields: [schema_1.message_files.file_id],
        references: [schema_1.files.id]
    }),
    message: one(schema_1.messages, {
        fields: [schema_1.message_files.message_id],
        references: [schema_1.messages.id]
    }),
}));
exports.filesRelations = (0, relations_1.relations)(schema_1.files, ({ many }) => ({
    message_files: many(schema_1.message_files),
    products_files: many(schema_1.products_files),
    user_uploads: many(schema_1.user_uploads),
}));
exports.messagesRelations = (0, relations_1.relations)(schema_1.messages, ({ one, many }) => ({
    message_files: many(schema_1.message_files),
    chat_panel: one(schema_1.chat_panels, {
        fields: [schema_1.messages.chat_panel_id],
        references: [schema_1.chat_panels.id]
    }),
    message: one(schema_1.messages, {
        fields: [schema_1.messages.reply_to],
        references: [schema_1.messages.id],
        relationName: "messages_reply_to_messages_id"
    }),
    messages: many(schema_1.messages, {
        relationName: "messages_reply_to_messages_id"
    }),
    user: one(schema_1.users, {
        fields: [schema_1.messages.sender_id],
        references: [schema_1.users.id]
    }),
}));
exports.products_filesRelations = (0, relations_1.relations)(schema_1.products_files, ({ one }) => ({
    file: one(schema_1.files, {
        fields: [schema_1.products_files.file_id],
        references: [schema_1.files.id]
    }),
    product: one(schema_1.products, {
        fields: [schema_1.products_files.product_id],
        references: [schema_1.products.id]
    }),
}));
exports.configurationsRelations = (0, relations_1.relations)(schema_1.configurations, ({ one }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.configurations.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.deliveriesRelations = (0, relations_1.relations)(schema_1.deliveries, ({ one, many }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.deliveries.delivery_person],
        references: [schema_1.users.id]
    }),
    delivery_timelines: many(schema_1.delivery_timeline),
}));
exports.directionsRelations = (0, relations_1.relations)(schema_1.directions, ({ one, many }) => ({
    city: one(schema_1.cities, {
        fields: [schema_1.directions.city_id],
        references: [schema_1.cities.id]
    }),
    country: one(schema_1.countries, {
        fields: [schema_1.directions.country_iso],
        references: [schema_1.countries.iso_numeric]
    }),
    province: one(schema_1.provinces, {
        fields: [schema_1.directions.province_id],
        references: [schema_1.provinces.id]
    }),
    user: one(schema_1.users, {
        fields: [schema_1.directions.user_id],
        references: [schema_1.users.id]
    }),
    orders: many(schema_1.orders),
}));
exports.delivery_timelineRelations = (0, relations_1.relations)(schema_1.delivery_timeline, ({ one }) => ({
    delivery: one(schema_1.deliveries, {
        fields: [schema_1.delivery_timeline.delivery_id],
        references: [schema_1.deliveries.id]
    }),
}));
exports.order_detailsRelations = (0, relations_1.relations)(schema_1.order_details, ({ one }) => ({
    order: one(schema_1.orders, {
        fields: [schema_1.order_details.order_id],
        references: [schema_1.orders.id]
    }),
    variant_product: one(schema_1.variant_products, {
        fields: [schema_1.order_details.variant_product_id],
        references: [schema_1.variant_products.id]
    }),
}));
exports.ordersRelations = (0, relations_1.relations)(schema_1.orders, ({ one, many }) => ({
    order_details: many(schema_1.order_details),
    user_retailer_id: one(schema_1.users, {
        fields: [schema_1.orders.retailer_id],
        references: [schema_1.users.id],
        relationName: "orders_retailer_id_users_id"
    }),
    direction: one(schema_1.directions, {
        fields: [schema_1.orders.shipping_address],
        references: [schema_1.directions.id]
    }),
    user_wholesaler_id: one(schema_1.users, {
        fields: [schema_1.orders.wholesaler_id],
        references: [schema_1.users.id],
        relationName: "orders_wholesaler_id_users_id"
    }),
}));
exports.chat_panelsRelations = (0, relations_1.relations)(schema_1.chat_panels, ({ many }) => ({
    messages: many(schema_1.messages),
    chat_participants: many(schema_1.chat_participants),
}));
exports.notificationsRelations = (0, relations_1.relations)(schema_1.notifications, ({ one }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.notifications.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.user_sessionsRelations = (0, relations_1.relations)(schema_1.user_sessions, ({ one }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.user_sessions.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.verification_tokensRelations = (0, relations_1.relations)(schema_1.verification_tokens, ({ one }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.verification_tokens.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.user_uploadsRelations = (0, relations_1.relations)(schema_1.user_uploads, ({ one }) => ({
    file: one(schema_1.files, {
        fields: [schema_1.user_uploads.file_id],
        references: [schema_1.files.id]
    }),
    user: one(schema_1.users, {
        fields: [schema_1.user_uploads.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.chat_participantsRelations = (0, relations_1.relations)(schema_1.chat_participants, ({ one }) => ({
    chat_panel: one(schema_1.chat_panels, {
        fields: [schema_1.chat_participants.chat_panel_id],
        references: [schema_1.chat_panels.id]
    }),
    user: one(schema_1.users, {
        fields: [schema_1.chat_participants.user_id],
        references: [schema_1.users.id]
    }),
}));
exports.product_categoriesRelations = (0, relations_1.relations)(schema_1.product_categories, ({ one }) => ({
    category: one(schema_1.categories, {
        fields: [schema_1.product_categories.category_id],
        references: [schema_1.categories.id]
    }),
    product: one(schema_1.products, {
        fields: [schema_1.product_categories.product_id],
        references: [schema_1.products.id]
    }),
}));
exports.category_translationsRelations = (0, relations_1.relations)(schema_1.category_translations, ({ one }) => ({
    user: one(schema_1.users, {
        fields: [schema_1.category_translations.updated_by],
        references: [schema_1.users.id]
    }),
    category: one(schema_1.categories, {
        fields: [schema_1.category_translations.category_id],
        references: [schema_1.categories.id]
    }),
}));
exports.product_translationsRelations = (0, relations_1.relations)(schema_1.product_translations, ({ one }) => ({
    product: one(schema_1.products, {
        fields: [schema_1.product_translations.product_id],
        references: [schema_1.products.id]
    }),
    user: one(schema_1.users, {
        fields: [schema_1.product_translations.updated_by],
        references: [schema_1.users.id]
    }),
}));
//# sourceMappingURL=relations.js.map