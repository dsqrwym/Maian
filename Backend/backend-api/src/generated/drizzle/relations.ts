import { relations } from 'drizzle-orm/relations';
import {
  countries,
  provinces,
  currencies,
  users,
  categories,
  cities,
  variant_products,
  products,
  discounts,
  files,
  message_files,
  messages,
  products_files,
  configurations,
  deliveries,
  directions,
  delivery_timeline,
  chat_panels,
  notifications,
  user_sessions,
  verification_tokens,
  carts,
  cart_details,
  orders,
  order_details,
  wholesaler_staffs,
  user_uploads,
  chat_participants,
  product_categories,
  document_sequences,
  order_pdf_files,
  category_translations,
  product_translations,
} from './schema.js';

export const provincesRelations = relations(provinces, ({ one, many }) => ({
  country: one(countries, {
    fields: [provinces.country_iso],
    references: [countries.iso_numeric],
  }),
  cities: many(cities),
  directions: many(directions),
}));

export const countriesRelations = relations(countries, ({ one, many }) => ({
  provinces: many(provinces),
  currency: one(currencies, {
    fields: [countries.currency_id],
    references: [currencies.iso_numeric],
  }),
  directions: many(directions),
}));

export const currenciesRelations = relations(currencies, ({ many }) => ({
  countries: many(countries),
}));

export const categoriesRelations = relations(categories, ({ one, many }) => ({
  user_created_by: one(users, {
    fields: [categories.created_by],
    references: [users.id],
    relationName: 'categories_created_by_users_id',
  }),
  category: one(categories, {
    fields: [categories.parent_id],
    references: [categories.id],
    relationName: 'categories_parent_id_categories_id',
  }),
  categories: many(categories, {
    relationName: 'categories_parent_id_categories_id',
  }),
  user_updated_by: one(users, {
    fields: [categories.updated_by],
    references: [users.id],
    relationName: 'categories_updated_by_users_id',
  }),
  user_user_id: one(users, {
    fields: [categories.user_id],
    references: [users.id],
    relationName: 'categories_user_id_users_id',
  }),
  product_categories: many(product_categories),
  category_translations: many(category_translations),
}));

export const usersRelations = relations(users, ({ one, many }) => ({
  categories_created_by: many(categories, {
    relationName: 'categories_created_by_users_id',
  }),
  categories_updated_by: many(categories, {
    relationName: 'categories_updated_by_users_id',
  }),
  categories_user_id: many(categories, {
    relationName: 'categories_user_id_users_id',
  }),
  variant_products_created_by: many(variant_products, {
    relationName: 'variant_products_created_by_users_id',
  }),
  variant_products_updated_by: many(variant_products, {
    relationName: 'variant_products_updated_by_users_id',
  }),
  discounts: many(discounts),
  configurations: many(configurations),
  deliveries: many(deliveries),
  directions: many(directions),
  products_created_by: many(products, {
    relationName: 'products_created_by_users_id',
  }),
  products_updated_by: many(products, {
    relationName: 'products_updated_by_users_id',
  }),
  products_user_id: many(products, {
    relationName: 'products_user_id_users_id',
  }),
  messages: many(messages),
  notifications: many(notifications),
  user_sessions: many(user_sessions),
  verification_tokens: many(verification_tokens),
  file: one(files, {
    fields: [users.profile_image_file_id],
    references: [files.id],
  }),
  carts_retailer_id: many(carts, {
    relationName: 'carts_retailer_id_users_id',
  }),
  carts_wholesaler_id: many(carts, {
    relationName: 'carts_wholesaler_id_users_id',
  }),
  orders_accepted_by: many(orders, {
    relationName: 'orders_accepted_by_users_id',
  }),
  orders_cancelled_by: many(orders, {
    relationName: 'orders_cancelled_by_users_id',
  }),
  orders_rejected_by: many(orders, {
    relationName: 'orders_rejected_by_users_id',
  }),
  orders_retailer_id: many(orders, {
    relationName: 'orders_retailer_id_users_id',
  }),
  orders_wholesaler_id: many(orders, {
    relationName: 'orders_wholesaler_id_users_id',
  }),
  wholesaler_staffs_created_by: many(wholesaler_staffs, {
    relationName: 'wholesaler_staffs_created_by_users_id',
  }),
  wholesaler_staffs_staff_user_id: many(wholesaler_staffs, {
    relationName: 'wholesaler_staffs_staff_user_id_users_id',
  }),
  wholesaler_staffs_updated_by: many(wholesaler_staffs, {
    relationName: 'wholesaler_staffs_updated_by_users_id',
  }),
  wholesaler_staffs_wholesaler_id: many(wholesaler_staffs, {
    relationName: 'wholesaler_staffs_wholesaler_id_users_id',
  }),
  user_uploads: many(user_uploads),
  chat_participants: many(chat_participants),
  document_sequences: many(document_sequences),
  category_translations: many(category_translations),
  product_translations: many(product_translations),
}));

