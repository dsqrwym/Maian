import { ForbiddenException, Injectable } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { AppAbility } from '#/casl/casl-types.js';
import { IOrderQuery } from '#/orders/dto/order-query.dto.js';
import { Action } from '#/casl/actions.js';
import { orders } from '#/generated/drizzle/schema.js';
import { and, count, eq, gte, ilike, lte, or, sql, SQL } from 'drizzle-orm';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import {
  ORDER_RETAILER_SNAPSHOT_COLUMNS,
  ORDER_SHOPPING_ADDRESS_SNAPSHOT_COLUMNS,
  ORDER_WHOLESALER_SNAPSHOT_COLUMNS,
} from '#/orders/order.constants.js';
import { SQL_IMMUTABLE_UNACCENT } from '#/drizzle/drizzle.constants.js';
import {
  IRetailerSnapshot,
  IWholesalerSnapshot,
} from '#/orders/order.types.js';
import { OrderSortByEnums } from '#/orders/order.enums.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { IOrderResponse } from '#/orders/dto/order-response.dto.js';

@Injectable()
export class ReadOrderService {
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly config: ConfigService,
  ) {
    this.MAX_SEARCH_TERMS = Number(this.config.get(ENV.MAX_SEARCH_TERMS, 10));
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
        cancelled_at: orders.cancelled_at,
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
      whereCondition.push(lte(orders.created_at, endDate));
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
        cancelled_at: item.cancelled_at,
        estimated_delivery_date: item.estimated_delivery_date,
        wholesaler_snapshot: item.wholesaler_snapshot as IWholesalerSnapshot,
        retailer_snapshot: item.retailer_snapshot as IRetailerSnapshot,
        id: item.id,
        order_number: item.order_number,
      })),
      pagination: { total: total[0]?.total ?? 0, page, limit },
    };
  }
}
