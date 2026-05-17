import type {
  IOrderDetailItem,
  IRetailerSnapshot,
  IShippingAddressSnapshot,
  IWholesalerSnapshot,
} from '#/orders/order.types.js';
import type pdfMake from 'pdfmake';
import type { Readable } from 'node:stream';

export type PdfMakeWithPolicies = typeof pdfMake & {
  setLocalAccessPolicy?: (callback: (filePath: string) => boolean) => void;
  setUrlAccessPolicy?: (callback: (url: string) => boolean) => void;
};

export type UserConfiguration = {
  language: string;
  timezone: string;
};

export type OrderPdfOrder = {
  id: bigint;
  order_number: string;
  order_series: string;
  order_year: number;
  order_sequence: number;
  retailer_id: string | null;
  wholesaler_id: string | null;
  currency: string;
  subtotal: string;
  discount_total: string;
  iva_total: string;
  total: string;
  item_count: number;
  created_at: string;
  rejected_reason: string | null;
  cancelled_reason: string | null;
  retailer_snapshot: IRetailerSnapshot;
  wholesaler_snapshot: IWholesalerSnapshot;
  shipping_address_snapshot: IShippingAddressSnapshot;
};

export type OrderPdfAssets = {
  wholesalerLogoDataUrl?: string | null;
};

export type GeneratedOrderPdfFile = {
  filename: string;
  content: Buffer | Readable;
  order: OrderPdfOrder;
  language: string;
};

export interface IOrderPdfData {
  id: string;

  order_number: string;
  order_series: string;
  order_year: number;
  order_sequence: number;

  currency: string;

  subtotal: string;
  discount_total: string;
  iva_total: string;
  total: string;
  item_count: number;

  created_at: string;

  retailer_snapshot: IRetailerSnapshot;
  wholesaler_snapshot: IWholesalerSnapshot;
  shipping_address_snapshot: IShippingAddressSnapshot;

  details: IOrderDetailItem[];

  wholesaler_logo_data_url?: string | null;
  pdf_font?: string;

  /**
   * 来自 configurations.language
   */
  language: string;

  /**
   * 来自 configurations.timezone
   */
  timezone: string;
}

export interface IOrderPdfLabels {
  documentTitle: string;
  order: string;
  page: string;
  series: string;
  year: string;
  sequence: string;
  date: string;
  seller: string;
  buyer: string;
  companyType: string;
  taxId: string;
  contact: string;
  email: string;
  phone: string;
  shippingAddress: string;
  country: string;
  documentSummary: string;
  number: string;
  currency: string;
  lineCount: string;
  itemsTitle: string;
  product: string;
  code: string;
  saleUnit: string;
  quantity: string;
  price: string;
  ivaPercent: string;
  subtotal: string;
  iva: string;
  total: string;
  variant: string;
  taxableBase: string;
  discount: string;
  grandTotal: string;
}
