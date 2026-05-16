import { ForbiddenException, Injectable } from '@nestjs/common';
import { AppAbility } from '#/casl/casl-types.js';
import { AddressType, UserRole } from '#/generated/drizzle/enums.js';
import { Action } from '#/casl/actions.js';
import {
  and,
  asc,
  count,
  desc,
  eq,
  ilike,
  inArray,
  or,
  SQL,
  sql,
} from 'drizzle-orm';
import {
  cities,
  directions,
  provinces,
  users,
} from '#/generated/drizzle/schema.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { IFindWholesalerQueryDto } from '#/user/dto/find-wholesaler-query.dto.js';
import { caslToDrizzle } from '#/casl/casl-to-drizzle.js';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import { ENV } from '#/config/constants.config.js';
import { ConfigService } from '@nestjs/config';
import { PinoLogger } from 'nestjs-pino';
import { WholesalerSortField } from '#/user/user.enums.js';
import { OrderByEnum } from '#/common/enums/sort.enum.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import {
  SQL_IMMUTABLE_UNACCENT,
  SQL_TRUE,
} from '#/drizzle/drizzle.constants.js';
import { buildWholesalerProfileExpr } from '#/utils/db/user.db.utils.js';
import { MARKETPLACE_VISIBLE_STATUSES } from '#/user/user-status.constants.js';

