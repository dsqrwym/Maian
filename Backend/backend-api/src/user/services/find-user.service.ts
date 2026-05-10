import { ForbiddenException, Injectable } from '@nestjs/common';
import { IFindUserQueryDto } from '../dto/find-user-query.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { Action } from '#/casl/actions.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { UserPayload } from '#/auth/auth.types.js';
import { and, count, eq, ilike, notInArray, or, sql } from 'drizzle-orm';
import { users } from '#/generated/drizzle/schema.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { FindUserResponse } from '../dto/user-response.js';

@Injectable()
export class FindUserService {
  constructor(private readonly drizzleService: DrizzleService) {}
  /*
  async findUser(query: FindUserQueryDto, ability: AppAbility) {
    if (!ability.can(Action.Read, 'users')) {
      throw new ForbiddenException('You do not have permission to find users');
    }
    const permissionCondition: usersWhereInput = accessibleBy(
      ability,
      Action.Read,
    ).users;

    const {
      search,
      role,
      status,
      selectUserStatus,
      selectUserRole,
      user_id,
      username,
      email,
      first_name,
      last_name,
      telephone,
      cif,
      profile,
      page,
      limit,
    } = query;

    const select: Prisma.usersSelect = {
      id: true,
      ...(selectUserStatus && { status: true }),
      ...(selectUserRole && { role: true }),
      ...(user_id && { user_id: true }),
      ...(username && { username: true }),
      ...(email && { email: true }),
      ...(first_name && { first_name: true }),
      ...(last_name && { last_name: true }),
      ...(telephone && { telephone: true }),
      ...(cif && { cif: true }),
      ...(profile && { profile: true }),
    };

    const andClauses: usersWhereInput[] = [permissionCondition];

    if (search) {
      andClauses.push({
        OR: [
          { username: { contains: search, mode: 'insensitive' } },
          { first_name: { contains: search, mode: 'insensitive' } },
          { last_name: { contains: search, mode: 'insensitive' } },
          { email: { contains: search, mode: 'insensitive' } },
          { telephone: { contains: search, mode: 'insensitive' } },
          { cif: { contains: search, mode: 'insensitive' } },
          { user_id: { contains: search, mode: 'insensitive' } },
        ],
      });
    }

    if (!permissionCondition.role) {
      andClauses.push({ role: role });
    }

    if (!permissionCondition.status) {
      andClauses.push({ status: status });
    }

    const where: usersWhereInput = {
      AND: andClauses,
    };

    const [users, total] = await this.prismaService.$transaction([
      this.prismaService.users.findMany({
        where,
        select,
        orderBy: {
          [query.orderBy ?? 'created_at']: query.orderDir ?? 'desc',
        },
        skip: (page - 1) * limit,
        take: limit,
      }),
      this.prismaService.users.count({ where }),
    ]);

    const result: PaginatedData = {
      items: users,
      pagination: {
        total: total,
        page: page,
        limit: limit,
      },
    };

    return result;
  }*/

  getPermissionCondition(user: UserPayload) {
    switch (user.userRole) {
      case UserRole.ADMIN:
        return notInArray(users.role, ['ADMIN', 'SUPERADMIN']);

      case UserRole.RETAILER:
        return and(
          eq(users.role, UserRole.WHOLESALER),
          notInArray(users.status, [
            'INACTIVE',
            'BANNED',
            'PENDING_VERIFICATION',
            'PENDING_REVIEW',
          ]),
        );
      default:
        return undefined;
    }
  }

  async findUser(
    query: IFindUserQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    if (!ability.can(Action.Read, 'users')) {
      throw new ForbiddenException('You do not have permission to find users');
    }
    const permissionCondition = this.getPermissionCondition(user);

    const {
      search,
      role,
      status,
      selectUserStatus,
      selectUserRole,
      user_id,
      username,
      email,
      first_name,
      last_name,
      telephone,
      cif,
      profile,
      page,
      limit,
    } = query;

    const mainQuery = this.drizzleService.db
      .select({
        id: users.id,
        ...(selectUserStatus && { status: users.status }),
        ...(selectUserRole && { role: users.role }),
        ...(user_id && { user_id: users.user_id }),
        ...(username && { username: users.username }),
        ...(email && { email: users.email }),
        ...(first_name && { first_name: users.first_name }),
        ...(last_name && { last_name: users.last_name }),
        ...(telephone && { telephone: users.telephone }),
        ...(cif && { cif: users.tax_id }),
        ...(profile && { profile: users.profile }),
      })
      .from(users)
      .$dynamic();

    const whereConditions = [permissionCondition];

    if (search) {
      const searchPattern = `%${search}%`;
      whereConditions.push(
        or(
          ilike(users.username, searchPattern),
          ilike(users.first_name, searchPattern),
          ilike(users.last_name, searchPattern),
          ilike(users.email, searchPattern),
          ilike(users.telephone, searchPattern),
          ilike(users.tax_id, searchPattern),
          ilike(users.user_id, searchPattern),
        ),
      );
    }

    if (!permissionCondition) {
      whereConditions.push(role ? eq(users.role, role) : undefined);
      whereConditions.push(status ? eq(users.status, status) : undefined);
    }

    const [items, total] = await Promise.all([
      mainQuery
        .where(and(...whereConditions))
        .orderBy(
          sql.raw(
            `${query.orderBy ?? 'created_at'} ${query.orderDir ?? 'desc'}`,
          ),
        )
        .limit(limit)
        .offset((page - 1) * limit),
      this.drizzleService.db
        .select({ count: count() })
        .from(users)
        .where(and(...whereConditions)),
    ]);

    const result: PaginatedDataWithT<FindUserResponse> = {
      items,
      pagination: {
        total: total[0]?.count ?? 0,
        page: page,
        limit: limit,
      },
    };

    return result;
  }
}
