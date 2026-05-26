import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ICreateProductDto } from './../dto/create-product.dto.js';
import { IUpdateProductDto } from './../dto/update-product.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { subject } from '@casl/ability';
import { UserPayload } from '#/auth/auth.types.js';
import { computePrice } from '#/utils/calculate/computePrice.js';
import { ProductStatus } from '#/generated/drizzle/enums.js';
import { PinoLogger } from 'nestjs-pino';
import { ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import {
  DOC_MIME_TYPES,
  IMAGE_MIME_TYPES,
  VIDEO_MIME_TYPES,
} from '#/config/fastify-multipart.config.js';
import { IProductFileDto } from './../dto/product-file.dto.js';
import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  files,
  product_categories,
  product_translations,
  products,
  products_files,
  user_uploads,
  variant_products,
} from '#/generated/drizzle/schema.js';
import { and, eq, exists, inArray, sql } from 'drizzle-orm';
import { SQL_TEMP_TABLE } from '#/drizzle/drizzle.constants.js';
import { LowStockAlertService } from '#/mail/low-stock-alert.service.js';
import type { LowStockAlertEmailItem } from '#/mail/mail.types.js';
import { restoreFilesFromCleanup } from '#/utils/db/file.db.utils.js';

@Injectable()
export class WriteProductsService {
  private readonly MAX_VARIANTS_PRODUCT: number;
  private readonly MAX_SUB_CATEGORIES_PRODUCT: number;
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
    private readonly configService: ConfigService,
    private readonly lowStockAlertService: LowStockAlertService,
  ) {
    this.MAX_VARIANTS_PRODUCT = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_VARIANTS, 50),
    );
    this.MAX_SUB_CATEGORIES_PRODUCT = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_SUB_CATEGORIES, 10),
    );
    this.logger.setContext(WriteProductsService.name);
  }

  async create(
    createProductDto: ICreateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const {
      user_id,
      primary_category_id,
      sub_category_ids,
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

    if (
      sub_category_ids &&
      sub_category_ids.length > this.MAX_SUB_CATEGORIES_PRODUCT
    ) {
      throw new BadRequestException(
        `You can only create up to ${this.MAX_SUB_CATEGORIES_PRODUCT} sub categories for a product`,
      );
    }

    await this.drizzle.db.transaction(async (tx) => {
      await this.validateAndCheckFiles(files, user, user_id, tx);

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

      // dto 验证保证不会重复
      const categoryValues = new Array<{
        product_id: bigint;
        category_id: bigint;
        is_primary: boolean;
      }>(1 + (sub_category_ids?.length ?? 0));
      categoryValues[0] = {
        product_id: createdProduct.id,
        category_id: BigInt(primary_category_id),
        is_primary: true,
      };
      for (let i = 0; i < (sub_category_ids?.length ?? 0); i++) {
        categoryValues[i + 1] = {
          product_id: createdProduct.id,
          category_id: BigInt(sub_category_ids![i]),
          is_primary: false,
        };
      }

      await tx.insert(product_categories).values(categoryValues);

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
      sub_category_ids,
      ...mainProductData
    } = updateProductDto;

    const result = await this.drizzle.db.transaction(async (tx) => {
      const lowStockAlerts: LowStockAlertEmailItem[] = [];
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
          name: true,
          product_code: true,
          status: true,
        },
        with: {
          variant_products: {
            columns: {
              id: true,
              product_code: true,
              available_stock: true,
              low_stock_threshold: true,
              status: true,
            },
          },
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

      let primaryCategoryId: bigint | undefined = undefined;

      if (primary_category_id) {
        await tx
          .delete(product_categories)
          .where(
            and(
              eq(product_categories.product_id, productId),
              eq(product_categories.category_id, BigInt(primary_category_id)),
              eq(product_categories.is_primary, false),
            ),
          );
        const [result] = await tx
          .update(product_categories)
          .set({ category_id: BigInt(primary_category_id) })
          .where(
            and(
              eq(product_categories.product_id, productId),
              eq(product_categories.is_primary, true),
            ),
          )
          .returning({ category_id: product_categories.category_id });
        primaryCategoryId = result?.category_id;
      }

      const needUpdateSubCategory =
        sub_category_ids !== undefined && sub_category_ids !== null;
      // 全量更新不需要获取当前数量
      if (
        needUpdateSubCategory &&
        sub_category_ids.length > this.MAX_SUB_CATEGORIES_PRODUCT
      ) {
        throw new BadRequestException(
          `A product can have at most ${this.MAX_SUB_CATEGORIES_PRODUCT} sub categories`,
        );
      }

      if (!primaryCategoryId && needUpdateSubCategory) {
        const [currentCategoryId] = await tx
          .select({ category_id: product_categories.category_id })
          .from(product_categories)
          .where(
            and(
              eq(product_categories.product_id, productId),
              eq(product_categories.is_primary, true),
            ),
          );
        if (!currentCategoryId?.category_id) {
          this.logger.error('Primary category not found', { productId });
          throw new BadRequestException('Product must have a primary category');
        }
        primaryCategoryId = currentCategoryId.category_id;
      }

      if (needUpdateSubCategory) {
        const subCategories = sub_category_ids.map((id) => BigInt(id));
        if (subCategories.includes(primaryCategoryId ?? 0n)) {
          throw new BadRequestException(
            'Primary category cannot be a sub category',
          );
        }
        // 全量替换
        await tx
          .delete(product_categories)
          .where(
            and(
              eq(product_categories.product_id, productId),
              eq(product_categories.is_primary, false),
            ),
          );
        if (subCategories.length > 0) {
          await tx.insert(product_categories).values(
            subCategories.map((category_id) => ({
              product_id: productId,
              category_id,
              is_primary: false,
            })),
          );
        }
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

        const currentVariants = await tx
          .select({
            id: variant_products.id,
            product_code: variant_products.product_code,
            available_stock: variant_products.available_stock,
            low_stock_threshold: variant_products.low_stock_threshold,
            status: variant_products.status,
          })
          .from(variant_products)
          .where(
            and(
              eq(variant_products.product_id, productId),
              inArray(variant_products.id, updateIds),
            ),
          )
          .for('update');
        const currentVariantsById = new Map(
          currentVariants.map((variant) => [variant.id, variant]),
        );
        const productName = mainProductData.name ?? existingProduct.name;
        const productCode =
          mainProductData.product_code ?? existingProduct.product_code;
        const currentProductStatus =
          mainProductData.status ?? existingProduct.status;

        for (const variant of updateVariants) {
          const variantId = BigInt(variant.id);
          const currentVariant = currentVariantsById.get(variantId);
          if (!currentVariant) continue;

          let priceData: { price: string; price_iva: string } | undefined =
            undefined;
          // 重新计算价格逻辑
          const iva = mainProductData.iva ?? existingProduct.iva;
          if (variant.price) {
            priceData = computePrice(variant.price, undefined, iva);
          } else if (variant.price_iva) {
            priceData = computePrice(undefined, variant.price_iva, iva);
          }

          const [updatedVariant] = await tx
            .update(variant_products)
            .set({
              type_sale: variant.type_sale,
              sort: variant.sort,
              product_code: variant.product_code,
              available_stock:
                variant.available_stock_delta === undefined
                  ? undefined
                  : sql`${variant_products.available_stock} + ${variant.available_stock_delta}`,
              sale_unit_qty: variant.sale_unit_qty,
              min_order_qty: variant.min_order_qty,
              low_stock_threshold: variant.low_stock_threshold,
              ...priceData, // 展开计算后的价格字段
              status: variant.status,
              updated_by: user.userId,
              updated_at: now,
            })
            .where(
              and(
                eq(variant_products.id, variantId),
                variant.available_stock_delta === undefined
                  ? undefined
                  : sql`${variant_products.available_stock} + ${variant.available_stock_delta} >= 0`,
              ),
            )
            .returning({
              id: variant_products.id,
              product_code: variant_products.product_code,
              available_stock: variant_products.available_stock,
              low_stock_threshold: variant_products.low_stock_threshold,
              status: variant_products.status,
            });

          if (!updatedVariant) {
            throw new BadRequestException('STOCK_CANNOT_BE_NEGATIVE');
          }

          const previousStatus =
            existingProduct.status === ProductStatus.ACTIVE &&
            currentVariant.status === ProductStatus.ACTIVE
              ? ProductStatus.ACTIVE
              : ProductStatus.INACTIVE;
          const nextStatus =
            currentProductStatus === ProductStatus.ACTIVE &&
            updatedVariant.status === ProductStatus.ACTIVE
              ? ProductStatus.ACTIVE
              : ProductStatus.INACTIVE;

          if (
            this.lowStockAlertService.shouldTriggerLowStockAlert({
              previousAvailableStock: currentVariant.available_stock,
              previousLowStockThreshold: currentVariant.low_stock_threshold,
              currentAvailableStock: updatedVariant.available_stock,
              currentLowStockThreshold: updatedVariant.low_stock_threshold,
              previousStatus,
              currentStatus: nextStatus,
            })
          ) {
            lowStockAlerts.push({
              variantProductId: updatedVariant.id.toString(),
              productName,
              productCode,
              variantProductCode: updatedVariant.product_code,
              availableStock: updatedVariant.available_stock,
              lowStockThreshold: updatedVariant.low_stock_threshold,
            });
          }
        }
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
        .from(SQL_TEMP_TABLE);

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

      return {
        wholesalerId: existingProduct.user_id,
        lowStockAlerts,
      };
    });

    this.dispatchLowStockAlertTask(
      result.wholesalerId,
      result.lowStockAlerts,
      id,
    );
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

  private dispatchLowStockAlertTask(
    wholesalerId: string,
    items: LowStockAlertEmailItem[],
    productId: string,
  ) {
    if (items.length === 0) return;

    void this.lowStockAlertService
      .notifyLowStockAlerts(wholesalerId, items)
      .catch((err: unknown) => {
        this.logger.error(
          { err, productId, wholesalerId, itemCount: items.length },
          'Low stock alert background task failed',
        );
      });
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
    await restoreFilesFromCleanup(uniqueFileIds, finalDb);

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
}
