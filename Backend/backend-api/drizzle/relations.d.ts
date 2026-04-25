export declare const provincesRelations: import("drizzle-orm/relations").Relations<"provinces", {
    country: import("drizzle-orm/relations").One<"countries", true>;
    cities: import("drizzle-orm/relations").Many<"cities">;
    directions: import("drizzle-orm/relations").Many<"directions">;
}>;
export declare const countriesRelations: import("drizzle-orm/relations").Relations<"countries", {
    provinces: import("drizzle-orm/relations").Many<"provinces">;
    currency: import("drizzle-orm/relations").One<"currencies", false>;
    directions: import("drizzle-orm/relations").Many<"directions">;
}>;
export declare const currenciesRelations: import("drizzle-orm/relations").Relations<"currencies", {
    countries: import("drizzle-orm/relations").Many<"countries">;
}>;
export declare const variant_productsRelations: import("drizzle-orm/relations").Relations<"variant_products", {
    user_created_by: import("drizzle-orm/relations").One<"users", true>;
    product: import("drizzle-orm/relations").One<"products", true>;
    user_updated_by: import("drizzle-orm/relations").One<"users", false>;
    cart_details: import("drizzle-orm/relations").Many<"cart_details">;
    order_details: import("drizzle-orm/relations").Many<"order_details">;
}>;
export declare const usersRelations: import("drizzle-orm/relations").Relations<"users", {
    variant_products_created_by: import("drizzle-orm/relations").Many<"variant_products">;
    variant_products_updated_by: import("drizzle-orm/relations").Many<"variant_products">;
    categories_created_by: import("drizzle-orm/relations").Many<"categories">;
    categories_updated_by: import("drizzle-orm/relations").Many<"categories">;
    categories_user_id: import("drizzle-orm/relations").Many<"categories">;
    discounts: import("drizzle-orm/relations").Many<"discounts">;
    carts: import("drizzle-orm/relations").Many<"carts">;
    configurations: import("drizzle-orm/relations").Many<"configurations">;
    deliveries: import("drizzle-orm/relations").Many<"deliveries">;
    products_created_by: import("drizzle-orm/relations").Many<"products">;
    products_updated_by: import("drizzle-orm/relations").Many<"products">;
    products_user_id: import("drizzle-orm/relations").Many<"products">;
    directions: import("drizzle-orm/relations").Many<"directions">;
    messages: import("drizzle-orm/relations").Many<"messages">;
    notifications: import("drizzle-orm/relations").Many<"notifications">;
    user_sessions: import("drizzle-orm/relations").Many<"user_sessions">;
    verification_tokens: import("drizzle-orm/relations").Many<"verification_tokens">;
    orders_retailer_id: import("drizzle-orm/relations").Many<"orders">;
    orders_wholesaler_id: import("drizzle-orm/relations").Many<"orders">;
    user_uploads: import("drizzle-orm/relations").Many<"user_uploads">;
    chat_participants: import("drizzle-orm/relations").Many<"chat_participants">;
    category_translations: import("drizzle-orm/relations").Many<"category_translations">;
    product_translations: import("drizzle-orm/relations").Many<"product_translations">;
}>;
export declare const productsRelations: import("drizzle-orm/relations").Relations<"products", {
    variant_products: import("drizzle-orm/relations").Many<"variant_products">;
    products_files: import("drizzle-orm/relations").Many<"products_files">;
    user_created_by: import("drizzle-orm/relations").One<"users", true>;
    user_updated_by: import("drizzle-orm/relations").One<"users", false>;
    user_user_id: import("drizzle-orm/relations").One<"users", true>;
    product_categories: import("drizzle-orm/relations").Many<"product_categories">;
    product_translations: import("drizzle-orm/relations").Many<"product_translations">;
}>;
export declare const citiesRelations: import("drizzle-orm/relations").Relations<"cities", {
    province: import("drizzle-orm/relations").One<"provinces", true>;
    directions: import("drizzle-orm/relations").Many<"directions">;
}>;
export declare const cart_detailsRelations: import("drizzle-orm/relations").Relations<"cart_details", {
    cart: import("drizzle-orm/relations").One<"carts", true>;
    variant_product: import("drizzle-orm/relations").One<"variant_products", false>;
}>;
export declare const cartsRelations: import("drizzle-orm/relations").Relations<"carts", {
    cart_details: import("drizzle-orm/relations").Many<"cart_details">;
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const categoriesRelations: import("drizzle-orm/relations").Relations<"categories", {
    user_created_by: import("drizzle-orm/relations").One<"users", false>;
    category: import("drizzle-orm/relations").One<"categories", false>;
    categories: import("drizzle-orm/relations").Many<"categories">;
    user_updated_by: import("drizzle-orm/relations").One<"users", false>;
    user_user_id: import("drizzle-orm/relations").One<"users", false>;
    product_categories: import("drizzle-orm/relations").Many<"product_categories">;
    category_translations: import("drizzle-orm/relations").Many<"category_translations">;
}>;
export declare const discountsRelations: import("drizzle-orm/relations").Relations<"discounts", {
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const message_filesRelations: import("drizzle-orm/relations").Relations<"message_files", {
    file: import("drizzle-orm/relations").One<"files", true>;
    message: import("drizzle-orm/relations").One<"messages", true>;
}>;
export declare const filesRelations: import("drizzle-orm/relations").Relations<"files", {
    message_files: import("drizzle-orm/relations").Many<"message_files">;
    products_files: import("drizzle-orm/relations").Many<"products_files">;
    user_uploads: import("drizzle-orm/relations").Many<"user_uploads">;
}>;
export declare const messagesRelations: import("drizzle-orm/relations").Relations<"messages", {
    message_files: import("drizzle-orm/relations").Many<"message_files">;
    chat_panel: import("drizzle-orm/relations").One<"chat_panels", true>;
    message: import("drizzle-orm/relations").One<"messages", false>;
    messages: import("drizzle-orm/relations").Many<"messages">;
    user: import("drizzle-orm/relations").One<"users", false>;
}>;
export declare const products_filesRelations: import("drizzle-orm/relations").Relations<"products_files", {
    file: import("drizzle-orm/relations").One<"files", true>;
    product: import("drizzle-orm/relations").One<"products", true>;
}>;
export declare const configurationsRelations: import("drizzle-orm/relations").Relations<"configurations", {
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const deliveriesRelations: import("drizzle-orm/relations").Relations<"deliveries", {
    user: import("drizzle-orm/relations").One<"users", false>;
    delivery_timelines: import("drizzle-orm/relations").Many<"delivery_timeline">;
}>;
export declare const directionsRelations: import("drizzle-orm/relations").Relations<"directions", {
    city: import("drizzle-orm/relations").One<"cities", true>;
    country: import("drizzle-orm/relations").One<"countries", true>;
    province: import("drizzle-orm/relations").One<"provinces", true>;
    user: import("drizzle-orm/relations").One<"users", true>;
    orders: import("drizzle-orm/relations").Many<"orders">;
}>;
export declare const delivery_timelineRelations: import("drizzle-orm/relations").Relations<"delivery_timeline", {
    delivery: import("drizzle-orm/relations").One<"deliveries", false>;
}>;
export declare const order_detailsRelations: import("drizzle-orm/relations").Relations<"order_details", {
    order: import("drizzle-orm/relations").One<"orders", true>;
    variant_product: import("drizzle-orm/relations").One<"variant_products", true>;
}>;
export declare const ordersRelations: import("drizzle-orm/relations").Relations<"orders", {
    order_details: import("drizzle-orm/relations").Many<"order_details">;
    user_retailer_id: import("drizzle-orm/relations").One<"users", false>;
    direction: import("drizzle-orm/relations").One<"directions", false>;
    user_wholesaler_id: import("drizzle-orm/relations").One<"users", false>;
}>;
export declare const chat_panelsRelations: import("drizzle-orm/relations").Relations<"chat_panels", {
    messages: import("drizzle-orm/relations").Many<"messages">;
    chat_participants: import("drizzle-orm/relations").Many<"chat_participants">;
}>;
export declare const notificationsRelations: import("drizzle-orm/relations").Relations<"notifications", {
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const user_sessionsRelations: import("drizzle-orm/relations").Relations<"user_sessions", {
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const verification_tokensRelations: import("drizzle-orm/relations").Relations<"verification_tokens", {
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const user_uploadsRelations: import("drizzle-orm/relations").Relations<"user_uploads", {
    file: import("drizzle-orm/relations").One<"files", true>;
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const chat_participantsRelations: import("drizzle-orm/relations").Relations<"chat_participants", {
    chat_panel: import("drizzle-orm/relations").One<"chat_panels", true>;
    user: import("drizzle-orm/relations").One<"users", true>;
}>;
export declare const product_categoriesRelations: import("drizzle-orm/relations").Relations<"product_categories", {
    category: import("drizzle-orm/relations").One<"categories", true>;
    product: import("drizzle-orm/relations").One<"products", true>;
}>;
export declare const category_translationsRelations: import("drizzle-orm/relations").Relations<"category_translations", {
    user: import("drizzle-orm/relations").One<"users", false>;
    category: import("drizzle-orm/relations").One<"categories", true>;
}>;
export declare const product_translationsRelations: import("drizzle-orm/relations").Relations<"product_translations", {
    product: import("drizzle-orm/relations").One<"products", true>;
    user: import("drizzle-orm/relations").One<"users", false>;
}>;
