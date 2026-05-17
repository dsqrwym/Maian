import { Injectable, NotFoundException } from '@nestjs/common';
import { eq } from 'drizzle-orm';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  configurations,
  order_details,
  orders,
} from '#/generated/drizzle/schema.js';
import { ORDER_ERRORS } from '#/orders/order.constants.js';
import type { IOrderDetailItem } from '#/orders/order.types.js';
import type {
  IOrderPdfData,
  OrderPdfAssets,
  OrderPdfOrder,
  UserConfiguration,
} from '#/pdf/pdf.type.js';
import { getOrderPdfFontFamily } from '#/pdf/pdf-font.constants.js';
import { localizeOrderDetailItems } from '#/utils/order-pdf.utils.js';

@Injectable()
export class OrderPdfDataService {
  constructor(private readonly drizzle: DrizzleService) {}

  async getOrderForPdf(orderId: bigint): Promise<OrderPdfOrder> {
    const [order] = await this.drizzle.db
      .select({
        id: orders.id,
        order_number: orders.order_number,
        order_series: orders.order_series,
        order_year: orders.order_year,
        order_sequence: orders.order_sequence,
        retailer_id: orders.retailer_id,
        wholesaler_id: orders.wholesaler_id,
        currency: orders.currency,
        subtotal: orders.subtotal,
        discount_total: orders.discount_total,
        iva_total: orders.iva_total,
        total: orders.total,
        item_count: orders.item_count,
        created_at: orders.created_at,
        rejected_reason: orders.rejected_reason,
        cancelled_reason: orders.cancelled_reason,
        retailer_snapshot: orders.retailer_snapshot,
        wholesaler_snapshot: orders.wholesaler_snapshot,
        shipping_address_snapshot: orders.shipping_address_snapshot,
      })
      .from(orders)
      .where(eq(orders.id, orderId))
      .limit(1);

    if (!order) {
      throw new NotFoundException(ORDER_ERRORS.ORDER_NOT_FOUND);
    }

    return order as Omit<
      typeof order,
      'retailer_snapshot' | 'wholesaler_snapshot' | 'shipping_address_snapshot'
    > &
      Pick<
        OrderPdfOrder,
        | 'retailer_snapshot'
        | 'wholesaler_snapshot'
        | 'shipping_address_snapshot'
      >;
  }

  async getUserConfiguration(
    userId?: string | null,
  ): Promise<UserConfiguration> {
    if (!userId) return { language: 'en', timezone: 'UTC' };

    const [configuration] = await this.drizzle.db
      .select({
        language: configurations.language,
        timezone: configurations.timezone,
      })
      .from(configurations)
      .where(eq(configurations.user_id, userId))
      .limit(1);

    return {
      language: configuration?.language ?? 'en',
      timezone: configuration?.timezone ?? 'UTC',
    };
  }

  async buildOrderPdfData(
    order: OrderPdfOrder,
    config: UserConfiguration,
    assets: OrderPdfAssets = {},
  ): Promise<IOrderPdfData> {
    const details = await this.getOrderDetails(order.id, config.language);

    return {
      id: order.id.toString(),
      order_number: order.order_number,
      order_series: order.order_series,
      order_year: order.order_year,
      order_sequence: order.order_sequence,
      currency: order.currency,
      subtotal: order.subtotal,
      discount_total: order.discount_total,
      iva_total: order.iva_total,
      total: order.total,
      item_count: order.item_count,
      created_at: order.created_at,
      retailer_snapshot: order.retailer_snapshot,
      wholesaler_snapshot: order.wholesaler_snapshot,
      shipping_address_snapshot: order.shipping_address_snapshot,
      details,
      wholesaler_logo_data_url: assets.wholesalerLogoDataUrl ?? null,
      pdf_font: getOrderPdfFontFamily(config.language),
      language: config.language,
      timezone: config.timezone,
    };
  }

  private async getOrderDetails(
    orderId: bigint,
    language: string,
  ): Promise<IOrderDetailItem[]> {
    const rows = await this.drizzle.db
      .select({
        id: order_details.id,
        product_id: order_details.product_id,
        variant_product_id: order_details.variant_product_id,
        product_name: order_details.product_name,
        product_title: order_details.product_title,
        product_code: order_details.product_code,
        variant_product_code: order_details.variant_product_code,
        product_translations_snapshot:
          order_details.product_translations_snapshot,
        variant_attributes_snapshot: order_details.variant_attributes_snapshot,
        type_sale: order_details.type_sale,
        sale_unit_qty: order_details.sale_unit_qty,
        quantity: order_details.quantity,
        unit_price: order_details.unit_price,
        unit_price_iva: order_details.unit_price_iva,
        iva: order_details.iva,
        subtotal: order_details.subtotal,
        iva_total: order_details.iva_total,
        total: order_details.total,
      })
      .from(order_details)
      .where(eq(order_details.order_id, orderId))
      .orderBy(order_details.id);

    return localizeOrderDetailItems(
      rows.map((item) => ({
        ...item,
        id: item.id.toString(),
        product_id: item.product_id?.toString() ?? null,
        variant_product_id: item.variant_product_id?.toString() ?? null,
        product_translations_snapshot:
          item.product_translations_snapshot as IOrderDetailItem['product_translations_snapshot'],
      })),
      language,
    );
  }
}
