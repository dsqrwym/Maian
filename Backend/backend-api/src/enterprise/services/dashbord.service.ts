import { Injectable } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { IDashboardQuery } from '#/enterprise/dto/dashbord-query.dto.js';
import { and, asc, desc, eq, gte, lt, sql, type SQL } from 'drizzle-orm';
import { order_details, orders } from '#/generated/drizzle/schema.js';
import { IDashboardResponse } from '#/enterprise/dto/dashbord-response.dto.js';
import { OrderStatus } from '#/generated/drizzle/enums.js';

@Injectable()
export class DashboardService {
  constructor(private readonly drizzle: DrizzleService) {}

  async getDashboard(
    wholesalerId: string,
    query: IDashboardQuery,
  ): Promise<IDashboardResponse> {
    const topLimit = query.topLimit;

    const [summary, revenueTrend, topSellingProducts] = await Promise.all([
      this.getSummaryAndStatus(wholesalerId, query),
      this.getRevenueTrend(wholesalerId, query),
      this.getTopSellingProducts(wholesalerId, query, topLimit),
    ]);

    return {
      summary: {
        totalOrders: summary.totalOrders,
        pendingOrders: summary.pendingOrders,
        acceptedOrders: summary.acceptedOrders,
        totalRevenue: summary.totalRevenue,
        averageOrderValue: summary.averageOrderValue,
      },

      orderStatus: {
        pending: summary.pendingOrders,
        accepted: summary.acceptedOrders,
        rejected: summary.rejectedOrders,
        cancelled: summary.cancelledOrders,
        total: summary.totalOrders,
      },

      revenueTrend,

      topSellingProducts,
    };
  }

  /**
   * 构建批发商仪表板查询条件
   * 起始结束日期， 批发商ID
   * @param wholesalerId
   * @param query
   * @private
   */
  private buildDashboardWhere(wholesalerId: string, query: IDashboardQuery) {
    const { startDate, endDate } = query;
    const conditions: SQL[] = [eq(orders.wholesaler_id, wholesalerId)];

    if (startDate !== undefined) {
      conditions.push(gte(orders.created_at, startDate));
    }

    if (endDate !== undefined) {
      const endExclusive = new Date(endDate);
      endExclusive.setUTCDate(endExclusive.getUTCDate() + 1);
      conditions.push(lt(orders.created_at, endExclusive.toISOString()));
    }

    return conditions;
  }

  /**
   * 获取批发商拥有的订单数量，成交额以及状态
   * @param wholesalerId
   * @param query
   * @private
   */
  private async getSummaryAndStatus(
    wholesalerId: string,
    query: IDashboardQuery,
  ) {
    const [summary] = await this.drizzle.db
      .select({
        totalOrders: sql<number>`COUNT(*)::int`,
        pendingOrders: sql<number>`COUNT(*) FILTER (WHERE ${orders.status} = ${OrderStatus.PENDING})::int`,
        acceptedOrders: sql<number>`COUNT(*) FILTER (WHERE ${orders.status} = ${OrderStatus.ACCEPTED})::int`,
        rejectedOrders: sql<number>`COUNT(*) FILTER (WHERE ${orders.status} = ${OrderStatus.REJECTED})::int`,
        cancelledOrders: sql<number>`COUNT(*) FILTER (WHERE ${orders.status} = ${OrderStatus.CANCELLED})::int`,
        totalRevenue: sql<string>`COALESCE(SUM(${orders.total}) FILTER (WHERE ${orders.status} = ${OrderStatus.ACCEPTED}), 0)::numeric(20,2)::text`,
        averageOrderValue: sql<string>`COALESCE(ROUND(AVG(${orders.total}) FILTER (WHERE ${orders.status} = ${OrderStatus.ACCEPTED}), 2), 0)::numeric(20,2)::text`,
      })
      .from(orders)
      .where(and(...this.buildDashboardWhere(wholesalerId, query)));

    return (
      summary ?? {
        totalOrders: 0,
        pendingOrders: 0,
        acceptedOrders: 0,
        rejectedOrders: 0,
        cancelledOrders: 0,
        totalRevenue: '0.00',
        averageOrderValue: '0.00',
      }
    );
  }

  /**
   * 获取批发商订单每日收入趋势
   * @param wholesalerId
   * @param query
   * @private
   */
  private async getRevenueTrend(
    wholesalerId: string,
    query: IDashboardQuery,
  ): Promise<IDashboardResponse['revenueTrend']> {
    const dateExpr = sql<string>`to_char(${orders.created_at}, 'YYYY-MM-DD')`;

    return this.drizzle.db
      .select({
        date: dateExpr,
        orderCount: sql<number>`COUNT(*)::int`,
        acceptedCount: sql<number>`COUNT(*) FILTER (WHERE ${orders.status} = ${OrderStatus.ACCEPTED})::int`,
        revenue: sql<string>`COALESCE(SUM(${orders.total}) FILTER (WHERE ${orders.status} = ${OrderStatus.ACCEPTED}), 0)::numeric(20,2)::text`,
      })
      .from(orders)
      .where(and(...this.buildDashboardWhere(wholesalerId, query)))
      .groupBy(dateExpr)
      .orderBy(asc(dateExpr));
  }

  /**
   * 获取批发商订单中销量最高的产品
   * @param wholesalerId
   * @param query
   * @param topLimit
   * @private
   */
  private async getTopSellingProducts(
    wholesalerId: string,
    query: IDashboardQuery,
    topLimit: number,
  ): Promise<IDashboardResponse['topSellingProducts']> {
    const conditions: SQL[] = [
      ...this.buildDashboardWhere(wholesalerId, query),
      eq(orders.status, OrderStatus.ACCEPTED),
    ];

    return this.drizzle.db
      .select({
        productId: order_details.product_id,
        productName: order_details.product_name,
        productTranslation: sql<
          IDashboardResponse['topSellingProducts'][number]['productTranslation']
        >`${order_details.product_translations_snapshot}`,
        soldQuantity: sql<number>`SUM(${order_details.quantity})::int`,
        revenue: sql<string>`COALESCE(SUM(${order_details.total}), 0)::numeric(20,2)::text`,
        orderCount: sql<number>`COUNT(DISTINCT ${order_details.order_id})::int`,
      })
      .from(order_details)
      .innerJoin(orders, eq(order_details.order_id, orders.id))
      .where(and(...conditions))
      .groupBy(
        order_details.product_id,
        order_details.product_name,
        order_details.product_translations_snapshot,
      )
      .orderBy(desc(sql`SUM(${order_details.quantity})`))
      .limit(topLimit);
  }
}
