import { ForbiddenException, Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { FindUserQueryDto } from '../dto/find-user-query.dto';
import { AppAbility } from '../../casl/casl-types';
import { Prisma } from '@prisma/client';
import { accessibleBy } from '@casl/prisma';
import { Action } from '../../casl/actions';
import usersWhereInput = Prisma.usersWhereInput;
import { ToPaginated } from '../../common/types/response.type';

@Injectable()
export class FindUserService {
  constructor(private readonly prismaService: PrismaService) {}
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

    const result: ToPaginated = {
      items: users,
      meta: {
        total: total,
        page: page,
        limit: limit,
      },
    };

    return result;
  }
}
