import { Injectable } from '@nestjs/common';
import { ICheckUserUsernameQueryDto } from '../dto/check-user-query.dto';
import { UserRole, UserStatus } from 'src/generated/drizzle/enums';
import { makeUsername } from '../../utils/user.utils';
import { DrizzleService } from '../../drizzle/drizzle.service';
import { and, eq, exists, ne, sql } from 'drizzle-orm';
import { users } from '../../generated/drizzle/schema';

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
      .from(sql`(VALUES (1)) AS tmp`)) as { exists: boolean }[];
    return user?.exists ?? false;
  }

  async checkUsernameUsed(query: ICheckUserUsernameQueryDto) {
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
              ),
            ),
        ),
      })
      .from(sql`(VALUES (1)) AS tmp`)) as { exists: boolean }[];
    return user?.exists ?? false;
  }
}