@Injectable()
export class ReadWholesalerService {
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzleService: DrizzleService,
    private readonly configService: ConfigService,
    private readonly logger: PinoLogger,
  ) {
    this.MAX_SEARCH_TERMS = Number(
      this.configService.get<number>(ENV.MAX_SEARCH_TERMS, 10),
    );
    this.logger.setContext(ReadWholesalerService.name);
  }

  async findWholesalers(
    query: IFindWholesalerQueryDto,
    ability: AppAbility,
  ): Promise<
    PaginatedDataWithT<{
      id: string;
      user_id: string | null;
      profile_image_file_id: bigint | null;
      display_name: string | null | undefined;
      company_name: string;
      company_type: string;
      description: string | null | undefined;
      delivery_available: boolean | null | undefined;
      pickup_available: boolean | null | undefined;
      minimum_order_amount: string | null | undefined;
      delivery_area_description: string | null | undefined;
      city: { name: string; name_local: string; id: number } | null;
      province: { name: string; name_local: string; id: number } | null;
    }>
  > {
    if (!ability.can(Action.Read, 'users')) {
      throw new ForbiddenException('You are not allowed to read wholesalers');
    }

    const {
      search,
      delivery_available,
      pickup_available,
      company_type,
      page,
      limit,
      orderDir,
      orderBy,
    } = query;
    const offset = (page - 1) * limit;

    const {
      companyNameExpr,
      companyTypeExpr,
      displayNameExpr,
      descriptionExpr,
      deliveryAreaDescriptionExpr,
      minimumOrderAmountExpr,
      deliveryAvailableExpr,
      pickupAvailableExpr,
    } = buildWholesalerProfileExpr(users.profile);

    const abilityConditions = caslToDrizzle(
      ability,
      Action.Read,
      'users',
      users,
    );

    const directionLateral = this.drizzleService.db
      .select({
        street: directions.street,
        zip_code: directions.zip_code,
        city_name: sql<string>`${cities.name}`.as('city_name'),
        city_name_local: sql<string>`${cities.name_local}`.as(
          'city_name_local',
        ),
        province_name: sql<string>`${provinces.name}`.as('province_name'),
        province_name_local: sql<string>`${provinces.name_local}`.as(
          'province_name_local',
        ),
        city: sql<{ name: string; name_local: string; id: number } | null>`
          jsonb_build_object(
            'name', ${cities.name},
            'name_local', ${cities.name_local},
            'id', ${cities.id}
          )
        `.as('city'),
        province: sql<{ name: string; name_local: string; id: number } | null>`
          jsonb_build_object(
            'name', ${provinces.name},
            'name_local', ${provinces.name_local},
            'id', ${provinces.id}
          )
        `.as('province'),
      })
      .from(directions)
      .innerJoin(cities, eq(cities.id, directions.city_id))
      .innerJoin(provinces, eq(provinces.id, directions.province_id))
      .where(
        and(
          eq(directions.user_id, users.id),
          eq(directions.type, AddressType.STORE),
        ),
      )
      .orderBy(asc(directions.id))
      .limit(1)
      .as('directionsLateral');

    // 构建 WHERE 条件
    const whereConditions: (SQL | undefined)[] = [
      abilityConditions,
      eq(users.role, UserRole.WHOLESALER),
      inArray(users.status, MARKETPLACE_VISIBLE_STATUSES),
    ];

    if (search) {
      const searchTerms = escapeLike(toUnaccent(search))
        .split(/\s+/)
        .filter((s) => s.length > 0)
        .slice(0, this.MAX_SEARCH_TERMS);

      for (const term of searchTerms) {
        const pattern = `%${term}%`;
        whereConditions.push(
          or(
            ilike(users.user_id, pattern),
            ilike(SQL_IMMUTABLE_UNACCENT(displayNameExpr), pattern),
            ilike(SQL_IMMUTABLE_UNACCENT(companyNameExpr), pattern),
            ilike(SQL_IMMUTABLE_UNACCENT(descriptionExpr), pattern),
            ilike(SQL_IMMUTABLE_UNACCENT(minimumOrderAmountExpr), pattern),
            ilike(SQL_IMMUTABLE_UNACCENT(directionLateral.city_name), pattern),
            ilike(
              SQL_IMMUTABLE_UNACCENT(directionLateral.city_name_local),
              pattern,
            ),
            ilike(
              SQL_IMMUTABLE_UNACCENT(directionLateral.province_name),
              pattern,
            ),
            ilike(
              SQL_IMMUTABLE_UNACCENT(directionLateral.province_name_local),
              pattern,
            ),
          ),
        );
      }
    }

    if (delivery_available !== undefined) {
      whereConditions.push(eq(deliveryAvailableExpr, delivery_available));
    }

    if (pickup_available !== undefined) {
      whereConditions.push(eq(pickupAvailableExpr, pickup_available));
    }

    if (company_type !== undefined) {
      whereConditions.push(eq(companyTypeExpr, company_type));
    }

    // 构建 排序
    const getSortField = (): SQL => {
      switch (orderBy) {
        case WholesalerSortField.DISPLAY_NAME:
          return sql`lower(coalesce(${displayNameExpr}, ${companyNameExpr}))`;

        case WholesalerSortField.COMPANY_NAME:
          return sql`lower(${companyNameExpr})`;

        case WholesalerSortField.CITY:
          return sql`lower(coalesce(${directionLateral.city_name_local}, ${directionLateral.city_name}))`;

        case WholesalerSortField.PROVINCE:
          return sql`lower(coalesce(${directionLateral.province_name_local}, ${directionLateral.province_name}))`;

        case WholesalerSortField.MINIMUM_ORDER_AMOUNT:
          return sql`nullif(${minimumOrderAmountExpr}, '')::numeric`;

        default:
          return sql`lower(coalesce(${displayNameExpr}, ${companyNameExpr}))`;
      }
    };

    const sortField = getSortField();
    const sortDirection =
      orderDir === OrderByEnum.DESC ? desc(sortField) : asc(sortField);

    const whereClause = and(...whereConditions);

    const [items, totalResult] = await Promise.all([
      this.drizzleService.db
        .select({
          id: users.id,
          user_id: users.user_id,
          profile_image_file_id: users.profile_image_file_id,
          display_name: displayNameExpr,
          company_name: companyNameExpr,
          company_type: companyTypeExpr,
          description: descriptionExpr,
          delivery_available: deliveryAvailableExpr,
          pickup_available: pickupAvailableExpr,
          minimum_order_amount: minimumOrderAmountExpr,
          delivery_area_description: deliveryAreaDescriptionExpr,
          city: directionLateral.city,
          province: directionLateral.province,
        })
        .from(users)
        .leftJoinLateral(directionLateral, SQL_TRUE)
        .where(whereClause)
        .orderBy(
          sortDirection,
          asc(sql`lower(${companyNameExpr})`),
          asc(users.id),
        )
        .limit(limit)
        .offset(offset),

      this.drizzleService.db
        .select({
          total: count(),
        })
        .from(users)
        .leftJoinLateral(directionLateral, SQL_TRUE)
        .where(whereClause),
    ]);

    const total = totalResult[0]?.total ?? 0;

    return {
      items,
      pagination: { total, page, limit },
    };
  }
}
