import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { AppAbility } from '#/casl/casl-types.js';
import {
  IOrderDetailQuery,
  IOrderQuery,
} from '#/orders/dto/order-query.dto.js';
import { Action } from '#/casl/actions.js';
import { order_details, orders } from '#/generated/drizzle/schema.js';
import { and, count, eq, gte, ilike, lt, lte, or, sql, SQL } from 'drizzle-orm';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import {
  ORDER_ERRORS,
  ORDER_RETAILER_SNAPSHOT_COLUMNS,
  ORDER_SHOPPING_ADDRESS_SNAPSHOT_COLUMNS,
  ORDER_WHOLESALER_SNAPSHOT_COLUMNS,
} from '#/orders/order.constants.js';
import {
  jsonbAggBuildObject,
  SQL_IMMUTABLE_UNACCENT,
} from '#/drizzle/drizzle.constants.js';
import {
  IOrderDetailItem,
  IRetailerSnapshot,
  IShippingAddressSnapshot,
  IWholesalerSnapshot,
} from '#/orders/order.types.js';
import { OrderSortByEnums } from '#/orders/order.enums.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import {
  IOrderDetailResponse,
  IOrderResponse,
} from '#/orders/dto/order-response.dto.js';
import { subject } from '@casl/ability';

@Injectable()
export class ReadOrderService {
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly config: ConfigService,
  ) {
    this.MAX_SEARCH_TERMS = Number(this.config.get(ENV.MAX_SEARCH_TERMS, 10));
  }

  async getOrderDetail(
    orderId: string,
    query: IOrderDetailQuery,
    ability: AppAbility,
    retailerId?: string,
    wholesalerId?: string,
  ) {
    if (!ability.can(Action.Read, 'orders')) {
      throw new ForbiddenException('You are not allowed to read orders');
    }
    if (!retailerId && !wholesalerId) {
      // 如果都没有说明用户的登录状态有问题
      throw new ForbiddenException('You are not allowed to read orders');
    }
    const id = BigInt(orderId);

    const { langCode } = query;

    const [orderDetail] = await this.drizzle.db
      .select({
        id: orders.id,
        order_number: orders.order_number,

        wholesaler_id: orders.wholesaler_id,
        retailer_id: orders.retailer_id,

        ...(wholesalerId && {
          retailer_snapshot: orders.retailer_snapshot,
        }),
        ...(retailerId && {
          wholesaler_snapshot: orders.wholesaler_snapshot,
        }),

        shipping_address_snapshot: orders.shipping_address_snapshot,

        item_count: orders.item_count,
        total_subtotal: orders.subtotal,
        total_iva: orders.iva_total,
        total_amount: orders.total,
        currency: orders.currency,

        status: orders.status,
        created_at: orders.created_at,
        accepted_at: orders.accepted_at,
        rejected_at: orders.rejected_at,
        rejected_reason: orders.rejected_reason,
        cancelled_at: orders.cancelled_at,
        cancelled_reason: orders.cancelled_reason,
        estimated_delivery_date: orders.estimated_delivery_date,

        items: jsonbAggBuildObject<IOrderDetailItem>(
          {
            id: order_details.id,
            product_id: order_details.product_id,
            variant_product_id: order_details.variant_product_id,

            product_name: order_details.product_name,
            product_title: order_details.product_title,
            product_code: order_details.product_code,
            variant_product_code: order_details.variant_product_code,

            product_translations_snapshot: sql`(
            select t 
            from jsonb_array_elements(
            ${order_details.product_translations_snapshot}
            ) as t 
            where t->>'lang_code' = ${langCode}
            limit 1
            )
            `,
            variant_attributes_snapshot:
              order_details.variant_attributes_snapshot,

            type_sale: order_details.type_sale,
            sale_unit_qty: order_details.sale_unit_qty,
            quantity: order_details.quantity,

            unit_price: order_details.unit_price,
            unit_price_iva: order_details.unit_price_iva,
            iva: order_details.iva,
            subtotal: order_details.subtotal,
            iva_total: order_details.iva_total,
            total: order_details.total,
          },
          {
            orderBy: sql`${order_details.id}`,
            filter: sql`${order_details.id} is not null`,
          },
        ).as('items'),
      })
      .from(orders)
      .leftJoin(order_details, eq(order_details.order_id, orders.id))
      .where(eq(orders.id, id))
      .groupBy(orders.id);

    if (
      !ability.can(
        Action.Read,
        subject('orders', {
          wholesaler_id: orderDetail.wholesaler_id,
          retailer_id: orderDetail.retailer_id,
        }),
      )
    ) {
      throw new ForbiddenException('You are not allowed to read orders');
    }

    if (!orderDetail) {
      throw new NotFoundException(ORDER_ERRORS.ORDER_NOT_FOUND);
    }

    return orderDetail as IOrderDetailResponse;
  }

  getSortField(sortBy?: OrderSortByEnums) {
    switch (sortBy) {
      case OrderSortByEnums.ORDER_DATE:
        return orders.created_at;
      case OrderSortByEnums.ORDER_NUMBER:
        return orders.order_number;
      case OrderSortByEnums.ORDER_DELIVERY_DATE:
        return orders.estimated_delivery_date;
      case OrderSortByEnums.TOTAL_SUBTOTAL:
        return orders.subtotal;
      case OrderSortByEnums.TOTAL_IVA:
        return orders.iva_total;
      case OrderSortByEnums.TOTAL_PRICE:
        return orders.total;
      case OrderSortByEnums.TOTAL_ITEM:
        return orders.item_count;
      default:
        return orders.created_at;
    }
  }

  async getMyOrders(
    query: IOrderQuery,
    ability: AppAbility,
    retailerId?: string,
    wholesalerId?: string,
  ): Promise<PaginatedDataWithT<IOrderResponse>> {
    if (!ability.can(Action.Read, 'orders')) {
      throw new ForbiddenException('You are not allowed to read orders');
    }
    if (!retailerId && !wholesalerId) {
      // 如果都没有说明用户的登录状态有问题
      throw new ForbiddenException('You are not allowed to read orders');
    }
    const { search, status, startDate, endDate } = query;
    const {
      minTotalPrice,
      maxTotalPrice,
      minTotalIva,
      maxTotalIva,
      minItemCount,
      maxItemCount,
      minSubtotal,
      maxSubtotal,
    } = query;
    const { page, limit, sortBy, orderBy } = query;
    const offset = (page - 1) * limit;
    const sortField = this.getSortField(sortBy);
    const orderQuery = this.drizzle.db
      .select({
        id: orders.id,
        order_number: orders.order_number,
        ...(wholesalerId && {
          retailer_snapshot: orders.retailer_snapshot,
          shipping_address_snapshot: orders.shipping_address_snapshot,
        }),
        ...(retailerId && {
          wholesaler_snapshot: orders.wholesaler_snapshot,
        }),
        item_count: orders.item_count,
        total_subtotal: orders.subtotal,
        total_iva: orders.iva_total,
        total_amount: orders.total,

        status: orders.status,
        created_at: orders.created_at,
        accepted_at: orders.accepted_at,
        rejected_at: orders.rejected_at,
        rejected_reason: orders.rejected_reason,
        cancelled_at: orders.cancelled_at,
        cancelled_reason: orders.cancelled_reason,
        estimated_delivery_date: orders.estimated_delivery_date,
      })
      .from(orders)
      .$dynamic();

    const whereCondition: (SQL | undefined)[] = [];

    if (search) {
      const searchTerms = escapeLike(toUnaccent(search))
        .split(/\s+/)
        .filter((s) => s.length > 0)
        .slice(0, this.MAX_SEARCH_TERMS);

      for (const term of searchTerms) {
        const likeSearch = `%${term}%`;
        const shoppingSearchCondition: (SQL | undefined)[] = [];
        const retailerSearchCondition: (SQL | undefined)[] = [];
        const wholesalerSearchCondition: (SQL | undefined)[] = [];
        for (const column of ORDER_SHOPPING_ADDRESS_SNAPSHOT_COLUMNS) {
          shoppingSearchCondition.push(
            ilike(SQL_IMMUTABLE_UNACCENT(column), likeSearch),
          );
        }
        for (const column of ORDER_WHOLESALER_SNAPSHOT_COLUMNS) {
          wholesalerSearchCondition.push(
            ilike(SQL_IMMUTABLE_UNACCENT(column), likeSearch),
          );
        }
        for (const column of ORDER_RETAILER_SNAPSHOT_COLUMNS) {
          retailerSearchCondition.push(
            ilike(SQL_IMMUTABLE_UNACCENT(column), likeSearch),
          );
        }
        whereCondition.push(
          or(
            eq(orders.order_number, term),
            ilike(orders.order_number, likeSearch),
            ...shoppingSearchCondition,
            ...retailerSearchCondition,
            ...wholesalerSearchCondition,
          ),
        );
      }
    }

    if (status) {
      whereCondition.push(eq(orders.status, status));
    }
    if (startDate) {
      whereCondition.push(gte(orders.created_at, startDate));
    }
    if (endDate) {
      const endExclusive = new Date(endDate);
      endExclusive.setUTCDate(endExclusive.getUTCDate() + 1);
      whereCondition.push(lt(orders.created_at, endExclusive.toISOString()));
    }
    if (retailerId) {
      whereCondition.push(eq(orders.retailer_id, retailerId));
    }
    if (wholesalerId) {
      whereCondition.push(eq(orders.wholesaler_id, wholesalerId));
    }
    if (minItemCount !== undefined) {
      whereCondition.push(gte(orders.item_count, minItemCount));
    }
    if (maxItemCount !== undefined) {
      whereCondition.push(lte(orders.item_count, maxItemCount));
    }
    if (minSubtotal !== undefined) {
      whereCondition.push(gte(orders.subtotal, minSubtotal.toString()));
    }
    if (maxSubtotal !== undefined) {
      whereCondition.push(lte(orders.subtotal, maxSubtotal.toString()));
    }
    if (minTotalPrice !== undefined) {
      whereCondition.push(gte(orders.total, minTotalPrice.toString()));
    }
    if (maxTotalPrice !== undefined) {
      whereCondition.push(lte(orders.total, maxTotalPrice.toString()));
    }
    if (minTotalIva !== undefined) {
      whereCondition.push(gte(orders.iva_total, minTotalIva.toString()));
    }
    if (maxTotalIva !== undefined) {
      whereCondition.push(lte(orders.iva_total, maxTotalIva.toString()));
    }

    const whereAndClause = and(...whereCondition);
    const [items, total] = await Promise.all([
      orderQuery
        .where(whereAndClause)
        .offset(offset)
        .limit(limit)
        .orderBy(sql`${sortField} ${sql.raw(orderBy ?? 'desc')}`),
      this.drizzle.db
        .select({
          total: count(),
        })
        .from(orders)
        .where(whereAndClause),
    ]);

    return {
      items: items.map((item) => ({
        item_count: item.item_count,
        total_subtotal: item.total_subtotal,
        total_iva: item.total_iva,
        total_amount: item.total_amount,
        status: item.status,
        created_at: item.created_at,
        accepted_at: item.accepted_at,
        rejected_at: item.rejected_at,
        rejected_reason: item.rejected_reason,
        cancelled_at: item.cancelled_at,
        cancelled_reason: item.cancelled_reason,
        estimated_delivery_date: item.estimated_delivery_date,
        wholesaler_snapshot: item.wholesaler_snapshot as
          | IWholesalerSnapshot
          | undefined,
        retailer_snapshot: item.retailer_snapshot as
          | IRetailerSnapshot
          | undefined,
        shipping_address_snapshot: item.shipping_address_snapshot as
          | IShippingAddressSnapshot
          | undefined,
        id: item.id,
        order_number: item.order_number,
      })),
      pagination: { total: total[0]?.total ?? 0, page, limit },
    };
  }
}
