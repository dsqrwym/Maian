import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ICreateCategoryDto } from '../dto/create-category.dto';
import { IUpdateCategoryDto } from '../dto/update-category.dto';
import { PrismaService } from '../../prisma/prisma.service';
import { Logger } from 'nestjs-pino';
import { AppAbility } from '../../casl/casl-types';
import { Action } from '../../casl/actions';
import { subject } from '@casl/ability';
import { ICategoryQueryDto } from '../dto/category-query.dto';
import { Prisma, UserRole } from 'src/generated/prisma/client';
import { PaginatedDataWithT } from '../../common/types-interfaces/response.interface';
import { UserPayload } from '../../auth/auth.types';
import { CategorySelectField, CategoryType } from '../category.enums';
import { ICategoryResponse } from '../dto/category-response.dto';

@Injectable()
export class CategoryService {
  constructor(
    private readonly prisma: PrismaService,
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

    return this.prisma.$transaction(async (tx) => {
      const existingCategory = await tx.categories.findFirst({
        where: {
          user_id: userId || null,
          name,
          ...(parentId && { parent_id: parentId }),
        },
      });

      if (existingCategory) {
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
        const parent = await tx.categories.findUnique({
          where: { id: parentId },
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
        await tx.categories.create({
          data: {
            user_id: userId,
            name,
            parent_id: parentId,
            level,
            iva,
            created_by: user.userId,
            category_translations: {
              createMany: {
                data:
                  translations?.map((translation) => ({
                    lang_code: translation.lang_code,
                    name: translation.name,
                  })) ?? [],
                skipDuplicates: true,
              },
            },
          },
        });

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

  async findAllUseSql(
    query: ICategoryQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    if (!ability.can(Action.Read, 'categories')) {
      throw new ForbiddenException(
        'You do not have permission to search categories',
      );
    }
    const permissionCondition = this.getReadListPermission(user);

    const { langCode, search, userId, fields, type, page, limit } = query;
    const parentId = query.parentId ? BigInt(query.parentId) : undefined;
    const iva = fields?.includes(CategorySelectField.IVA);
    const level = fields?.includes(CategorySelectField.LEVEL);
    const user_id = fields?.includes(CategorySelectField.USER_ID);
    const translations = fields?.includes(CategorySelectField.TRANSLATIONS);
    const relations = fields?.includes(CategorySelectField.RELATIONS);

    const params: string[] = [];
    let paramIndex = 1;

    const whereClauses: string[] = ['1=1'];

    if (permissionCondition) {
      params.push(permissionCondition);
      whereClauses.push(`c.user_id = $${paramIndex++}`);
    }

    if (search) {
      params.push(`%${search}%`);
      const searchIndex = paramIndex++;
      whereClauses.push(`
        (
          c.name_unaccent ILIKE $${searchIndex}
          OR EXISTS (
            SELECT 1 FROM category_translations ct
            WHERE ct.category_id = c.id
            AND ct.name_unaccent ILIKE $${searchIndex}
          )
        )
      `);
    }

    if (permissionCondition === undefined) {
      if (userId) {
        params.push(userId);
        whereClauses.push(`c.user_id = $${paramIndex++}`);
      } else if (type) {
        if (type === CategoryType.PRIVATE) {
          whereClauses.push(`c.user_id IS NOT NULL`);
        } else if (type === CategoryType.PUBLIC) {
          whereClauses.push(`c.user_id IS NULL`);
        }
      }
    }

    if (parentId != undefined) {
      params.push(parentId.toString());
      whereClauses.push(`c.parent_id = $${paramIndex++}`);
    }

    if (query.maxLevel != undefined) {
      params.push(query.maxLevel.toString());
      whereClauses.push(`c.level <= $${paramIndex++}`);
    }

    // 最终 where
    const whereSql = whereClauses.join(' AND ');

    const countParams = params.slice();
    const countSql = `SELECT COUNT(*) AS total FROM categories c WHERE ${whereSql}`;

    const offset = (page - 1) * limit;
    const limitParamIndex = paramIndex++;
    const offsetParamIndex = paramIndex++;
    params.push(limit + '', offset + '');

    if (translations && langCode) params.push(langCode);
    if (relations && userId) params.push(userId);

    const selectFields = [
      'c.id',
      'c.name',
      iva ? 'c.iva' : null,
      level ? 'c.level' : null,
      user_id ? 'c.user_id' : null,

      translations
        ? `(SELECT COALESCE(jsonb_agg(jsonb_build_object(
        'lang_code', ct.lang_code,
        'name', ct.name
      )), '[]'::jsonb)
      FROM category_translations ct
      WHERE ct.category_id = c.id
      ${langCode ? `AND ct.lang_code = $${paramIndex++}` : ''}) AS category_translations`
        : null,

      relations
        ? `
    (
      SELECT jsonb_build_object(
        'id', p.id,
        'name', p.name,
        ${iva ? `'iva', p.iva,` : ''}
        ${level ? `'level', p.level,` : ''}
        ${user_id ? `'user_id', p.user_id,` : ''}
        'parent',
        (
          SELECT jsonb_build_object(
            'id', pp.id,
            'name', pp.name
            ${iva ? `, 'iva', pp.iva` : ''}
            ${level ? `, 'level', pp.level` : ''}
            ${user_id ? `, 'user_id', pp.user_id` : ''}
          )
          FROM categories pp
          WHERE pp.id = p.parent_id
         )
      )
      FROM categories p
      WHERE p.id = c.parent_id
      ) AS parent
    `
        : null,

      // children
      relations
        ? `
    (
      SELECT COALESCE(jsonb_agg(jsonb_build_object(
        'id', ch.id,
        'name', ch.name
        ${iva ? `, 'iva', ch.iva` : ''}
        ${level ? `, 'level', ch.level` : ''}
        ${user_id ? `, 'user_id', ch.user_id` : ''}
      )), '[]'::jsonb)
      FROM categories ch
      WHERE ch.parent_id = c.id
      ${userId ? `AND ch.user_id = $${paramIndex++}` : ''}
    ) AS children
    `
        : null,
    ]
      .filter(Boolean)
      .join(', ');

    const sql = query.withChildrenCount
      ? `
    -- CTE + 分页 + 动态字段查询说明
    -- page_categories CTE
    -- 先筛选出符合 WHERE 条件的分类，并按分页 LIMIT/OFFSET 返回 id
    -- 为什么只取 id？：
    -- 避免对全表执行 translations / parent / children 子查询
    -- 分页前就执行全部子查询会导致大量计算（尤其是表大时）
    -- 如果把 translations / relations / parent / children 也放在 CTE 内：
    -- PostgreSQL 可能对 CTE 执行 MATERIALIZED 或非优化 inline
    
    WITH page_categories AS (
      SELECT id
      FROM categories c
      WHERE ${whereSql}
      ORDER BY c.name
      LIMIT $${limitParamIndex} 
      OFFSET $${offsetParamIndex}
    ),
    children_counts AS (
      SELECT c.parent_id AS root_id, COUNT(*) AS children_count
      FROM categories c
      WHERE c.parent_id IN (SELECT id FROM page_categories)
      GROUP BY c.parent_id
    )
    SELECT ${selectFields}, COALESCE(cc.children_count, 0) AS children_count
    FROM categories c
    JOIN page_categories pc ON pc.id = c.id
    LEFT JOIN children_counts cc ON cc.root_id = c.id
    ORDER BY c.name;
  `
      : `
    SELECT ${selectFields}
    FROM categories c
    WHERE ${whereSql}
    ORDER BY c.name
    LIMIT $${limitParamIndex} 
    OFFSET $${offsetParamIndex}
    `;

    const [categories, count] = await Promise.all([
      this.prisma.$queryRawUnsafe<ICategoryResponse[]>(sql, ...params),
      this.prisma.$queryRawUnsafe<[{ total: number }]>(
        countSql,
        ...countParams,
      ),
    ]);

    const total = Number(count[0].total);

    const result: PaginatedDataWithT<ICategoryResponse> = {
      items: categories,
      pagination: {
        total: total,
        page: page,
        limit: limit,
      },
    };

    return result;
  }

  async getCategoryForUpdate(id: string, ability: AppAbility) {
    const category = await this.prisma.categories.findUnique({
      where: { id: BigInt(id) },
      select: {
        user_id: true,
        name: true,
        iva: true,
        category_translations: {
          select: {
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
      translations: category.category_translations,
    };
  }

  async update(
    updateCategoryDto: IUpdateCategoryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const id = BigInt(updateCategoryDto.id);
    const categoryUserId = await this.prisma.categories.findUnique({
      where: { id },
      select: { user_id: true },
    });

    if (
      !ability.can(
        Action.Update,
        subject('categories', {
          user_id: categoryUserId?.user_id ?? undefined,
        }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to update categories',
      );
    }
    const { name, iva, translations, translationsToDelete } = updateCategoryDto;

    return this.prisma.$transaction(async (tx) => {
      await tx.categories.update({
        where: { id },
        data: {
          name,
          iva: iva ?? null,
          updated_at: new Date(),
          updated_by: user.userId,
        },
      });

      if (translationsToDelete && translationsToDelete.length > 0) {
        await tx.category_translations.deleteMany({
          where: {
            category_id: id,
            lang_code: {
              in: translationsToDelete,
            },
          },
        });
      }

      if (translations && translations.length > 0) {
        await tx.$queryRaw`
            INSERT INTO category_translations 
            (category_id,
            lang_code,
            name,
            updated_by)
            SELECT ${id} AS category_id,
                   x.lang_code,
                   x.name,
                   ${user.userId}::uuid AS updated_by
            FROM unnest(
                         ARRAY [
                             ${Prisma.join(
                               translations.map(
                                 (t) => Prisma.sql`${t.lang_code}`,
                               ),
                               ',',
                             )}
                             ],
                         ARRAY [
                             ${Prisma.join(
                               translations.map((t) => Prisma.sql`${t.name}`),
                               ',',
                             )}
                             ]
                 ) AS x(lang_code, name)
            ON CONFLICT (category_id, lang_code)
                DO UPDATE SET 
                name       = EXCLUDED.name,
                updated_at = NOW(),
                updated_by = ${user.userId}::uuid;
        `;
      }
    });
  }

  async remove(id: bigint, ability: AppAbility) {
    const categoryUserId = await this.prisma.categories.findUnique({
      where: { id },
      select: { user_id: true },
    });

    if (
      !ability.can(
        Action.Delete,
        subject('categories', {
          user_id: categoryUserId?.user_id ?? undefined,
        }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to delete categories',
      );
    }

    await this.prisma.categories.delete({ where: { id } });
  }
}
