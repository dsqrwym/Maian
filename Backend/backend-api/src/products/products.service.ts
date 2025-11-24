import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { AppAbility } from '../casl/casl-types';
import { Action } from '../casl/actions';
import { subject } from '@casl/ability';
import { PrismaService } from '../prisma/prisma.service';
import { UserPayload } from '../auth/auth.types';
import { computePrice } from '../utils/calculate/computePrice';
import { ProductListQueryDto } from './dto/product-list-query.dto';
import { accessibleBy } from '@casl/prisma';
import { Prisma, UserRole } from 'src/generated/prisma/client';
import { PinoLogger } from 'nestjs-pino';
import { ProductListSelectField, ProductSelectField } from './product.enums';
import { ToPaginated } from '../common/types/response.type';
import { ProductQueryDto } from './dto/product-query.dto';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../config/constants.config';
import {
  DOC_MIME_TYPES,
  IMAGE_MIME_TYPES,
  VIDEO_MIME_TYPES,
} from '../config/fastify-multipart.config';
import { ProductFileDto } from './dto/product-file.dto';

@Injectable()
export class ProductsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly logger: PinoLogger,
    private readonly configService: ConfigService,
  ) {
    this.logger.setContext(ProductsService.name);
  }
  async create(
    createProductDto: CreateProductDto,
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
    } = createProductDto;

    if (!ability.can(Action.Create, subject('products', { user_id }))) {
      throw new ForbiddenException(
        'You are not allowed to create products that do not belong to you or your company',
      );
    }

    const maxVariantsForProduct = this.configService.get<number>(
      ENV.PRODUCT_MAX_VARIANTS,
      50,
    );

    if (variants.length > Number(maxVariantsForProduct)) {
      throw new BadRequestException(
        `You can only create up to ${maxVariantsForProduct} variants for a product`,
      );
    }

    await this.validateAndCheckFiles(files, user, user_id);

    await this.prisma.products.create({
      data: {
        iva,
        name,
        title,
        user_id,
        description,
        product_code,
        created_by: user.userId,
        product_categories: {
          create: {
            category_id: BigInt(primary_category_id),
            is_primary: true,
          },
        },
        variant_products: {
          createMany: {
            data: variants.map((variant) => ({
              type_sale: variant.type_sale,
              sort: variant.sort,
              iva: variant.iva ?? iva,
              ...computePrice(
                variant.price,
                variant.price_iva,
                variant.iva ?? iva,
              ),
              product_code: variant.product_code,
              min_order_qty: variant.min_order_qty,
              sale_uni_qty: variant.sale_unit_qty,
              available_stock: variant.available_stock,
              low_stock_threshold: variant.low_stock_threshold,
              created_by: user.userId,
            })),
          },
        },
        ...(translations && {
          product_translations: {
            create: translations.map((translation) => ({
              lang_code: translation.lang_code,
              name: translation.name,
              description: translation.description,
            })),
          },
        }),
        ...(files && {
          product_files: {
            create: files.map((file) => ({
              file_id: file.file_id,
              sort: file.sort,
            })),
          },
        }),
      },
    });
  }

  async findAll(query: ProductListQueryDto, ability: AppAbility) {
    if (!ability.can(Action.Read, 'products')) {
      throw new ForbiddenException(
        'You do not have permission to read products',
      );
    }
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const permissionCondition: Prisma.productsWhereInput = accessibleBy(
      ability,
      Action.Read,
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    ).products;

    this.logger.info('permissionCondition', permissionCondition);

    const { search, langCode, category_id, wholesaler_id, status } = query;
    const fields = query.fields;
    const iva = fields?.includes(ProductListSelectField.IVA);
    const selectedStatus = fields?.includes(ProductListSelectField.STATUS);
    const user_id = fields?.includes(ProductListSelectField.USER_ID);
    const category = fields?.includes(ProductListSelectField.CATEGORY);

    const select: Prisma.productsSelect = {
      id: true,
      name: true,
      title: true,
      product_code: true,
      product_translations: {
        select: { lang_code: true, name: true },
        where: { ...(langCode && { lang_code: langCode }) },
      },
      products_files: {
        select: { files: { select: { id: true, mime_type: true } } },
        where: { files: { mime_type: { startsWith: 'image/' } } },
        orderBy: { sort: 'asc' },
        take: 1,
      },
      variant_products: {
        select: {
          price: true,
          price_iva: true,
          available_stock: true,
          min_order_qty: true,
          sale_unit_qty: true,
        },
        orderBy: { price_iva: 'asc' },
      },
      ...(iva && { iva }),
      ...(selectedStatus && { status: true }),
      ...(user_id && { user_id }),
      ...(category && { category }),
      ...(category && {
        product_categories: {
          select: {
            categories: {
              select: {
                id: true,
                name: true,
                category_translations: {
                  select: { lang_code: true, name: true },
                  where: { ...(langCode && { lang_code: langCode }) },
                },
              },
            },
          },
          where: { is_primary: true },
        },
      }),
    };

    const andClauses: Prisma.productsWhereInput[] = [permissionCondition];

    if (search) {
      andClauses.push({
        OR: [
          { name_unaccent: { contains: search, mode: 'insensitive' } },
          { title_unaccent: { contains: search, mode: 'insensitive' } },
          { product_code: { contains: search, mode: 'insensitive' } },
          {
            variant_products: {
              some: { product_code: { contains: search, mode: 'insensitive' } },
            },
          },
          {
            product_categories: {
              some: {
                categories: {
                  name: { contains: search, mode: 'insensitive' },
                  category_translations: {
                    some: {
                      name_unaccent: { contains: search, mode: 'insensitive' },
                    },
                  },
                },
              },
            },
          },
        ],
      });
    }

    if (category_id != undefined) {
      andClauses.push({
        product_categories: {
          some: {
            category_id: BigInt(category_id),
          },
        },
      });
    }

    if (!permissionCondition.user_id && wholesaler_id != undefined) {
      andClauses.push({
        user_id: wholesaler_id,
      });
    }

    if (status != undefined) {
      andClauses.push({ status });
    }

    const { page, limit } = query;
    const { sort_by, sort_order } = query;

    const orderBy: Prisma.productsOrderByWithRelationInput = {
      [sort_by]: sort_order,
    };

    const [products, count] = await Promise.all([
      this.prisma.products.findMany({
        select,
        where: { AND: andClauses },
        orderBy,
        skip: (page - 1) * limit,
        take: limit,
      }),
      this.prisma.products.count({
        where: { AND: andClauses },
      }),
    ]);

    const resultProduct = products.map((product) => {
      const { variant_products, ...productReset } = product;
      const { price, price_iva } = variant_products[0];
      const totalStock = variant_products.reduce(
        (total, variant) =>
          total + variant.available_stock * variant.sale_unit_qty,
        0,
      );
      const minOrderQty = Math.min(
        ...variant_products.map((v) => v.min_order_qty * v.sale_unit_qty),
      );
      return {
        ...productReset,
        minPrice: price,
        minPriceIva: price_iva,
        totalStock,
        minOrderQty,
      };
    });

    const result: ToPaginated = {
      items: resultProduct,
      meta: { total: count, page, limit },
    };

    return result;
  }

  async findOne(
    id: string,
    query: ProductQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    if (!ability.can(Action.Read, 'products')) {
      throw new ForbiddenException(
        'You do not have permission to read products',
      );
    }
    const notAllowedToSelect =
      user.userRole === UserRole.RETAILER ||
      user.userRole === UserRole.DELIVERY;
    const { fields, langCode } = query;
    let user_id: boolean | undefined;
    let status: boolean | undefined;
    let created_at: boolean | undefined;
    let updated_at: boolean | undefined;
    let created_by: boolean | undefined;
    let updated_by: boolean | undefined;
    let reserved_stock: boolean | undefined;
    let low_stock_threshold: boolean | undefined;
    if (!notAllowedToSelect) {
      user_id = fields?.includes(ProductSelectField.USER_ID);
      status = fields?.includes(ProductSelectField.STATUS);
      created_at = fields?.includes(ProductSelectField.CREATED_AT);
      updated_at = fields?.includes(ProductSelectField.UPDATED_AT);
      created_by = fields?.includes(ProductSelectField.CREATED_BY);
      updated_by = fields?.includes(ProductSelectField.UPDATED_BY);
      reserved_stock = fields?.includes(ProductSelectField.RESERVED_STOCK);
      low_stock_threshold = fields?.includes(
        ProductSelectField.LOW_STOCK_THRESHOLD,
      );
    }
    const select: Prisma.productsSelect = {
      id: true,
      iva: true,
      name: true,
      title: true,
      product_code: true,
      products_files: true,
      product_translations: {
        select: { lang_code: true, name: true },
        where: { ...(langCode && { lang_code: langCode }) },
      },
      variant_products: {
        select: {
          id: true,
          iva: true,
          sort: true,
          price: true,
          type_sale: true,
          price_iva: true,
          product_code: true,
          min_order_qty: true,
          available_stock: true,
          ...(status && { status }),
          ...(created_at && { created_at }),
          ...(updated_at && { updated_at }),
          ...(created_by && { created_by }),
          ...(updated_by && { updated_by }),
          ...(reserved_stock && { reserved_stock }),
          ...(low_stock_threshold && { low_stock_threshold }),
        },
      },
      ...(user_id && { user_id }),
      ...(status && { status }),
      ...(created_at && { created_at }),
      ...(updated_at && { updated_at }),
      ...(created_by && { created_by }),
      ...(updated_by && { updated_by }),
    };

    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const permissionCondition: Prisma.productsWhereInput = accessibleBy(
      ability,
      Action.Read,
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    ).products;

    const idBigInt = BigInt(id);
    const where: Prisma.productsWhereInput = {
      id: idBigInt,
      ...permissionCondition,
    };

    this.logger.info('permissionCondition', permissionCondition);

    return this.prisma.products.findFirst({ where, select });
  }

  async update(
    id: string,
    updateProductDto: UpdateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const productId = BigInt(id);

    const existingProduct = await this.prisma.products.findUnique({
      where: { id: productId },
      select: {
        user_id: true,
        iva: true,
        variant_products: { select: { id: true } },
        products_files: { select: { files: { select: { mime_type: true } } } },
      },
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

    const existVariant =
      (variantsToDelete?.length ?? 0) + (createVariants?.length ?? 0);

    if (existVariant > 50) {
      throw new BadRequestException('You can only update up to 50 variants');
    }

    await this.validateAndCheckFiles(files, user);

    await this.prisma.$transaction(async (tx) => {
      const now = new Date();
      // 更新主产品信息 (Main Info)
      await tx.products.update({
        where: { id: productId },
        data: {
          ...mainProductData,
          updated_by: user.userId,
          updated_at: now,
          // 如果有更新主分类的逻辑:
          ...(primary_category_id && {
            product_categories: {
              updateMany: {
                where: { product_id: productId, is_primary: true },
                data: { category_id: BigInt(primary_category_id) },
              },
            },
          }),
        },
      });

      const currentVariantIds = existingProduct.variant_products.map((v) =>
        BigInt(v.id),
      );

      // 创建变体
      if (createVariants && createVariants.length > 0) {
        const createData: Prisma.variant_productsCreateManyInput[] =
          createVariants.map((variant) => ({
            product_id: productId,
            created_by: user.userId,
            type_sale: variant.type_sale,
            sort: variant.sort,
            iva: variant.iva ?? mainProductData.iva ?? existingProduct.iva,
            ...computePrice(
              variant.price,
              variant.price_iva,
              Number(variant.iva ?? mainProductData.iva ?? existingProduct.iva),
            ),
            product_code: variant.product_code,
            min_order_qty: variant.min_order_qty,
            sale_unit_qty: variant.sale_unit_qty,
            available_stock: variant.available_stock,
            low_stock_threshold: variant.low_stock_threshold,
          }));

        await tx.variant_products.createMany({ data: createData });
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
            // 重新计算价格逻辑
            const iva =
              variant.iva ?? mainProductData.iva ?? existingProduct.iva;
            const priceData = computePrice(
              variant.price,
              variant.price_iva,
              Number(iva), // 优先级：变体 -> 产品更新值 -> 数据库旧值
            );

            return tx.variant_products.update({
              where: { id: BigInt(variant.id) },
              data: {
                type_sale: variant.type_sale,
                sort: variant.sort,
                iva: variant.iva,
                product_code: variant.product_code,
                available_stock: variant.available_stock,
                sale_unit_qty: variant.sale_unit_qty,
                min_order_qty: variant.min_order_qty,
                low_stock_threshold: variant.low_stock_threshold,
                ...priceData, // 展开计算后的价格字段
                updated_by: user.userId,
                updated_at: now,
              },
            });
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
        await tx.variant_products.deleteMany({
          where: { product_id: productId, id: { in: toDeleteIds } },
        });
      }

      if (translationsToDelete && translationsToDelete.length > 0) {
        await tx.product_translations.deleteMany({
          where: {
            product_id: productId,
            lang_code: { in: translationsToDelete },
          },
        });
      }

      // 对于翻译，数据量小，Upsert 是最优雅的 XD。
      if (translations && translations.length > 0) {
        await tx.$queryRaw`
          INSERT INTO product_translations (
            product_id,
            lang_code,
            name,
            title,
            description,
            updated_by
          )
          VALUES ${Prisma.join(
            translations.map(
              (t) => Prisma.sql`(
                ${id}::bigint,
                ${t.lang_code},
                ${t.name},
                ${t.title},
                ${t.description},
                ${user.userId}::uuid
              )`,
            ),
          )}
          ON CONFLICT (product_id, lang_code)
          DO UPDATE SET
            name = EXCLUDED.name,
            title = EXCLUDED.title,
            description = EXCLUDED.description,
            updated_by = ${user.userId}::uuid,
            updated_at = NOW()
          ;
        `;
      }

      if (files) {
        // 策略：文件通常涉及排序。全量替换关系表是处理排序最简单的方法。
        await tx.products_files.deleteMany({
          where: { product_id: productId },
        });

        if (files.length > 0) {
          const data: Prisma.products_filesCreateManyInput[] = files.map(
            (file) => ({
              product_id: productId,
              file_id: BigInt(file.file_id),
              sort: file.sort,
            }),
          );
          await tx.products_files.createMany({ data });
        }
      }
    });
  }

  async remove(id: string, ability: AppAbility) {
    const idBigInt = BigInt(id);
    const product = await this.prisma.products.findUnique({
      where: { id: idBigInt },
      select: { user_id: true },
    });
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
    await this.prisma.products.delete({ where: { id: idBigInt } });
  }

  private async validateAndCheckFiles(
    filesDTO: ProductFileDto[] | undefined,
    user: UserPayload,
    productOwnerId?: string,
  ) {
    if (!filesDTO || filesDTO.length === 0) return;
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
    const validFiles = await this.prisma.user_uploads.findMany({
      where: {
        file_id: { in: uniqueFileIds },
        user_id: { in: Array.from(allowedOwnerIds) },
      },
      select: {
        file_id: true,
      },
      // 即使 User 和 Wholesaler 都拥有 File A，结果里也只会出现一次 File A
      distinct: ['file_id'],
    });
    const fileIds = filesDTO.map((f) => BigInt(f.file_id));

    // uniqueFileIds 和 validFiles 是去重的, 如果数量不一致->说明有文件没找到归属权
    if (validFiles.length === fileIds.length) {
      throw new ForbiddenException(
        'You do not have permission to use one or more provided files.',
      );
    }
    const filesForProduct = await this.prisma.files.findMany({
      where: { id: { in: fileIds } },
    });

    if (filesForProduct.length !== filesDTO.length) {
      throw new BadRequestException('One or more files not found');
    }

    const imagesForProduct = filesForProduct.filter((file) =>
      IMAGE_MIME_TYPES.has(file.mime_type),
    );
    const videosForProduct = filesForProduct.filter((file) =>
      VIDEO_MIME_TYPES.has(file.mime_type),
    );
    const docsForProduct = filesForProduct.filter((file) =>
      DOC_MIME_TYPES.has(file.mime_type),
    );

    const maxImageForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_IMAGES, 10),
    );
    const maxVideoForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_VIDEOS, 1),
    );
    const maxDocForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_DOCUMENTS, 5),
    );

    if (imagesForProduct.length > maxImageForProduct) {
      throw new BadRequestException(
        `You can only upload up to ${maxImageForProduct} images for a product`,
      );
    }
    if (videosForProduct.length > maxVideoForProduct) {
      throw new BadRequestException(
        `You can only upload up to ${maxVideoForProduct} videos for a product`,
      );
    }
    if (docsForProduct.length > maxDocForProduct) {
      throw new BadRequestException(
        `You can only upload up to ${maxDocForProduct} documents for a product`,
      );
    }
  }
}
