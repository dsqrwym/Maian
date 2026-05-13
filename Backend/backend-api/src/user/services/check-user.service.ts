import { Injectable } from '@nestjs/common';
import { ICheckUserUsernameQueryDto } from '../dto/check-user-query.dto.js';
import { UserRole, UserStatus } from '#/generated/drizzle/enums.js';
import { makeUsername } from '#/utils/user.utils.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { and, eq, exists, ne, sql } from 'drizzle-orm';
import { users } from '#/generated/drizzle/schema.js';
import { SQL_TEMP_TABLE } from '#/drizzle/drizzle.constants.js';

@Injectable()
export class CheckUserService {
  constructor(private readonly drizzleService: DrizzleService) {}
  async checkEmailUsed(email: string) {
    const [user] = (await this.drizzleService.db
      .select({
        exists: exists(
          this.drizzleService.db
            .select({ one: sql<number>`1` })
            .from(users)
            .where(
              and(
                eq(users.email, email),
                ne(users.status, UserStatus.PENDING_VERIFICATION),
              ),
            ),
        ),
      })
      .from(SQL_TEMP_TABLE)) as { exists: boolean }[];
    return user?.exists ?? false;
  }

  async checkUsernameUsed(query: ICheckUserUsernameQueryDto) {
    const userId = query.userId;
    let username = query.username;
    if (query.isAdmin) {
      username = makeUsername(UserRole.ADMIN, username);
    }
    if (query.wholesalerId) {
      username = makeUsername(query.wholesalerId, username);
    }
    const [user] = (await this.drizzleService.db
      .select({
        exists: exists(
          this.drizzleService.db
            .select({ one: sql<number>`1` })
            .from(users)
            .where(
              and(
                eq(users.username, username),
                ne(users.status, UserStatus.PENDING_VERIFICATION),
                userId ? ne(users.id, userId) : undefined,
              ),
            ),
        ),
      })
      .from(SQL_TEMP_TABLE)) as { exists: boolean }[];
    return user?.exists ?? false;
  }

  async checkUserTaxId(taxId: string, userId: string, role: UserRole) {
    const [user] = (await this.drizzleService.db
      .select({
        exists: exists(
          this.drizzleService.db
            .select({ one: sql<number>`1` })
            .from(users)
            .where(
              and(
                eq(users.tax_id, taxId),
                eq(users.role, role),
                ne(users.id, userId),
              ),
            ),
        ),
      })
      .from(SQL_TEMP_TABLE)) as { exists: boolean }[];
    return user?.exists ?? false;
  }
}
