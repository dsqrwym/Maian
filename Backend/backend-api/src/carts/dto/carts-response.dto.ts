import type {
  CartGroupStatus,
  CartItemStatus,
} from '#/carts/cart.constants.js';
import type { Decimal } from 'decimal.js';

export interface ICartResponse {
  groups: ICartGroupWithoutDecimal[];
  summary: ICartSummary;
}

export interface ICartGroup {
  wholesaler: ICartWholesaler;
  item_count: number;
  total_quantity: number;
  subtotal: Decimal;
  iva_total: Decimal;
  total: Decimal;
  status: CartGroupStatus;
  items: ICartItem[];
}

export interface ICartGroupWithoutDecimal extends Omit<
  ICartGroup,
  'subtotal' | 'total' | 'iva_total'
> {
  subtotal: string;
  iva_total: string;
  total: string;
}

export interface ICartWholesaler {
  id: string;
  company_name: string;
  display_name?: string | null;
  profile_image_file_id?: bigint | null;
  minimum_order_amount?: string | null;
}

export interface ICartItem {
  cart_detail_id: bigint;
  product_id: bigint;
  variant_id: bigint;

  product_name: string;
  product_title?: string | null;

  product_code: string;
  variant_code: string;

  main_image?: { id: string; mime_type: string } | null;

  quantity: number;
  sale_unit_qty: number;
  min_order_qty: number;
  max_order_quantity: number;

  price: string;
  price_iva: string;
  iva: string;

  line_subtotal: string;
  line_iva: string;
  line_total: string;

  status: CartItemStatus;
}

export interface ICartSummary {
  wholesaler_count: number;
  item_count: number;
  total_quantity: number;
  subtotal: string;
  iva_total: string;
  total: string;
}
