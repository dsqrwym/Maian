import { Injectable, NotFoundException } from '@nestjs/common';
import { IFindEmployeeQuery } from '#/enterprise/dto/find-employee-query.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { users, wholesaler_staffs } from '#/generated/drizzle/schema.js';
import { and, count, eq, ilike, or, sql, SQL } from 'drizzle-orm';
import { ENV } from '#/config/constants.config.js';
import { ConfigService } from '@nestjs/config';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import { SQL_IMMUTABLE_UNACCENT } from '#/drizzle/drizzle.constants.js';
import { EmployeeSortByFields } from '#/enterprise/enterprise.enums.js';
import { UserRole, UserStatus } from '#/generated/drizzle/enums.js';
import { PaginationMetaDto } from '#/utils/dto/pagination.dto.js';

@Injectable()
export class ReadEmployeeService {
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzleService: DrizzleService,
    private readonly configService: ConfigService,
  ) {
    this.MAX_SEARCH_TERMS = Number(
      this.configService.get<number>(ENV.MAX_SEARCH_TERMS, 10),
    );
  }

  getSortFieldDrizzle(sortBy?: EmployeeSortByFields) {
    // COALESCE 代表 如果左值为 null 则返回右值，缺点是没有索引 以后优化
    switch (sortBy) {
      case EmployeeSortByFields.Email:
        return users.email;
      case EmployeeSortByFields.FirstName:
        return users.first_name;
      case EmployeeSortByFields.LastName:
        return users.last_name;
      case EmployeeSortByFields.Username:
        return users.username;
      case EmployeeSortByFields.TaxId:
        return users.tax_id;
      case EmployeeSortByFields.Telephone:
        return users.telephone;
      case EmployeeSortByFields.USER_ID:
        return users.user_id;
      default:
        return undefined;
    }
  }

  async findAllEmployees(
    query: IFindEmployeeQuery,
    wholesalerId: string,
  ): Promise<{
    items: {
      id: string;
      user_id: string | null;
      first_name: string | null;
      last_name: string | null;
      email: string;
      username: string | null;
      telephone: string | null;
      tax_id: string | null;
      role: UserRole;
      status: UserStatus;
    }[];
    pagination: PaginationMetaDto;
  }> {
    const { search, role } = query;
    const { page, limit, sortBy, sortOrder } = query;
    const offset = (page - 1) * limit;
    const sortField = this.getSortFieldDrizzle(sortBy);

    let employeeQuery = this.drizzleService.db
      .select({
        id: users.id,
        user_id: users.user_id,
        first_name: users.first_name,
        last_name: users.last_name,
        email: users.email,
        username: users.username,
        telephone: users.telephone,
        tax_id: users.tax_id,
        role: wholesaler_staffs.role,
        status: users.status,
      })
      .from(users)
      .innerJoin(
        wholesaler_staffs,
        eq(users.id, wholesaler_staffs.staff_user_id),
      )
      .$dynamic();

    // 构建 WHERE 条件
    const whereConditions: (SQL | undefined)[] = [
      eq(wholesaler_staffs.wholesaler_id, wholesalerId),
    ];
    if (search) {
      // 精确匹配跨字段关键词
      const searchTerms = escapeLike(toUnaccent(search))
        .split(/\s+/)
        .filter((s) => s.length > 0)
        .slice(0, this.MAX_SEARCH_TERMS);
      for (const keyWord of searchTerms) {
        const likeSearch = `%${keyWord}%`;
        whereConditions.push(
          or(
            ilike(users.user_id, likeSearch),
            ilike(users.telephone, likeSearch),
            ilike(users.tax_id, likeSearch),
            ilike(SQL_IMMUTABLE_UNACCENT(users.email), likeSearch),
            ilike(SQL_IMMUTABLE_UNACCENT(users.first_name), likeSearch),
            ilike(SQL_IMMUTABLE_UNACCENT(users.last_name), likeSearch),
            ilike(SQL_IMMUTABLE_UNACCENT(users.username), likeSearch),
          ),
        );
      }
    }

    if (role) {
      whereConditions.push(eq(wholesaler_staffs.role, role));
    }

    const whereClause = and(...whereConditions);
    employeeQuery = employeeQuery
      .where(whereClause)
      .limit(limit)
      .offset(offset);

    if (sortField) {
      employeeQuery = employeeQuery.orderBy(
        sql`${sortField} ${sql.raw(sortOrder ?? 'asc')}`,
      );
    }

    const [items, [countResult]] = await Promise.all([
      employeeQuery,
      this.drizzleService.db
        .select({ count: count() })
        .from(users)
        .innerJoin(
          wholesaler_staffs,
          eq(users.id, wholesaler_staffs.staff_user_id),
        )
        .where(whereClause),
    ]);

    const total = countResult.count ?? 0;

    items.forEach((item) => {
      item.username = item.username?.split('@')[1] ?? '';
    });

    return {
      items,
      pagination: { total, page, limit },
    };
  }

  async getForUpdate(employeeId: string, wholesalerId: string) {
    const [employee] = await this.drizzleService.db
      .select({
        first_name: users.first_name,
        last_name: users.last_name,
        username: users.username,
        telephone: users.telephone,
        tax_id: users.tax_id,
      })
      .from(wholesaler_staffs)
      .innerJoin(users, eq(wholesaler_staffs.staff_user_id, users.id))
      .where(
        and(
          eq(wholesaler_staffs.staff_user_id, employeeId),
          eq(wholesaler_staffs.wholesaler_id, wholesalerId),
        ),
      );

    if (!employee) {
      throw new NotFoundException('Employee not found');
    }
    employee.username = employee.username?.split('@')[1] ?? '';
    return employee;
  }
}
