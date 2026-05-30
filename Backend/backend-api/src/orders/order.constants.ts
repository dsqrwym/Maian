import { alias } from 'drizzle-orm/pg-core';
import { orders, users } from '#/generated/drizzle/schema.js';
import {
  buildRetailerProfileExpr,
  buildWholesalerProfileExpr,
} from '#/utils/db/user.db.utils.js';
import { sql } from 'drizzle-orm';

export const ORDER_ERRORS = {
  WHOLESALER_NOT_FOUND_OR_INVALID: 'WHOLESALER_NOT_FOUND_OR_INVALID',
  RETAILER_NOT_FOUND_OR_INVALID: 'RETAILER_NOT_FOUND_OR_INVALID',
  SHIPPING_ADDRESS_NOT_FOUND: 'SHIPPING_ADDRESS_NOT_FOUND',
  CART_EMPTY: 'CART_EMPTY',
  PRODUCT_NOT_AVAILABLE: 'PRODUCT_NOT_AVAILABLE',
  VARIANT_NOT_AVAILABLE: 'VARIANT_NOT_AVAILABLE',
  QUANTITY_BELOW_MIN_ORDER: 'QUANTITY_BELOW_MIN_ORDER',
  NOT_ENOUGH_STOCK: 'NOT_ENOUGH_STOCK',
  ORDER_LINE_LIMIT_EXCEEDED: 'ORDER_LINE_LIMIT_EXCEEDED',
  ORDER_SEQUENCE_FAILED: 'ORDER_SEQUENCE_FAILED',
  ORDER_CREATE_FAILED: 'ORDER_CREATE_FAILED',

  ORDER_NOT_FOUND: 'ORDER_NOT_FOUND',
  ORDER_STATUS_INVALID: 'ORDER_STATUS_INVALID',
  RESERVED_STOCK_INCONSISTENT: 'RESERVED_STOCK_INCONSISTENT',
} as const;

/**
 * Order 文档类型，项目目前只会用 PED.
 */
export const ORDER_DOCUMENT_TYPE = 'PED';
/**
 * Order 货币，项目目前只会用 EURO.
 */
export const ORDER_CURRENCY = 'EUR';

export const RETAILER_TABLE = alias(users, 'retailer');
export const RETAILER_PROFILE = buildRetailerProfileExpr(
  RETAILER_TABLE.profile,
);
export const WHOLESALER_TABLE = alias(users, 'wholesaler');
export const WHOLESALER_PROFILE = buildWholesalerProfileExpr(
  WHOLESALER_TABLE.profile,
);
export const ORDER_RETAILER_SNAPSHOT = orders.retailer_snapshot;
export const ORDER_WHOLESALER_SNAPSHOT = orders.wholesaler_snapshot;
export const SHOPPING_ADDRESS_SNAPSHOT = orders.shipping_address_snapshot;
export const ORDER_RETAILER_SNAPSHOT_INFO = {
  retailerEmail: sql<string>`${ORDER_RETAILER_SNAPSHOT}->>'email'`,
  retailerUserId: sql<string>`${ORDER_RETAILER_SNAPSHOT}->>'user_id'`,
  companyNameExpr: sql<
    string | null | undefined
  >`${ORDER_RETAILER_SNAPSHOT}->>'company_name'`,
  displayNameExpr: sql<
    string | null | undefined
  >`${ORDER_RETAILER_SNAPSHOT}->>'display_name'`,
  contactNameExpr: sql<
    string | null | undefined
  >`${ORDER_RETAILER_SNAPSHOT}->>'contact_name'`,
  taxIdExpr: sql<
    string | null | undefined
  >`${ORDER_RETAILER_SNAPSHOT}->>'tax_id'`,
};
export const ORDER_RETAILER_SNAPSHOT_COLUMNS = Object.values(
  ORDER_RETAILER_SNAPSHOT_INFO,
);
export const ORDER_WHOLESALER_SNAPSHOT_INFO = {
  wholesalerEmail: sql<string>`${ORDER_WHOLESALER_SNAPSHOT}->>'email'`,
  wholesalerUserId: sql<string>`${ORDER_WHOLESALER_SNAPSHOT}->>'user_id'`,
  companyNameExpr: sql<string>`${ORDER_WHOLESALER_SNAPSHOT}->>'company_name'`,
  displayNameExpr: sql<
    string | null | undefined
  >`${ORDER_WHOLESALER_SNAPSHOT}->>'display_name'`,
  taxIdExpr: sql<
    string | null | undefined
  >`${ORDER_WHOLESALER_SNAPSHOT}->>'tax_id'`,
};
export const ORDER_WHOLESALER_SNAPSHOT_COLUMNS = Object.values(
  ORDER_WHOLESALER_SNAPSHOT_INFO,
);
export const ORDER_SHOPPING_ADDRESS_SNAPSHOT_INFO = {
  cityNameExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'city_name'`,
  cityNameLocalExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'city_name_local'`,
  provinceName: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'province_name'`,
  provinceNameLocalExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'province_name_local'`,
  countryNameExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'country_name'`,
  countryNameLocalExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'country_name_local'`,
  zipCodeExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'zip_code'`,
  streetExpr: sql<string>`${SHOPPING_ADDRESS_SNAPSHOT}->>'street'`,
};
export const ORDER_SHOPPING_ADDRESS_SNAPSHOT_COLUMNS = Object.values(
  ORDER_SHOPPING_ADDRESS_SNAPSHOT_INFO,
);
