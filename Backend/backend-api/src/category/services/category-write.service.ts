import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ICreateCategoryDto } from '../dto/create-category.dto.js';
import { IUpdateCategoryDto } from '../dto/update-category.dto.js';
import { PinoLogger } from 'nestjs-pino';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { subject } from '@casl/ability';
import { UserPayload } from '#/auth/auth.types.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { and, eq, exists, inArray, isNull, sql } from 'drizzle-orm';
import {
  categories,
  category_translations,
  product_categories,
} from '#/generated/drizzle/schema.js';

@Injectable()
export class CategoryWriteService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(CategoryWriteService.name);
  }

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
          where: eq(categories.id, parentId),
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

        this.logger.debug(
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

    // 检查是否为某个产品依赖的 primary category
    const primaryCategorySubQuery = this.drizzle.db
      .select({ one: sql<number>`1` })
      .from(product_categories)
      .where(
        and(
          eq(product_categories.category_id, id),
          eq(product_categories.is_primary, true),
        ),
      );

    const [primaryRef] = (await this.drizzle.db
      .select({ exists: exists(primaryCategorySubQuery) })
      .from(sql`(VALUES (1)) AS tmp`)
      .execute()) as { exists: boolean }[];

    if (primaryRef?.exists) {
      throw new ConflictException(
        'Cannot delete category: it is the primary category of one or more products. Please change the primary category of those products first.',
      );
    }

    await this.drizzle.db.delete(categories).where(eq(categories.id, id));
  }
}
