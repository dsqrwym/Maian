import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ICreateProductDto } from './dto/create-product.dto.js';
import { IUpdateProductDto } from './dto/update-product.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { subject } from '@casl/ability';
import { UserPayload } from '#/auth/auth.types.js';
import { computePrice } from '#/utils/calculate/computePrice.js';
import { IProductListQueryDto } from './dto/product-list-query.dto.js';
import { ProductStatus, UserRole } from '#/generated/drizzle/enums.js';
import { PinoLogger } from 'nestjs-pino';
import { ProductListSelectField, ProductSortField } from './product.enums.js';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import {
  DOC_MIME_TYPES,
  IMAGE_MIME_TYPES,
  VIDEO_MIME_TYPES,
} from '#/config/fastify-multipart.config.js';
import { IProductFileDto } from './dto/product-file.dto.js';
import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  categories,
  category_translations,
  files,
  product_categories,
  product_translations,
  products,
  products_files,
  user_uploads,
  variant_products,
} from '#/generated/drizzle/schema.js';
import {
  and,
  asc,
  count,
  eq,
  exists,
  ilike,
  inArray,
  like,
  or,
  SQL,
  sql,
} from 'drizzle-orm';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import { IProductResponse } from './dto/product-response.js';

@Injectable()
export class ProductsService {
  private readonly MAX_VARIANTS_PRODUCT: number;
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
    private readonly configService: ConfigService,
  ) {
    this.MAX_VARIANTS_PRODUCT = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_VARIANTS, 50),
    );
    this.MAX_SEARCH_TERMS = Number(
      this.configService.get<number>(ENV.MAX_SEARCH_TERMS, 10),
    );
    this.logger.setContext(ProductsService.name);
  }

  async create(
    createProductDto: ICreateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const {
      user_id,
      primary_category_id,
      product_code,
      description,
      iva,
      name,
      title,
      variants,
      translations,
      files,
      status,
    } = createProductDto;

    if (!ability.can(Action.Create, subject('products', { user_id }))) {
      throw new ForbiddenException(
        'You are not allowed to create products that do not belong to you or your company',
      );
    }

    if (variants.length > Number(this.MAX_VARIANTS_PRODUCT)) {
      throw new BadRequestException(
        `You can only create up to ${this.MAX_VARIANTS_PRODUCT} variants for a product`,
      );
    }

    await this.validateAndCheckFiles(files, user, user_id);

    await this.drizzle.db.transaction(async (tx) => {
      const [createdProduct] = await tx
        .insert(products)
        .values({
          iva,
          name,
          title,
          user_id,
          status,
          description,
          product_code,
          created_by: user.userId,
        })
        .returning({ id: products.id });

      await tx.insert(product_categories).values({
        product_id: createdProduct.id,
        category_id: BigInt(primary_category_id),
        is_primary: true,
      });

      // dto 验证已经保证了 price 和 price_iva 的有效性
      await tx.insert(variant_products).values(
        variants.map((variant) => ({
          product_id: createdProduct.id,
          type_sale: variant.type_sale,
          sort: variant.sort,
          ...computePrice(variant.price, variant.price_iva, iva),
          product_code: variant.product_code,
          min_order_qty: variant.min_order_qty,
          sale_unit_qty: variant.sale_unit_qty,
          available_stock: variant.available_stock,
          low_stock_threshold: variant.low_stock_threshold,
          created_by: user.userId,
          status: variant.status,
        })),
      );

      if (translations && translations.length > 0) {
        await tx.insert(product_translations).values(
          translations.map((translation) => ({
            product_id: createdProduct.id,
            lang_code: translation.lang_code,
            name: translation.name,
            title: translation.title,
            description: translation.description,
          })),
        );
      }

      if (files && files.length > 0) {
        await tx.insert(products_files).values(
          files.map((file) => ({
            product_id: createdProduct.id,
            file_id: BigInt(file.file_id),
            sort: file.sort,
          })),
        );
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

  getSortFieldDrizzle(sortBy?: ProductSortField) {
    switch (sortBy) {
      case ProductSortField.NAME:
        return products.name;
      case ProductSortField.TITLE:
        return products.title;
      case ProductSortField.CATEGORY:
        return sql.raw(`("mainCategoryLateral"."main_category"->>'name')`);
      case ProductSortField.PRODUCT_CODE:
        return products.product_code;
      case ProductSortField.MIN_ORDER_QTY:
        return sql.raw(`"variantAggregates"."min_order_qty"`);
      case ProductSortField.AVAILABLE_STOCK:
        return sql.raw(`"variantAggregates"."total_stock"`);
      case ProductSortField.PRICE_IVA:
        return sql.raw(`"variantAggregates"."min_price_iva"`);
      case ProductSortField.PRICE:
        return sql.raw(`"variantAggregates"."min_price"`);
      default:
        return undefined;
    }
  }

  async findAllUseSqlD(
    query: IProductListQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ): Promise<{
    items: IProductResponse[];
    pagination: { total: number; page: number; limit: number };
  }> {
    if (!ability.can(Action.Read, 'products')) {
      throw new ForbiddenException(
        'You do not have permission to read products',
      );
    }
    const permissionCondition = this.getReadListPermission(user);

    const { search, langCode, wholesaler_id, status } = query;
    const category_id = query.category_id
      ? BigInt(query.category_id)
      : undefined;
    const fields = query.fields;
    const iva = fields?.includes(ProductListSelectField.IVA);
    const selectedStatus = fields?.includes(ProductListSelectField.STATUS);
    const user_id = fields?.includes(ProductListSelectField.USER_ID);
    const category = fields?.includes(ProductListSelectField.CATEGORY);

    const { page, limit, sort_by, sort_order } = query;
    const offset = (page - 1) * limit;
    const sortField = this.getSortFieldDrizzle(sort_by);

    // LEFT JOIN LATERAL 关联主查询，查询 variants
    const variantAggregates = this.drizzle.db
      .select({
        product_id: variant_products.product_id,
        // 聚合总数
        total_stock:
          sql<number>`SUM(${variant_products.available_stock} * ${variant_products.sale_unit_qty})`.as(
            'total_stock',
          ),
        min_order_qty:
          sql<number>`MIN(${variant_products.min_order_qty} * ${variant_products.sale_unit_qty})`.as(
            'min_order_qty',
          ),
        // 用 array_agg（聚合为数组） + order by 基于 price_iva 升序排列，取首个变体的价格对，确保来源行一致
        min_price_iva:
          sql<string>`(ARRAY_AGG(${variant_products.price_iva} ORDER BY ${variant_products.price_iva} ASC))[1]::text`.as(
            'min_price_iva',
          ),
        min_price:
          sql<string>`(ARRAY_AGG(${variant_products.price} ORDER BY ${variant_products.price_iva} ASC))[1]::text`.as(
            'min_price',
          ),
      })
      .from(variant_products)
      // WHERE 条件引用主表 ID 实现 Lateral 关联
      .where(eq(variant_products.product_id, products.id))
      // 必须要用 group by product_id 满足聚合查询对非聚合列的分组约束
      .groupBy(variant_products.product_id)
      .as('variantAggregates');

    // LEFT JOIN LATERAL 关联主查询，查询主图
    const mainImgLateral = this.drizzle.db
      .select({
        main_image: sql<{ id: string; mime_type: string }>`
      jsonb_build_object(
        'id', ${files.id},
        'mime_type', ${files.mime_type}
      )
    `.as('main_image'),
      })
      .from(products_files)
      .innerJoin(files, eq(files.id, products_files.file_id))
      .where(
        and(
          eq(products_files.product_id, products.id),
          like(files.mime_type, 'image/%'),
        ),
      )
      .orderBy(asc(products_files.sort))
      .limit(1)
      .as('mainImgLateral');

    // LEFT JOIN LATERAL 关联主查询，查询主分类
    const mainCategoryLateral = this.drizzle.db
      .select({
        main_category: sql<{
          id: string;
          name: string;
          category_translations: { lang_code: string; name: string }[];
        }>`
      jsonb_build_object(
        'id', ${categories.id},
        'name', ${categories.name},
        'category_translations', (
            SELECT COALESCE(jsonb_agg(jsonb_build_object(
              'lang_code', ${category_translations.lang_code},
              'name', ${category_translations.name}
            )), '[]'::jsonb)
            FROM ${category_translations}
            WHERE ${category_translations.category_id} = ${categories.id}
            ${langCode ? sql`AND ${category_translations.lang_code} = ${langCode}` : sql.empty()}
          )
      )`.as('main_category'),
      })
      .from(product_categories)
      .innerJoin(categories, eq(categories.id, product_categories.category_id))
      .where(
        and(
          eq(product_categories.product_id, products.id),
          eq(product_categories.is_primary, true),
        ),
      )
      .limit(1)
      .as('mainCategoryLateral');

    // LEFT JOIN LATERAL 关联主查询，查询翻译
    const translationsLateral = this.drizzle.db
      .select({
        product_translations: sql<
          { lang_code: string; name: string | null; title: string | null }[]
        >`
          COALESCE(
            jsonb_agg(
              jsonb_build_object(
                'lang_code', ${product_translations.lang_code},
                'name', ${product_translations.name},
                'title', ${product_translations.title}
              )
            ),
            '[]'::jsonb
          )`.as('product_translations'),
      })
      .from(product_translations)
      .where(eq(product_translations.product_id, products.id))
      .as('productTranslationsLateral');

    let productQuery = this.drizzle.db
      .select({
        id: products.id,
        name: products.name,
        title: products.title,
        product_code: products.product_code,
        min_price_iva: variantAggregates.min_price_iva,
        min_price: variantAggregates.min_price,
        total_stock: variantAggregates.total_stock,
        min_order_qty: variantAggregates.min_order_qty,
        main_image: mainImgLateral.main_image,
        ...(category && { main_category: mainCategoryLateral.main_category }),
        ...(iva && { iva: products.iva }),
        ...(selectedStatus && { status: products.status }),
        ...(user_id && { user_id: products.user_id }),
        product_translations: translationsLateral.product_translations,
      })
      .from(products)
      .leftJoinLateral(variantAggregates, sql`TRUE`)
      .leftJoinLateral(mainImgLateral, sql`TRUE`)
      .leftJoinLateral(mainCategoryLateral, sql`TRUE`)
      .leftJoinLateral(translationsLateral, sql`TRUE`)
      .$dynamic();

    // 构建 WHERE 条件
    const whereConditions: (SQL | undefined)[] = [];

    if (langCode) {
      whereConditions.push(eq(product_translations.lang_code, langCode));
    }

    if (permissionCondition) {
      whereConditions.push(eq(products.user_id, permissionCondition));
    }

    if (search) {
      // 精确匹配跨字段关键词
      const searchTerms = escapeLike(toUnaccent(search))
        .split(/\s+/)
        .filter((s) => s.length > 0)
        .slice(0, this.MAX_SEARCH_TERMS);
      searchTerms.forEach((keyWord) => {
        const likeSearch = `%${keyWord}%`;
        whereConditions.push(
          or(
            ilike(products.name_unaccent, likeSearch),
            ilike(products.title_unaccent, likeSearch),
            ilike(products.product_code, likeSearch),
            exists(
              this.drizzle.db
                .select({ one: sql<number>`1` })
                .from(variant_products)
                .where(
                  and(
                    eq(products.id, variant_products.product_id),
                    ilike(variant_products.product_code, likeSearch),
                  ),
                ),
            ),
            exists(
              this.drizzle.db
                .select({ one: sql<number>`1` })
                .from(product_categories)
                .innerJoin(
                  categories,
                  eq(categories.id, product_categories.category_id),
                )
                .leftJoin(
                  category_translations,
                  eq(categories.id, category_translations.category_id),
                )
                .where(
                  and(
                    eq(product_categories.product_id, products.id),
                    ilike(category_translations.name_unaccent, likeSearch),
                  ),
                ),
            ),
            exists(
              this.drizzle.db
                .select({ one: sql<number>`1` })
                .from(product_translations)
                .where(
                  and(
                    eq(product_translations.product_id, products.id),
                    or(
                      ilike(product_translations.name_unaccent, likeSearch),
                      ilike(product_translations.title_unaccent, likeSearch),
                    ),
                  ),
                ),
            ),
          ),
        );
      });
    }

    if (category_id && category_id !== 0n) {
      whereConditions.push(
        exists(
          this.drizzle.db
            .select({ one: sql<number>`1` })
            .from(product_categories)
            .where(
              and(
                eq(product_categories.product_id, products.id),
                eq(product_categories.category_id, category_id),
              ),
            ),
        ),
      );
    }

    if (!permissionCondition && wholesaler_id) {
      whereConditions.push(eq(products.user_id, wholesaler_id));
    }

    if (status) {
      whereConditions.push(eq(products.status, status));
    }

    productQuery = productQuery
      .where(and(...whereConditions))
      .limit(limit)
      .offset(offset);

    if (sortField) {
      productQuery = productQuery.orderBy(
        sql`${sortField} ${sql.raw(sort_order)}`,
      );
    }

    const countQuery = this.drizzle.db
      .select({ count: count() })
      .from(products)
      .where(and(...whereConditions));

    const [items, [countResult]] = await Promise.all([
      productQuery,
      countQuery,
    ]);
    const total: number = countResult?.count ?? 0;
    return {
      items,
      pagination: { total, page, limit },
    };
  }

  async update(
    id: string,
    updateProductDto: IUpdateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const productId = BigInt(id);

    // 解构 DTO
    const {
      createVariants,
      updateVariants,
      variantsToDelete,
      translations,
      translationsToDelete,
      files,
      primary_category_id,
      ...mainProductData
    } = updateProductDto;

    await this.drizzle.db.transaction(async (tx) => {
      // 悲观锁：锁定产品行，保证后续 读取变体数量 → 检查 → 插入/删除，这一系列操作对于同一个产品是串行化的，避免并发时突破变体上限。
      await tx
        .select({ id: products.id })
        .from(products)
        .where(eq(products.id, productId))
        .for('update'); // 显式行锁，直到事务结束才释放

      const existingProduct = await tx.query.products.findFirst({
        columns: {
          user_id: true,
          iva: true,
        },
        with: {
          variant_products: { columns: { id: true } },
          products_files: {
            with: { file: { columns: { mime_type: true } } },
          },
          product_translations: { columns: { lang_code: true } },
        },
        where: eq(products.id, productId),
      });

      if (!existingProduct) {
        throw new NotFoundException('Product not found');
      }

      if (
        !ability.can(
          Action.Update,
          subject('products', { user_id: existingProduct.user_id }),
        )
      ) {
        throw new ForbiddenException(
          'You are not allowed to update this product',
        );
      }
      const currentVariantIds: bigint[] = existingProduct.variant_products.map(
        (v) => v.id,
      );

      await this.validateAndCheckFiles(
        files,
        user,
        existingProduct.user_id,
        tx,
      );

      // 计算操作后的最终变体数量
      const finalCount =
        existingProduct.variant_products.length -
        (variantsToDelete?.length ?? 0) +
        (createVariants?.length ?? 0);

      if (finalCount > this.MAX_VARIANTS_PRODUCT) {
        throw new BadRequestException(
          `You can only update up to ${this.MAX_VARIANTS_PRODUCT} variants`,
        );
      }

      // 开启正式更新
      const now = new Date().toISOString();
      // 更新主表并同时校验存在性，更新成功且持有事务会保证在事务执行期间，级联删除会被阻塞，直到事务完成或回滚
      // 已存在悲观锁所以乐观锁 where 条件没有必要了
      await tx
        .update(products)
        .set({
          ...mainProductData,
          version: sql`${products.version} + 1`,
          updated_by: user.userId,
          updated_at: now,
        })
        .where(eq(products.id, productId));

      if (primary_category_id) {
        await tx
          .update(product_categories)
          .set({ category_id: BigInt(primary_category_id) })
          .where(
            and(
              eq(product_categories.product_id, productId),
              eq(product_categories.is_primary, true),
            ),
          );
      }

      // 创建变体
      if (createVariants && createVariants.length > 0) {
        const createData = createVariants.map((variant) => ({
          product_id: productId,
          created_by: user.userId,
          type_sale: variant.type_sale,
          sort: variant.sort,
          ...computePrice(
            variant.price,
            variant.price_iva,
            mainProductData.iva ?? existingProduct.iva,
          ),
          product_code: variant.product_code,
          min_order_qty: variant.min_order_qty,
          sale_unit_qty: variant.sale_unit_qty,
          available_stock: variant.available_stock,
          low_stock_threshold: variant.low_stock_threshold,
          status: variant.status,
        }));

        await tx.insert(variant_products).values(createData);
      }

      // 更新变体
      if (updateVariants && updateVariants.length > 0) {
        const updateIds = updateVariants.map((v) => BigInt(v.id));

        const invalidIds = updateIds.filter(
          (id) => !currentVariantIds.includes(id),
        );
        if (invalidIds.length > 0) {
          throw new BadRequestException(
            `Variant IDs [${invalidIds.join(', ')}] do not belong to product ${id}`,
          );
        }

        await Promise.all(
          updateVariants.map((variant) => {
            const variantId = BigInt(variant.id);
            let priceData: { price: string; price_iva: string } | undefined =
              undefined;
            // 重新计算价格逻辑
            const iva = mainProductData.iva ?? existingProduct.iva;
            if (variant.price) {
              priceData = computePrice(variant.price, undefined, iva);
            } else if (variant.price_iva) {
              priceData = computePrice(undefined, variant.price_iva, iva);
            }

            return tx
              .update(variant_products)
              .set({
                type_sale: variant.type_sale,
                sort: variant.sort,
                product_code: variant.product_code,
                available_stock: variant.available_stock,
                sale_unit_qty: variant.sale_unit_qty,
                min_order_qty: variant.min_order_qty,
                low_stock_threshold: variant.low_stock_threshold,
                ...priceData, // 展开计算后的价格字段
                status: variant.status,
                updated_by: user.userId,
                updated_at: now,
              })
              .where(eq(variant_products.id, variantId));
          }),
        );
      }

      // 删除变体
      if (variantsToDelete && variantsToDelete.length > 0) {
        const toDeleteIds = variantsToDelete.map((id) => BigInt(id));
        const invalidIds = toDeleteIds.filter(
          (id) => !currentVariantIds.includes(id),
        );
        if (invalidIds.length > 0) {
          throw new BadRequestException(
            `Variant IDs [${invalidIds.join(', ')}] do not belong to product ${id}`,
          );
        }
        await tx
          .delete(variant_products)
          .where(
            and(
              eq(variant_products.product_id, productId),
              inArray(variant_products.id, toDeleteIds),
            ),
          );
      }

      // 验证至少一个 ACTIVE variant
      const activeVariant = await tx
        .select({
          exists: exists(
            tx
              .select({ one: sql<number>`1` })
              .from(variant_products)
              .where(
                and(
                  eq(variant_products.product_id, productId),
                  eq(variant_products.status, ProductStatus.ACTIVE),
                ),
              ),
          ),
        })
        .from(sql`(VALUES (1)) AS tmp`);

      if (!activeVariant[0]?.exists) {
        throw new BadRequestException(
          'At least one variant must remain ACTIVE',
        );
      }

      const currentTranslationsLangCodes =
        existingProduct.product_translations.map((v) => v.lang_code);

      if (translationsToDelete && translationsToDelete.length > 0) {
        const invalidLangCodes = translationsToDelete.filter(
          (langCodes) => !currentTranslationsLangCodes.includes(langCodes),
        );
        if (invalidLangCodes.length > 0) {
          throw new BadRequestException(
            `LangCodes [${invalidLangCodes.join(', ')}] do not belong to product ${id}`,
          );
        }
        await tx
          .delete(product_translations)
          .where(
            and(
              eq(product_translations.product_id, productId),
              inArray(product_translations.lang_code, translationsToDelete),
            ),
          );
      }

      // 对于翻译 数据量小 Upsert 是最优雅的 XD。
      if (translations && translations.length > 0) {
        await tx
          .insert(product_translations)
          .values(
            translations.map((t) => ({
              product_id: productId,
              lang_code: t.lang_code,
              name: t.name,
              title: t.title,
              description: t.description,
            })),
          )
          .onConflictDoUpdate({
            target: [
              product_translations.product_id,
              product_translations.lang_code,
            ],
            set: {
              name: sql`EXCLUDED.name`,
              title: sql`EXCLUDED.title`,
              description: sql`EXCLUDED.description`,
              updated_by: user.userId,
              updated_at: now,
            },
          });
      }

      if (files !== undefined) {
        // 策略：文件通常涉及排序。全量替换关系表是处理排序最简单的方法。
        await tx
          .delete(products_files)
          .where(eq(products_files.product_id, productId));

        if (files && files.length > 0) {
          const data = files.map((file) => ({
            product_id: productId,
            file_id: BigInt(file.file_id),
            sort: file.sort,
          }));
          await tx.insert(products_files).values(data);
        }
      }
    });
  }

  async remove(id: string, ability: AppAbility) {
    const idBigInt = BigInt(id);
    const [product] = await this.drizzle.db
      .select({ user_id: products.user_id })
      .from(products)
      .where(eq(products.id, idBigInt))
      .limit(1);
    if (!product) {
      throw new NotFoundException('Product not found');
    }
    if (
      !ability.can(
        Action.Delete,
        subject('products', { user_id: product.user_id }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to delete products',
      );
    }
    await this.drizzle.db.delete(products).where(eq(products.id, idBigInt));
  }

  private async validateAndCheckFiles(
    filesDTO: IProductFileDto[] | null | undefined,
    user: UserPayload,
    productOwnerId?: string,
    db?: DrizzleDb,
  ) {
    if (!filesDTO || filesDTO.length === 0) return;
    const finalDb = db ?? this.drizzle.db;

    const uniqueFileIds = [...new Set(filesDTO.map((f) => BigInt(f.file_id)))];
    const allowedOwnerIds = new Set<string>();
    // 当前用户总是应该有权访问自己上传的文件
    allowedOwnerIds.add(user.userId);

    // 如果当前用户是员工，他有权访问公司(批发商)的文件
    if (user.wholesalerId) {
      allowedOwnerIds.add(user.wholesalerId);
    }

    // 如果这是 Admin 在为别人创建/更新产品，他有权使用“那个产品归属者”所拥有的文件
    if (productOwnerId) {
      allowedOwnerIds.add(productOwnerId);
    }
    // 验证权限 + 获取 MIME 类型
    const validFiles = await finalDb
      .select({
        file_id: files.id,
        mime_type: files.mime_type,
      })
      .from(files)
      .innerJoin(user_uploads, eq(files.id, user_uploads.file_id))
      .where(
        and(
          inArray(files.id, uniqueFileIds),
          inArray(user_uploads.user_id, Array.from(allowedOwnerIds)),
        ),
      )
      .groupBy(files.id, files.mime_type);

    // uniqueFileIds 和 validFiles 是去重的, 如果数量不一致->说明有文件没找到归属权
    if (validFiles.length !== uniqueFileIds.length) {
      throw new ForbiddenException(
        'You do not have permission to use one or more provided files.',
      );
    }

    // 构建 file_id → mime_type 的映射（用于快速查找）
    const fileMimeMap = new Map<bigint, string>();
    for (const file of validFiles) {
      fileMimeMap.set(file.file_id, file.mime_type);
    }

    // 基于原始 filesDTO（包含重复）进行分类计数
    let imagesForProduct = 0;
    let videosForProduct = 0;
    let docsForProduct = 0;

    for (const item of filesDTO) {
      const mime = fileMimeMap.get(BigInt(item.file_id));
      // mime 一定存在，因为已经验证过所有 file_id 都有权限且存在
      if (mime && IMAGE_MIME_TYPES.has(mime)) imagesForProduct++;
      else if (mime && VIDEO_MIME_TYPES.has(mime)) videosForProduct++;
      else if (mime && DOC_MIME_TYPES.has(mime)) docsForProduct++;
    }

    const maxImageForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_IMAGES, 10),
    );
    const maxVideoForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_VIDEOS, 1),
    );
    const maxDocForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_DOCUMENTS, 5),
    );

    if (imagesForProduct > maxImageForProduct) {
      throw new BadRequestException(
        `You can only upload up to ${maxImageForProduct} images for a product`,
      );
    }
    if (videosForProduct > maxVideoForProduct) {
      throw new BadRequestException(
        `You can only upload up to ${maxVideoForProduct} videos for a product`,
      );
    }
    if (docsForProduct > maxDocForProduct) {
      throw new BadRequestException(
        `You can only upload up to ${maxDocForProduct} documents for a product`,
      );
    }
  }

  // 存在悲观锁所以 version 的放回不在必要
  async getForUpdate(id: string, ability: AppAbility) {
    if (!ability.can(Action.Read, 'products')) {
      throw new ForbiddenException(
        'You do not have permission to read products',
      );
    }
    const product = await this.drizzle.db.query.products.findFirst({
      where: eq(products.id, BigInt(id)),
      columns: {
        user_id: true,
        id: true,
        iva: true,
        name: true,
        title: true,
        status: true,
        description: true,
        product_code: true,
      },
      with: {
        products_files: {
          columns: { file_id: true, sort: true },
          with: { file: { columns: { mime_type: true } } },
        },
        product_categories: {
          columns: { is_primary: true },
          with: {
            category: {
              columns: {
                id: true,
                name: true,
                iva: true,
              },
              with: {
                category_translations: {
                  columns: { lang_code: true, name: true },
                },
              },
            },
          },
        },
        product_translations: {
          columns: {
            lang_code: true,
            name: true,
            title: true,
            description: true,
          },
        },
        variant_products: {
          columns: {
            id: true,
            sort: true,
            price: true,
            type_sale: true,
            price_iva: true,
            product_code: true,
            sale_unit_qty: true,
            min_order_qty: true,
            available_stock: true,
            low_stock_threshold: true,
            status: true,
          },
        },
      },
    });

    if (!product) {
      throw new NotFoundException('Product not found');
    }

    if (
      !ability.can(
        Action.Read,
        subject('products', { user_id: product.user_id }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to read that product',
      );
    }

    return {
      products_files: product.products_files.map((file) => ({
        file_id: file.file_id,
        sort: file.sort,
        mime_type: file.file.mime_type,
      })),
      name: product.name,
      id: product.id,
      title: product.title,
      description: product.description,
      iva: product.iva,
      product_code: product.product_code,
      status: product.status,
      product_categories: product.product_categories.map((category) => ({
        id: category.category.id,
        name: category.category.name,
        iva: category.category.iva,
        category_translations: category.category.category_translations,
        is_primary: category.is_primary,
      })),
      product_translations: product.product_translations,
      variant_products: product.variant_products,
    };
  }
}
