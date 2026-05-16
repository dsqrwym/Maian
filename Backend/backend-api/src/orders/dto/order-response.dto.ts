import type { OrderStatus } from '#/generated/drizzle/enums.js';
import type {
  IRetailerSnapshot,
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
  cancelled_at: string | null;
  estimated_delivery_date: string | null;
  wholesaler_snapshot?: IWholesalerSnapshot;
  retailer_snapshot?: IRetailerSnapshot;
  id: bigint;
  order_number: string;
}
