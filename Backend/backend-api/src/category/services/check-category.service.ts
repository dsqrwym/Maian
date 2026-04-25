import { Injectable } from '@nestjs/common';
import {
  ICheckCategoryNameCreateQueryDto,
  ICheckCategoryNameUpdateQueryDto,
} from '../dto/check-category-query.dto';
import { DrizzleService } from 'src/drizzle/drizzle.service';
import { categories } from '@/generated/drizzle/schema';
import { and, eq, exists, isNull, ne, sql } from 'drizzle-orm';
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
      .from(sql`(VALUES (1)) AS tmp`)
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
      .from(sql`(VALUES (1)) AS tmp`)
      .execute()) as { exists: boolean }[];
    return result?.exists ?? false;
  }
}
