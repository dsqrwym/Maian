import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { CreateCategoryDto } from '../dto/create-category.dto';
import { UpdateCategoryDto } from '../dto/update-category.dto';
import { PrismaService } from '../../prisma/prisma.service';
import { Logger } from 'nestjs-pino';
import { AppAbility } from '../../casl/casl-types';
import { Action } from '../../casl/actions';
import { subject } from '@casl/ability';
import { FindCategoryDto } from '../dto/find-category.dto';
import { accessibleBy } from '@casl/prisma';
import { Prisma } from '@prisma/client';
import { ToPaginated } from '../../common/types/response.type';
import { UserPayload } from '../../auth/auth.types';
import { CategorySelectField, CategoryType } from '../category.enums';

@Injectable()
export class CategoryService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly logger: Logger,
  ) {}

  async create(
    createCategoryDto: CreateCategoryDto,
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

  async search(query: FindCategoryDto, ability: AppAbility) {
    if (!ability.can(Action.Read, 'categories')) {
      throw new ForbiddenException(
        'You do not have permission to search categories',
      );
    }

    const { langCode, search, userId, fields, type, page, limit } = query;
    const parentId = query.parentId ? BigInt(query.parentId) : undefined;
    const iva = fields?.includes(CategorySelectField.IVA);
    const level = fields?.includes(CategorySelectField.LEVEL);
    const user_id = fields?.includes(CategorySelectField.USER_ID);
    const translations = fields?.includes(CategorySelectField.TRANSLATIONS);
    const relations = fields?.includes(CategorySelectField.RELATIONS);

    const permissionCondition: Prisma.categoriesWhereInput = accessibleBy(
      ability,
      Action.Read,
    ).categories;

    this.logger.log('permissionCondition', permissionCondition);

    const select: Prisma.categoriesSelect = {
      id: true,
      name: true,
      user_id: true,
      created_at: true,
      ...(iva && { iva: true }),
      ...(level && { level: true }),
      ...(user_id && { user_id: true }),
      ...(translations && {
        category_translations: {
          select: {
            name: true,
            lang_code: true,
          },
          where: { ...(langCode && { lang_code: langCode }) },
        },
      }),
      ...(relations && {
        parent: {
          select: {
            id: true,
            name: true,
            user_id: true,
            parent: true,
            ...(iva && { iva: true }),
            ...(level && { level: true }),
          },
        },
        children: {
          select: {
            id: true,
            name: true,
            user_id: true,
            ...(iva && { iva: true }),
            ...(level && { level: true }),
          },
          where: { ...(userId && { user_id: userId }) },
        },
      }),
    };

    const andClauses: Prisma.categoriesWhereInput[] = [permissionCondition];

    if (search) {
      andClauses.push({
        OR: [
          { name_unaccent: { contains: search, mode: 'insensitive' } },
          {
            category_translations: {
              some: {
                name_unaccent: { contains: search, mode: 'insensitive' },
                ...(langCode && { lang_code: langCode }),
              },
            },
          },
        ],
      });
    }

    if (!permissionCondition.user_id) {
      if (userId) {
        andClauses.push({ user_id: userId });
      } else if (type) {
        if (type === CategoryType.PRIVATE) {
          andClauses.push({ user_id: { not: null } });
        } else if (type === CategoryType.PUBLIC) {
          andClauses.push({ user_id: null });
        }
      }
    }

    if (parentId) {
      andClauses.push({ parent_id: parentId });
    }

    if (query.maxLevel) {
      andClauses.push({ level: { lt: query.maxLevel } });
    }

    // 最终 where
    const where: Prisma.categoriesWhereInput = {
      AND: andClauses,
    };

    // 查询
    const [categories, total] = await Promise.all([
      this.prisma.categories.findMany({
        where,
        select,
        orderBy: { name: 'asc' },
        skip: (page - 1) * limit,
        take: limit,
      }),
      this.prisma.categories.count({ where }),
    ]);

    const result: ToPaginated = {
      items: categories,
      meta: {
        total: total,
        page: page,
        limit: limit,
      },
    };

    return result;
  }

  async update(
    updateCategoryDto: UpdateCategoryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const categoryUserId = await this.prisma.categories.findUnique({
      where: { id: updateCategoryDto.id },
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
    const { id, name, iva, translations, translationsToDelete } =
      updateCategoryDto;

    return this.prisma.$transaction(async (tx) => {
      await tx.categories.update({
        where: { id },
        data: {
          ...(name && { name }),
          ...(iva && { iva }),
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
          INSERT INTO category_translations (category_id, lang_code, name)
          VALUES ${Prisma.join(
            translations.map(
              (t) => Prisma.sql`(${id}, ${t.lang_code}, ${t.name})`,
            ),
          )} ON CONFLICT (category_id, lang_code)
          DO
          UPDATE SET name = EXCLUDED.name,
            updated_at = NOW(),
            updated_by = ${user.userId}
          ;
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
