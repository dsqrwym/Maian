import type {
  AddressType as DrizzleAddressType,
  DeliveryStatus,
  ProductStatus as DrizzleProductStatus,
  SaleVariant,
  UserRole as DrizzleUserRole,
  UserStatus as DrizzleUserStatus,
  OrderStatus as DrizzleOrderStatus,
} from './schema.js';

export type UserRole = (typeof DrizzleUserRole.enumValues)[number];
export const UserRole = {
  WHOLESALER: 'WHOLESALER',
  RETAILER: 'RETAILER',
  SUPPORT: 'SUPPORT',
  DELIVERY: 'DELIVERY',
  WAREHOUSE: 'WAREHOUSE',
  ADMIN: 'ADMIN',
  SUPERADMIN: 'SUPERADMIN',
} as const;

export type UserStatus = (typeof DrizzleUserStatus.enumValues)[number];
export const UserStatus = {
  PENDING_VERIFICATION: 'PENDING_VERIFICATION',
  INACTIVE: 'INACTIVE',
  ACTIVE: 'ACTIVE',
  PENDING_REVIEW: 'PENDING_REVIEW',
  APPROVED: 'APPROVED',
  BANNED: 'BANNED',
} as const;

export type DeliveryStatus = (typeof DeliveryStatus.enumValues)[number];
export type AddressType = (typeof DrizzleAddressType.enumValues)[number];
export const AddressType = {
  DELIVERY: 'DELIVERY',
  INVOICE: 'INVOICE',
  STORE: 'STORE',
} as const;

export type ProductStatus = (typeof DrizzleProductStatus.enumValues)[number];
export const ProductStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;
export type SaleVariant = (typeof SaleVariant.enumValues)[number];

export type OrderStatus = (typeof DrizzleOrderStatus.enumValues)[number];
export const OrderStatus = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  CANCELLED: 'CANCELLED',
} as const;