export const citiesRelations = relations(cities, ({ one, many }) => ({
  province: one(provinces, {
    fields: [cities.province_id],
    references: [provinces.id],
  }),
  directions: many(directions),
}));

export const variant_productsRelations = relations(
  variant_products,
  ({ one, many }) => ({
    user_created_by: one(users, {
      fields: [variant_products.created_by],
      references: [users.id],
      relationName: 'variant_products_created_by_users_id',
    }),
    product: one(products, {
      fields: [variant_products.product_id],
      references: [products.id],
    }),
    user_updated_by: one(users, {
      fields: [variant_products.updated_by],
      references: [users.id],
      relationName: 'variant_products_updated_by_users_id',
    }),
    cart_details: many(cart_details),
    order_details: many(order_details),
  }),
);

export const productsRelations = relations(products, ({ one, many }) => ({
  variant_products: many(variant_products),
  products_files: many(products_files),
  user_created_by: one(users, {
    fields: [products.created_by],
    references: [users.id],
    relationName: 'products_created_by_users_id',
  }),
  user_updated_by: one(users, {
    fields: [products.updated_by],
    references: [users.id],
    relationName: 'products_updated_by_users_id',
  }),
  user_user_id: one(users, {
    fields: [products.user_id],
    references: [users.id],
    relationName: 'products_user_id_users_id',
  }),
  order_details: many(order_details),
  product_categories: many(product_categories),
  product_translations: many(product_translations),
}));

export const discountsRelations = relations(discounts, ({ one }) => ({
  user: one(users, {
    fields: [discounts.user_id],
    references: [users.id],
  }),
}));

export const message_filesRelations = relations(message_files, ({ one }) => ({
  file: one(files, {
    fields: [message_files.file_id],
    references: [files.id],
  }),
  message: one(messages, {
    fields: [message_files.message_id],
    references: [messages.id],
  }),
}));

export const filesRelations = relations(files, ({ many }) => ({
  message_files: many(message_files),
  products_files: many(products_files),
  users: many(users),
  user_uploads: many(user_uploads),
  order_pdf_files: many(order_pdf_files),
}));

export const messagesRelations = relations(messages, ({ one, many }) => ({
  message_files: many(message_files),
  chat_panel: one(chat_panels, {
    fields: [messages.chat_panel_id],
    references: [chat_panels.id],
  }),
  message: one(messages, {
    fields: [messages.reply_to],
    references: [messages.id],
    relationName: 'messages_reply_to_messages_id',
  }),
  messages: many(messages, {
    relationName: 'messages_reply_to_messages_id',
  }),
  user: one(users, {
    fields: [messages.sender_id],
    references: [users.id],
  }),
}));

export const products_filesRelations = relations(products_files, ({ one }) => ({
  file: one(files, {
    fields: [products_files.file_id],
    references: [files.id],
  }),
  product: one(products, {
    fields: [products_files.product_id],
    references: [products.id],
  }),
}));

export const configurationsRelations = relations(configurations, ({ one }) => ({
  user: one(users, {
    fields: [configurations.user_id],
    references: [users.id],
  }),
}));

export const deliveriesRelations = relations(deliveries, ({ one, many }) => ({
  user: one(users, {
    fields: [deliveries.delivery_person],
    references: [users.id],
  }),
  delivery_timelines: many(delivery_timeline),
}));

export const directionsRelations = relations(directions, ({ one }) => ({
  city: one(cities, {
    fields: [directions.city_id, directions.province_id],
    references: [cities.id, cities.province_id],
  }),
  country: one(countries, {
    fields: [directions.country_iso],
    references: [countries.iso_numeric],
  }),
  province: one(provinces, {
    fields: [directions.province_id, directions.country_iso],
    references: [provinces.id, provinces.country_iso],
  }),
  user: one(users, {
    fields: [directions.user_id],
    references: [users.id],
  }),
}));

export const delivery_timelineRelations = relations(
  delivery_timeline,
  ({ one }) => ({
    delivery: one(deliveries, {
      fields: [delivery_timeline.delivery_id],
      references: [deliveries.id],
    }),
  }),
);

export const chat_panelsRelations = relations(chat_panels, ({ many }) => ({
  messages: many(messages),
  chat_participants: many(chat_participants),
}));

export const notificationsRelations = relations(notifications, ({ one }) => ({
  user: one(users, {
    fields: [notifications.user_id],
    references: [users.id],
  }),
}));

export const user_sessionsRelations = relations(user_sessions, ({ one }) => ({
  user: one(users, {
    fields: [user_sessions.user_id],
    references: [users.id],
  }),
}));

export const verification_tokensRelations = relations(
  verification_tokens,
  ({ one }) => ({
    user: one(users, {
      fields: [verification_tokens.user_id],
      references: [users.id],
    }),
  }),
);

