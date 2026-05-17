import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { and, eq, max, min } from 'drizzle-orm';
import { orders } from '#/generated/drizzle/schema.js';

@Injectable()
export class FilterOrderMetadataService {
  constructor(private readonly drizzle: DrizzleService) {}

  async getFilterOrderMetadata(
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

    const [metadata] = await this.drizzle.db
      .select({
        max_total: max(orders.total),
        min_total: min(orders.total),

        max_subtotal: max(orders.subtotal),
        min_subtotal: min(orders.subtotal),

        max_iva_total: max(orders.iva_total),
        min_iva_total: min(orders.iva_total),

        max_item_count: max(orders.item_count),
        min_item_count: min(orders.item_count),
      })
      .from(orders)
      .where(
        and(
          retailerId ? eq(orders.retailer_id, retailerId) : undefined,
          wholesalerId ? eq(orders.wholesaler_id, wholesalerId) : undefined,
        ),
      );

    if (!metadata) {
      throw new NotFoundException('You are not allowed to read orders');
    }

    return metadata;
  }
}
