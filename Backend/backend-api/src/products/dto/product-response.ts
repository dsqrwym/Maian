export interface IProductResponse {
  product_translations: {
    lang_code: string;
    name: string | null;
    title: string | null;
  }[];
  user_id?: string;
  status?: string;
  iva?: string;
  main_category?: {
    id: string;
    name: string;
    category_translations: {
      lang_code: string;
      name: string;
    }[];
  };
  id: bigint;
  name: string;
  title: string | null;
  product_code: string;
  min_price_iva: string;
  min_price: string;
  total_stock: number;
  min_order_qty: number;
  main_image: {
    id: string;
    mime_type: string;
  } | null;
}