export const cartsRelations = relations(carts, ({ one, many }) => ({
  user_retailer_id: one(users, {
    fields: [carts.retailer_id],
    references: [users.id],
    relationName: 'carts_retailer_id_users_id',
  }),
  user_wholesaler_id: one(users, {
    fields: [carts.wholesaler_id],
    references: [users.id],
    relationName: 'carts_wholesaler_id_users_id',
  }),
  cart_details: many(cart_details),
}));

export const cart_detailsRelations = relations(cart_details, ({ one }) => ({
  cart: one(carts, {
    fields: [cart_details.cart_id],
    references: [carts.id],
  }),
  variant_product: one(variant_products, {
    fields: [cart_details.variant_products_id],
    references: [variant_products.id],
  }),
}));

export const order_detailsRelations = relations(order_details, ({ one }) => ({
  order: one(orders, {
    fields: [order_details.order_id],
    references: [orders.id],
  }),
  product: one(products, {
    fields: [order_details.product_id],
    references: [products.id],
  }),
  variant_product: one(variant_products, {
    fields: [order_details.variant_product_id],
    references: [variant_products.id],
  }),
}));

export const ordersRelations = relations(orders, ({ one, many }) => ({
  order_details: many(order_details),
  user_accepted_by: one(users, {
    fields: [orders.accepted_by],
    references: [users.id],
    relationName: 'orders_accepted_by_users_id',
  }),
  user_cancelled_by: one(users, {
    fields: [orders.cancelled_by],
    references: [users.id],
    relationName: 'orders_cancelled_by_users_id',
  }),
  user_rejected_by: one(users, {
    fields: [orders.rejected_by],
    references: [users.id],
    relationName: 'orders_rejected_by_users_id',
  }),
  user_retailer_id: one(users, {
    fields: [orders.retailer_id],
    references: [users.id],
    relationName: 'orders_retailer_id_users_id',
  }),
  user_wholesaler_id: one(users, {
    fields: [orders.wholesaler_id],
    references: [users.id],
    relationName: 'orders_wholesaler_id_users_id',
  }),
  order_pdf_files: many(order_pdf_files),
}));

export const wholesaler_staffsRelations = relations(
  wholesaler_staffs,
  ({ one }) => ({
    user_created_by: one(users, {
      fields: [wholesaler_staffs.created_by],
      references: [users.id],
      relationName: 'wholesaler_staffs_created_by_users_id',
    }),
    user_staff_user_id: one(users, {
      fields: [wholesaler_staffs.staff_user_id],
      references: [users.id],
      relationName: 'wholesaler_staffs_staff_user_id_users_id',
    }),
    user_updated_by: one(users, {
      fields: [wholesaler_staffs.updated_by],
      references: [users.id],
      relationName: 'wholesaler_staffs_updated_by_users_id',
    }),
    user_wholesaler_id: one(users, {
      fields: [wholesaler_staffs.wholesaler_id],
      references: [users.id],
      relationName: 'wholesaler_staffs_wholesaler_id_users_id',
    }),
  }),
);

export const user_uploadsRelations = relations(user_uploads, ({ one }) => ({
  file: one(files, {
    fields: [user_uploads.file_id],
    references: [files.id],
  }),
  user: one(users, {
    fields: [user_uploads.user_id],
    references: [users.id],
  }),
}));

export const chat_participantsRelations = relations(
  chat_participants,
  ({ one }) => ({
    chat_panel: one(chat_panels, {
      fields: [chat_participants.chat_panel_id],
      references: [chat_panels.id],
    }),
    user: one(users, {
      fields: [chat_participants.user_id],
      references: [users.id],
    }),
  }),
);

export const product_categoriesRelations = relations(
  product_categories,
  ({ one }) => ({
    category: one(categories, {
      fields: [product_categories.category_id],
      references: [categories.id],
    }),
    product: one(products, {
      fields: [product_categories.product_id],
      references: [products.id],
    }),
  }),
);

export const document_sequencesRelations = relations(
  document_sequences,
  ({ one }) => ({
    user: one(users, {
      fields: [document_sequences.owner_id],
      references: [users.id],
    }),
  }),
);

export const order_pdf_filesRelations = relations(
  order_pdf_files,
  ({ one }) => ({
    file: one(files, {
      fields: [order_pdf_files.file_id],
      references: [files.id],
    }),
    order: one(orders, {
      fields: [order_pdf_files.order_id],
      references: [orders.id],
    }),
  }),
);

export const category_translationsRelations = relations(
  category_translations,
  ({ one }) => ({
    user: one(users, {
      fields: [category_translations.updated_by],
      references: [users.id],
    }),
    category: one(categories, {
      fields: [category_translations.category_id],
      references: [categories.id],
    }),
  }),
);

export const product_translationsRelations = relations(
  product_translations,
  ({ one }) => ({
    product: one(products, {
      fields: [product_translations.product_id],
      references: [products.id],
    }),
    user: one(users, {
      fields: [product_translations.updated_by],
      references: [users.id],
    }),
  }),
);
