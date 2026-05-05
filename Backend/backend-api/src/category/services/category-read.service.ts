import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { subject } from '@casl/ability';
import { ICategoryQueryDto } from '../dto/category-query.dto.js';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { UserPayload } from '#/auth/auth.types.js';
import { CategorySelectField, CategoryType } from '../category.enums.js';
import {
  ICategoryResponse,
  ICategoryResponseRelation,
} from '../dto/category-response.dto.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  and,
  count,
  eq,
  exists,
  ilike,
  inArray,
  isNotNull,
  isNull,
  lte,
  or,
  SQL,
  sql,
} from 'drizzle-orm';
import {
  categories,
  category_translations,
  product_categories,
  products,
} from '#/generated/drizzle/schema.js';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import { alias } from 'drizzle-orm/pg-core';
import { caslToDrizzle } from '#/casl/casl-to-drizzle.js';

@Injectable()
export class CategoryReadService {
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
    private readonly config: ConfigService,
  ) {
    this.MAX_SEARCH_TERMS = this.config.get<number>(ENV.MAX_SEARCH_TERMS, 10);
    this.logger.setContext(CategoryReadService.name);
  }

  /**
   * 构建当前分类是否有产品关联的 EXISTS 条件。
   * mode = self:
   *   只判断当前 category 自己是否直接关联产品。
   *   适合 enterprise / 后台筛选。
   * mode = descendant:
   *   判断当前 category 自己、子类、孙类是否有关联产品。
   *   适合 standard 零售端分类浏览。
   * ownerId:
   *   用于零售商进入某个批发商店铺时，只判断该批发商的产品。
   *   例如 ownerId = wholesalerId。
   */
  buildProductLinkedCondition = (
    mode: 'self' | 'descendant',
    ability: AppAbility,
    ownerId?: string,
  ): SQL => {
    let productCondition = caslToDrizzle(
      ability,
      Action.Read,
      'products',
      products,
    );
    //ownerId 用于当零售商进入批发商店铺页面只看批发商的产品

    if (mode === 'self') {
      return exists(
        this.drizzle.db
          .select({ one: sql<number>`1` })
          .from(product_categories)
          .innerJoin(products, eq(product_categories.product_id, products.id))
          .where(
            and(
              eq(product_categories.category_id, categories.id),
              productCondition,
              // 如果指定批发商，只看该批发商产品
              ownerId ? eq(products.user_id, ownerId) : undefined,
            ),
          )
          .limit(1),
      );
    }

    productCondition = caslToDrizzle(
      ability,
      Action.Read,
      'products',
      products,
    );

    const linkedCategory = alias(categories, 'linked_category');
    const child = alias(categories, 'child');

    return exists(
      this.drizzle.db
        .select({ one: sql`1` })
        // 产品类别是否关联
        .from(product_categories)
        // 产品是否关联
        .innerJoin(products, eq(products.id, product_categories.product_id))
        // 类别是否关联
        .innerJoin(
          linkedCategory,
          eq(linkedCategory.id, product_categories.category_id),
        )
        .where(
          and(
            or(
              // 是否等于当前类别
              eq(linkedCategory.id, categories.id),
              // 是否等于当前类别的子类
              eq(linkedCategory.parent_id, categories.id),
              // 是否等于当前类别的孙类
              inArray(
                linkedCategory.parent_id,
                this.drizzle.db
                  .select({ id: child.id })
                  .from(child)
                  .where(eq(child.parent_id, categories.id)),
              ),
            ),
            productCondition,
            // 如果指定批发商，只看该批发商产品
            ownerId ? eq(products.user_id, ownerId) : undefined,
          ),
        ),
    );
  };

  getReadListPermission(user: UserPayload) {
    switch (user.userRole) {
      case UserRole.ADMIN:
      case UserRole.SUPERADMIN:
      case UserRole.RETAILER:
        return undefined;
      case UserRole.WHOLESALER:
        return user.userId;
      case UserRole.DELIVERY:
      case UserRole.SUPPORT:
      case UserRole.WAREHOUSE:
        return user.wholesalerId;
    }
  }

  async findAllUseDrizzle(
    query: ICategoryQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ): Promise<PaginatedDataWithT<ICategoryResponse>> {
    // 权限与字段控制
    if (!ability.can(Action.Read, 'categories')) {
      throw new ForbiddenException('No permission');
    }
    const permissionCondition = this.getReadListPermission(user);
    const { langCode, search, userId, fields, type, page, limit } = query;
    const parentId = query.parentId ? BigInt(query.parentId) : undefined;
    const iva = fields?.includes(CategorySelectField.IVA);
    const level = fields?.includes(CategorySelectField.LEVEL);
    const user_id = fields?.includes(CategorySelectField.USER_ID);
    const translations = fields?.includes(CategorySelectField.TRANSLATIONS);
    const relations = fields?.includes(CategorySelectField.RELATIONS);
    // 构建 jsonb 对象的内部字段
    const buildJsonFields = (alias: string) => {
      const parts = [
        sql`'id', ${sql.raw(alias)}.id`,
        sql`'name', ${sql.raw(alias)}.name`,
      ];
      if (iva) parts.push(sql`'iva', ${sql.raw(alias)}.iva::text`);
      if (level) parts.push(sql`'level', ${sql.raw(alias)}.level`);
      if (user_id) parts.push(sql`'user_id', ${sql.raw(alias)}.user_id`);
      return sql.join(parts, sql`, `);
    };

    const offset = (page - 1) * limit;

    // 翻译子查询
    const transLateral = this.drizzle.db
      .select({
        category_translations: sql<
          { lang_code: string; name: string }[]
        >`COALESCE(jsonb_agg(jsonb_build_object(
        'lang_code', ${category_translations.lang_code},
        'name', ${category_translations.name}
      )), '[]'::jsonb)`.as('category_translations'),
      })
      .from(category_translations)
      .where(
        and(
          eq(category_translations.category_id, categories.id),
          langCode ? eq(category_translations.lang_code, langCode) : undefined,
        ),
      )
      .as('transLateral');

    // 子分类数量
    const childCategories = alias(categories, 'child_categories');

    const childrenCountLateral = this.drizzle.db
      .select({
        count: count().as('children_count'),
      })
      .from(childCategories)
      .where(eq(childCategories.parent_id, categories.id))
      .as('childrenCountLateral');

    // 执行主查询
    let mainQuery = this.drizzle.db
      .select({
        id: categories.id,
        name: categories.name,
        ...(iva && { iva: categories.iva }),
        ...(level && { level: categories.level }),
        ...(user_id && { user_id: categories.user_id }),
        ...(translations && {
          category_translations: transLateral.category_translations,
        }),
        ...(query.withChildrenCount && {
          children_count: childrenCountLateral.count,
        }),
        // Relations (parent/children) 的 JSON 构建 使用 sql 片段以保持嵌套结构
        ...(relations && {
          parent: sql<ICategoryResponseRelation>`(
            SELECT jsonb_build_object(
              ${buildJsonFields('p')},
              'parent', (
                SELECT jsonb_build_object(${buildJsonFields('pp')})
                FROM categories pp
                WHERE pp.id = p.parent_id
              )
            ) 
            FROM categories p
            WHERE p.id = ${categories.parent_id}
          )`,
          children: sql<ICategoryResponseRelation[]>`COALESCE((
            SELECT jsonb_agg(jsonb_build_object(${buildJsonFields('ch')}))
            FROM categories ch WHERE ch.parent_id = ${categories.id}
            ${userId ? sql`AND ch.user_id = ${userId}` : sql``}
          ), '[]'::jsonb)`,
        }),
      })
      .from(categories)
      .$dynamic();

    if (translations) {
      mainQuery = mainQuery.leftJoinLateral(transLateral, sql`TRUE`);
    }
    if (query.withChildrenCount) {
      mainQuery = mainQuery.leftJoinLateral(childrenCountLateral, sql`TRUE`);
    }

    let localizedNameSort: SQL = sql`${categories.name}`;
    if (langCode) {
      // langCode 在就根据当前语言排序
      const localizedSortLateral = this.drizzle.db
        .select({
          sort_name: sql<string>`${category_translations.name}`.as('sort_name'),
        })
        .from(category_translations)
        .where(
          and(
            eq(category_translations.category_id, categories.id),
            eq(category_translations.lang_code, langCode),
          ),
        )
        .limit(1)
        .as('localizedSortLateral');

      mainQuery = mainQuery.leftJoinLateral(localizedSortLateral, sql`TRUE`);
      localizedNameSort = sql`COALESCE(${localizedSortLateral.sort_name}, ${categories.name})`;
    }
    // 动态 WHERE 条件
    const whereConditions: (SQL | undefined)[] = [];

    if (search) {
      const searchTerms = escapeLike(toUnaccent(search))
        .split(/\s+/)
        .filter((s) => s.length > 0)
        .slice(0, this.MAX_SEARCH_TERMS);

      searchTerms.forEach((key) => {
        const pattern = `%${key}%`;
        whereConditions.push(
          or(
            ilike(categories.name_unaccent, pattern),
            exists(
              this.drizzle.db
                .select({ one: sql<number>`1` })
                .from(category_translations)
                .where(
                  and(
                    eq(category_translations.category_id, categories.id),
                    ilike(category_translations.name_unaccent, pattern),
                  ),
                ),
            ),
          ),
        );
      });
    }

    // 处理权限逻辑
    if (permissionCondition === undefined) {
      if (userId && query.includePublic) {
        whereConditions.push(
          or(isNull(categories.user_id), eq(categories.user_id, userId)),
        );
      } else if (userId) whereConditions.push(eq(categories.user_id, userId));
      else if (type === CategoryType.PRIVATE)
        whereConditions.push(isNotNull(categories.user_id));
      else if (type === CategoryType.PUBLIC)
        whereConditions.push(isNull(categories.user_id));
    } else {
      if (userId)
        whereConditions.push(eq(categories.user_id, permissionCondition));
      else
        whereConditions.push(
          or(
            eq(categories.user_id, permissionCondition),
            isNull(categories.user_id),
          ),
        );
    }

    if (parentId != undefined) {
      whereConditions.push(eq(categories.parent_id, parentId));
    }
    if (query.productFilterMode) {
      whereConditions.push(
        this.buildProductLinkedCondition(
          query.productFilterMode,
          ability,
          permissionCondition ?? userId,
        ),
      );
    }
    if (query.level != undefined) {
      whereConditions.push(eq(categories.level, query.level));
    }
    if (query.maxLevel != undefined) {
      whereConditions.push(lte(categories.level, query.maxLevel));
    }

    const finalWhere = and(...whereConditions);

    const sortOrder = query.sort_order ?? 'asc';

    /**
     * public category 排在前面：
     *   user_id IS NULL => 0
     *   user_id IS NOT NULL => 1
     */
    const visibilitySort = sql`CASE WHEN ${categories.user_id} IS NULL THEN 0 ELSE 1 END`;

    const orderByExpr =
      query.sort_by === 'level'
        ? sql`
        ${visibilitySort} ASC,
        ${categories.level} ${sql.raw(sortOrder)},
        ${localizedNameSort} ASC
      `
        : sql`
        ${visibilitySort} ASC,
        ${localizedNameSort} ${sql.raw(sortOrder)}
      `;

    // 执行查询
    const [items, countResult] = await Promise.all([
      mainQuery
        .where(finalWhere)
        .orderBy(orderByExpr)
        .limit(limit)
        .offset(offset),
      this.drizzle.db
        .select({ total: count() })
        .from(categories)
        .where(finalWhere),
    ]);

    const total = countResult[0]?.total ?? 0;
    return {
      items,
      pagination: { total, page, limit },
    };
  }

  async getCategoryForUpdate(id: string, ability: AppAbility) {
    const category = await this.drizzle.db.query.categories.findFirst({
      where: eq(categories.id, BigInt(id)),
      columns: {
        user_id: true,
        name: true,
        iva: true,
        version: true,
      },
      with: {
        category_translations: {
          columns: {
            name: true,
            lang_code: true,
          },
        },
      },
    });

    if (!category) {
      throw new NotFoundException('Category not found');
    }

    if (
      !ability.can(
        Action.Read,
        subject('categories', {
          user_id: category?.user_id ?? undefined,
        }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to read categories',
      );
    }
    return {
      name: category.name,
      iva: category.iva,
      version: category.version,
      translations: category.category_translations,
    };
  }
}
