import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ICreateCategoryDto } from '../dto/create-category.dto';
import { IUpdateCategoryDto } from '../dto/update-category.dto';
import { Logger } from 'nestjs-pino';
import { AppAbility } from '@/casl/casl-types';
import { Action } from '@/casl/actions';
import { subject } from '@casl/ability';
import { ICategoryQueryDto } from '../dto/category-query.dto';
import { PaginatedDataWithT } from '@/common/types-interfaces/response.interface';
import { UserPayload } from '@/auth/auth.types';
import { CategorySelectField, CategoryType } from '../category.enums';
import {
  ICategoryResponse,
  ICategoryResponseRelation,
} from '../dto/category-response.dto';
import { DrizzleService } from 'src/drizzle/drizzle.service';
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
import { categories, category_translations } from '@/generated/drizzle/schema';
import { toUnaccent } from '@/utils/string.util';
import { UserRole } from '@/generated/drizzle/enums';

@Injectable()
export class CategoryService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: Logger,
  ) {}

  async create(
    createCategoryDto: ICreateCategoryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const { userId, name, iva, translations } = createCategoryDto;
    const parentId = createCategoryDto.parentId
      ? BigInt(createCategoryDto.parentId)
      : undefined;
    if (
      !ability.can(Action.Create, subject('categories', { user_id: userId }))
    ) {
      throw new ForbiddenException(
        'You do not have permission to create categories',
      );
    }

    return this.drizzle.db.transaction(async (tx) => {
      const existingCategorySubQuery = tx
        .select({ one: sql<number>`1` })
        .from(categories)
        .where(
          and(
            eq(categories.name, name),
            userId
              ? eq(categories.user_id, userId)
              : isNull(categories.user_id),
            parentId ? eq(categories.parent_id, parentId) : undefined,
          ),
        );
      const existing = (await tx
        .select({ exists: exists(existingCategorySubQuery) })
        .from(sql`(VALUES (1)) AS tmp`)
        .execute()) as { exists: boolean }[];

      if (existing[0]?.exists) {
        const scope = userId ? 'private' : 'public';
        this.logger.warn(
          `Category '${name}' already exists in ${scope} scope for user ${userId || 'global'}`,
        );
        throw new ConflictException(
          `A category with the name '${name}' already exists in this scope`,
        );
      }

      let level = 1;

      if (parentId) {
        const parent = await tx.query.categories.findFirst({
          columns: {
            id: true,
            user_id: true,
            level: true,
          },
          where: (categories, { eq }) => eq(categories.id, parentId),
        });

        if (!parent) {
          throw new NotFoundException(
            `Parent category with ID ${parentId} not found`,
          );
        }

        // 公共不能挂在私有下
        if (parent.user_id && userId === null) {
          throw new BadRequestException(
            'Public category cannot have private parent',
          );
        }

        // 层级 = 父类层级 + 1
        level = parent.level + 1;
        if (level > 3) {
          throw new BadRequestException(
            'Cannot create more than 3 levels of categories',
          );
        }
      }

      try {
        const newCategory = await tx
          .insert(categories)
          .values({
            user_id: userId,
            name,
            parent_id: parentId,
            level,
            iva,
            created_by: user.userId,
          })
          .returning({ id: categories.id });
        const newCategoryId = newCategory[0].id;

        if (translations && translations.length > 0) {
          await tx
            .insert(category_translations)
            .values(
              translations.map((translation) => ({
                category_id: newCategoryId,
                lang_code: translation.lang_code,
                name: translation.name,
              })),
            )
            .onConflictDoNothing();
        }

        this.logger.log(
          `Created category '${name}' (level ${level}) for user ${userId || 'global'}`,
        );
        return;
      } catch (error) {
        this.logger.error(
          { error, categoryName: name, userId: userId },
          'Failed to create category',
        );
        throw error;
      }
    });
  }

  // 主要针对批发商端
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
    const childrenCountLateral = this.drizzle.db
      .select({
        count: count().as('children_count'),
      })
      .from(categories)
      .where(eq(categories.parent_id, categories.id)) // 这里的 categories.id 指向外层主表
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
    // 动态 WHERE 条件
    const whereConditions: (SQL | undefined)[] = [];

    if (search) {
      const pattern = `%${toUnaccent(search)}%`;
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
    }

    // 处理权限逻辑
    if (permissionCondition === undefined) {
      if (userId) whereConditions.push(eq(categories.user_id, userId));
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
    if (query.maxLevel != undefined) {
      whereConditions.push(lte(categories.level, query.maxLevel));
    }
    const finalWhere = and(...whereConditions);

    // 执行查询
    const [items, countResult] = await Promise.all([
      mainQuery
        .where(finalWhere)
        .orderBy(categories.name)
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
      where: (categories, { eq }) => eq(categories.id, BigInt(id)),
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

  async update(
    categoryId: string,
    updateCategoryDto: IUpdateCategoryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const id = BigInt(categoryId);
    const clientVersion = BigInt(updateCategoryDto.version);

    const existingCategory = await this.drizzle.db.query.categories.findFirst({
      where: eq(categories.id, id),
      columns: { user_id: true },
    });

    if (!existingCategory) throw new NotFoundException('Category not found');

    if (
      !ability.can(
        Action.Update,
        subject('categories', {
          user_id: existingCategory?.user_id ?? undefined,
        }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to update categories',
      );
    }

    const { name, iva, translations, translationsToDelete } = updateCategoryDto;

    return this.drizzle.db.transaction(async (tx) => {
      const result = await tx
        .update(categories)
        .set({
          ...(name !== undefined && { name: name }),
          ...(iva !== undefined && { iva: iva }),
          updated_at: sql`(NOW() AT TIME ZONE 'UTC')`,
          updated_by: user.userId,
          version: sql`${categories.version} + 1`,
        })
        .where(
          and(eq(categories.id, id), eq(categories.version, clientVersion)),
        );

      if ((result.rowCount ?? 0) === 0) {
        throw new ConflictException(`Category has been modified.`);
      }

      if (translationsToDelete && translationsToDelete.length > 0) {
        await tx
          .delete(category_translations)
          .where(
            and(
              eq(category_translations.category_id, id),
              inArray(category_translations.lang_code, translationsToDelete),
            ),
          );
      }

      if (translations && translations.length > 0) {
        await tx
          .insert(category_translations)
          .values(
            translations.map((t) => ({
              category_id: id,
              lang_code: t.lang_code,
              name: t.name,
              updated_by: user.userId,
            })),
          )
          .onConflictDoUpdate({
            target: [
              category_translations.category_id,
              category_translations.lang_code,
            ],
            set: {
              name: sql`EXCLUDED.name`,
              updated_at: sql`(NOW() AT TIME ZONE 'UTC')`,
              updated_by: user.userId,
            },
          });
      }
    });
  }

  async remove(categoryId: string, ability: AppAbility) {
    const id = BigInt(categoryId);
    const [category] = await this.drizzle.db
      .select({ user_id: categories.user_id })
      .from(categories)
      .where(eq(categories.id, id))
      .limit(1);

    if (
      !ability.can(
        Action.Delete,
        subject('categories', {
          user_id: category?.user_id ?? undefined,
        }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to delete categories',
      );
    }

    await this.drizzle.db.delete(categories).where(eq(categories.id, id));
  }
}
