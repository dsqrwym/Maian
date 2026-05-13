import { Injectable } from '@nestjs/common';
import {
  ICheckCategoryNameCreateQueryDto,
  ICheckCategoryNameUpdateQueryDto,
} from '../dto/check-category-query.dto.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { categories } from '#/generated/drizzle/schema.js';
import { and, eq, exists, isNull, ne, sql } from 'drizzle-orm';
import { SQL_TEMP_TABLE } from '#/drizzle/drizzle.constants.js';
@Injectable()
export class CheckCategoryService {
  constructor(private readonly drizzle: DrizzleService) {}

  async checkNameUsedForCreate(query: ICheckCategoryNameCreateQueryDto) {
    const { name, userId } = query;
    const subquery = this.drizzle.db
      .select({ one: sql<number>`1` }) // SELECT 1
      .from(categories)
      .where(
        and(
          eq(categories.name, name),
          userId ? eq(categories.user_id, userId) : isNull(categories.user_id),
        ),
      );
    const [result] = (await this.drizzle.db
      .select({ exists: exists(subquery) })
      .from(SQL_TEMP_TABLE)
      .execute()) as { exists: boolean }[];

    return result?.exists ?? false;
  }

  async checkNameUsedForUpdate(query: ICheckCategoryNameUpdateQueryDto) {
    const { id, name, userId } = query;
    const excludeId = BigInt(id);
    const subquery = this.drizzle.db
      .select({ one: sql<number>`1` }) // SELECT 1
      .from(categories)
      .where(
        and(
          eq(categories.name, name),
          userId ? eq(categories.user_id, userId) : isNull(categories.user_id),
          ne(categories.id, excludeId),
        ),
      );
    const [result] = (await this.drizzle.db
      .select({ exists: exists(subquery) })
      .from(SQL_TEMP_TABLE)
      .execute()) as { exists: boolean }[];
    return result?.exists ?? false;
  }
}
