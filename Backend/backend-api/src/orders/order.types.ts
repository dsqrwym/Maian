import type { Decimal } from 'decimal.js';
import type { SaleVariant } from '#/generated/drizzle/enums.js';
import type { IProductTranslationDto } from '#/products/dto/product-translation.dto.js';

/**
 * 零售商快照
 */
export interface IRetailerSnapshot {
  id: string;
  user_id: string | null;
  display_name?: string | null;
  company_name?: string | null;
  company_type?: string | null;
  contact_name?: string | null;
  tax_id?: string | null;
  email: string;
  telephone?: string | null;
}

/**
 * 批发商快照
 */
export interface IWholesalerSnapshot {
  id: string;
  user_id?: string | null;
  display_name?: string | null;
  company_name: string;
  company_type: string;
  tax_id?: string | null;
  email: string;
  telephone?: string | null;
}

/**
 * 送货地址快照
 */
export interface IShippingAddressSnapshot {
  id: string;

  street: string;
  zip_code: string;

  city_id: number;
  city_name: string;
  city_name_local: string;

  province_id: number;
  province_name: string;
  province_name_local: string;

  country_iso: number;
  country_alpha2: string;
  country_alpha3: string;
  country_name: string;
  country_name_local: string;

  latitude?: number | null;
  longitude?: number | null;
}

/**
 * 订单行
 */
export interface IOrderLine {
  cartId: bigint;
  cartDetailsId: bigint;

  productId: bigint;
  variantProductId: bigint;

  productName: string;
  productTitle: string | null;
  productCode: string;
  variantProductCode: string;

  productTranslationsSnapshot: IProductTranslationDto[] | null;

  variantAttributesSnapshot?: unknown;

  typeSale: SaleVariant;
  saleUnitQty: number;

  /**
   * 购买的销售单位数量。
   * 例如：2 箱。
   */
  quantity: number;

  /**
   * 每个销售单位的未税价格。
   */
  unitPrice: string;

  /**
   * 每个销售单位的含税价格。
   */
  unitPriceIva: string;

  iva: string;

  subtotal: Decimal;
  ivaTotal: Decimal;
  total: Decimal;
}

export interface IOrderDetailItem {
  id: string;
  product_id: string | null;
  variant_product_id: string | null;

  product_name: string;
  product_title: string | null;
  product_code: string;
  variant_product_code: string;

  product_translations_snapshot:
    | {
        lang_code: string;
        name: string | null;
        title: string | null;
      }[]
    | null;

  variant_attributes_snapshot: unknown;

  type_sale: SaleVariant;
  sale_unit_qty: number;
  quantity: number;

  unit_price: string;
  unit_price_iva: string;
  iva: string;
  subtotal: string;
  iva_total: string;
  total: string;
}
