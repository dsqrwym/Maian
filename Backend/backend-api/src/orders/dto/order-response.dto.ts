import type { OrderStatus } from '#/generated/drizzle/enums.js';
import type {
  IOrderDetailItem,
  IRetailerSnapshot,
  IShippingAddressSnapshot,
  IWholesalerSnapshot,
} from '#/orders/order.types.js';

export interface IOrderResponse {
  item_count: number;
  total_subtotal: string;
  total_iva: string;
  total_amount: string;
  status: OrderStatus;
  created_at: string;
  accepted_at: string | null;
  rejected_at: string | null;
  rejected_reason: string | null;
  cancelled_at: string | null;
  cancelled_reason: string | null;
  estimated_delivery_date: string | null;
  wholesaler_snapshot?: IWholesalerSnapshot;
  retailer_snapshot?: IRetailerSnapshot;
  shipping_address_snapshot?: IShippingAddressSnapshot;
  id: bigint;
  order_number: string;
}

export interface IOrderDetailResponse extends IOrderResponse {
  currency: string;
  items: IOrderDetailItem[];
  id: bigint;
  order_number: string;
  wholesaler_id: string | null;
  retailer_id: string | null;
}
