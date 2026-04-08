import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ICreateProductDto } from './dto/create-product.dto';
import { IUpdateProductDto } from './dto/update-product.dto';
import { AppAbility } from '../casl/casl-types';
import { Action } from '../casl/actions';
import { subject } from '@casl/ability';
import { PrismaService } from '../prisma/prisma.service';
import { UserPayload } from '../auth/auth.types';
import { computePrice } from '../utils/calculate/computePrice';
import { IProductListQueryDto } from './dto/product-list-query.dto';
import { accessibleBy } from '@casl/prisma';
import { Prisma, UserRole } from 'src/generated/prisma/client';
import { PinoLogger } from 'nestjs-pino';
import {
  ProductListSelectField,
  ProductSelectField,
  ProductSortField,
} from './product.enums';
import { PaginatedData } from '../common/types-interfaces/response.interface';
import { IProductQueryDto } from './dto/product-query.dto';
import { ConfigService } from '@nestjs/config';
import { ENV } from '../config/constants.config';
import {
  DOC_MIME_TYPES,
  IMAGE_MIME_TYPES,
  VIDEO_MIME_TYPES,
} from '../config/fastify-multipart.config';
import { IProductFileDto } from './dto/product-file.dto';

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
    } = createProductDto;

    if (!ability.can(Action.Create, subject('products', { user_id }))) {
      throw new ForbiddenException(
        'You are not allowed to create products that do not belong to you or your company',
      );
    }

    const maxVariantsForProduct = Number(
      this.configService.get<number>(ENV.PRODUCT_MAX_VARIANTS, 50),
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
              ...computePrice(variant.price, variant.price_iva, iva),
              product_code: variant.product_code,
              min_order_qty: variant.min_order_qty,
              sale_unit_qty: variant.sale_unit_qty,
              available_stock: variant.available_stock,
              low_stock_threshold: variant.low_stock_threshold,
              created_by: user.userId,
            })),
          },
        },
        ...(translations && {
          product_translations: {
            createMany: {
              data: translations.map((translation) => ({
                lang_code: translation.lang_code,
                name: translation.name,
                title: translation.title,
                description: translation.description,
              })),
            },
          },
        }),
        ...(files && {
          products_files: {
            createMany: {
              data: files.map((file) => ({
                file_id: BigInt(file.file_id),
                sort: file.sort,
              })),
            },
          },
        }),
      },
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

  getSortField(sortBy?: ProductSortField): string | undefined {
    switch (sortBy) {
      case ProductSortField.NAME:
        return 'p.name';
      case ProductSortField.TITLE:
        return 'p.title';
      case ProductSortField.CATEGORY:
        return `(cat.main_category->>'name')`;
      case ProductSortField.PRODUCT_CODE:
        return 'p.product_code';
      case ProductSortField.MIN_ORDER_QTY:
        return 'vp.min_order_qty';
      case ProductSortField.AVAILABLE_STOCK:
        return 'vp.total_stock';
      case ProductSortField.PRICE_IVA:
        return 'vp.min_price_iva';
      case ProductSortField.PRICE:
        return 'vp.min_price';
      default:
        return undefined;
    }
  }

  async findAllUseSql(
    query: IProductListQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    if (!ability.can(Action.Read, 'products')) {
      throw new ForbiddenException(
        'You do not have permission to read products',
      );
    }
    const permissionCondition = this.getReadListPermission(user);

    const { search, langCode, category_id, wholesaler_id, status } = query;
    const fields = query.fields;
    const iva = fields?.includes(ProductListSelectField.IVA);
    const selectedStatus = fields?.includes(ProductListSelectField.STATUS);
    const user_id = fields?.includes(ProductListSelectField.USER_ID);
    const category = fields?.includes(ProductListSelectField.CATEGORY);

    // 构建动态 SQL 参数数组
    const params: string[] = [];
    let paramIndex = 1; // $1, $2 ...

    const { page, limit, sort_by, sort_order } = query;
    const offset = (page - 1) * limit;

    const sortField = this.getSortField(sort_by);

    if (langCode) params.push(langCode);

    const selectFields = [
      'p.id',
      'p.name',
      'p.title',
      'p.product_code',
      'vp.min_price',
      'vp.min_price_iva',
      'vp.total_stock',
      'vp.min_order_qty',
      'img.main_image',
      category ? 'cat.main_category' : null,
      iva ? 'p.iva' : null,
      selectedStatus ? 'p.status' : null,
      user_id ? 'p.user_id' : null,
      `(SELECT COALESCE(jsonb_agg(jsonb_build_object(
        'lang_code', pt.lang_code,
        'name', pt.name,
        'title', pt.title
      )), '[]'::jsonb)
      FROM product_translations pt
      WHERE pt.product_id = p.id
      ${langCode ? `AND pt.lang_code = $${paramIndex++}` : ''}) AS product_translations`,
    ]
      .filter(Boolean)
      .join(', ');

    const whereClauses: string[] = ['1=1'];

    if (permissionCondition) {
      params.push(permissionCondition);
      whereClauses.push(`p.user_id = $${paramIndex++}`);
    }

    if (search) {
      params.push(`%${search}%`);
      const searchIndex = paramIndex++;
      whereClauses.push(`
      (
        p.name_unaccent ILIKE $${searchIndex} OR
        p.title_unaccent ILIKE $${searchIndex} OR
        p.product_code ILIKE $${searchIndex}
        OR EXISTS (
          SELECT 1 FROM variant_products vp
          WHERE vp.product_id = p.id AND vp.product_code ILIKE $${searchIndex}
        )
        OR EXISTS (
          SELECT 1
          FROM product_categories pc
          JOIN categories c ON c.id = pc.category_id
          LEFT JOIN category_translations ct ON ct.category_id = c.id
          WHERE pc.product_id = p.id
          AND (c.name ILIKE $${searchIndex} OR ct.name_unaccent ILIKE $${searchIndex})
        )
        OR EXISTS (
          SELECT 1
          FROM product_translations pt
          WHERE pt.product_id = p.id
          AND (pt.name_unaccent ILIKE $${searchIndex} OR pt.title_unaccent ILIKE $${searchIndex})
        )
      )
    `);
    }

    if (category_id != null) {
      params.push(category_id);
      whereClauses.push(`
      EXISTS (
        SELECT 1 FROM product_categories pc
        WHERE pc.product_id = p.id AND pc.category_id = $${paramIndex++}
      )
    `);
    }

    if (!permissionCondition && wholesaler_id) {
      params.push(wholesaler_id);
      whereClauses.push(`p.user_id = $${paramIndex++}`);
    }

    if (status) {
      params.push(status);
      whereClauses.push(`p.status = $${paramIndex++}`);
    }

    const whereSql = whereClauses.join(' AND ');

    const sql = `
    SELECT ${selectFields}
    FROM products p
      -- 主图
      LEFT JOIN LATERAL (
        SELECT jsonb_build_object('id', f.id, 'mime_type', f.mime_type) AS main_image
        FROM products_files pf
        JOIN files f ON f.id = pf.file_id
        WHERE pf.product_id = p.id AND f.mime_type LIKE 'image/%'
        ORDER BY pf.sort ASC
        LIMIT 1
      ) img ON TRUE
      -- 主分类
      LEFT JOIN LATERAL (
        SELECT jsonb_build_object(
          'id', c.id,
          'name', c.name,
          'category_translations', (
            SELECT COALESCE(jsonb_agg(jsonb_build_object(
              'lang_code', ct.lang_code,
              'name', ct.name
            )), '[]'::jsonb)
            FROM category_translations ct
            WHERE ct.category_id = c.id
            ${langCode ? `AND ct.lang_code = '${langCode}'` : ''}
          )
        ) AS main_category
        FROM product_categories pc
        JOIN categories c ON c.id = pc.category_id
        WHERE pc.product_id = p.id AND pc.is_primary = true
        LIMIT 1
      ) cat ON TRUE
      -- variant_products 聚合
      LEFT JOIN LATERAL (
        SELECT
          MIN(vp.price_iva) AS min_price_iva,
          MIN(vp.price) FILTER (
            WHERE vp.price_iva = (
              SELECT MIN(price_iva) FROM variant_products WHERE product_id = p.id
            )
          ) AS min_price,
          SUM(vp.available_stock * vp.sale_unit_qty) AS total_stock,
          MIN(vp.min_order_qty * vp.sale_unit_qty) AS min_order_qty
        FROM variant_products vp
        WHERE vp.product_id = p.id
      ) vp ON TRUE
    WHERE ${whereSql}
    ${sortField ? `ORDER BY ${sortField} ${sort_order}` : ''}
    LIMIT $${paramIndex++}
    OFFSET $${paramIndex++};
  `;
    const countParams = params.slice(); // 拷贝一份
    params.push(limit + '', offset + '');

    const countSql = `SELECT COUNT(*) AS total
                      FROM products p WHERE ${whereSql}`;

    const [products, count] = await Promise.all([
      this.prisma.$queryRawUnsafe<any[]>(sql, ...params),
      this.prisma.$queryRawUnsafe<[{ total: number }]>(
        countSql,
        ...countParams,
      ),
    ]);

    const total = Number(count[0].total);

    const result: PaginatedData = {
      items: products,
      pagination: { total, page, limit },
    };

    return result;
  }

  async findOne(
    id: string,
    query: IProductQueryDto,
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
    updateProductDto: IUpdateProductDto,
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
            const iva = mainProductData.iva ?? existingProduct.iva;
            const priceData = computePrice(
              variant.price,
              variant.price_iva,
              iva, // 产品更新值 -> 数据库旧值
            );

            return tx.variant_products.update({
              where: { id: BigInt(variant.id) },
              data: {
                type_sale: variant.type_sale,
                sort: variant.sort,
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
            INSERT INTO product_translations
            (product_id,
             lang_code,
             name,
             title,
             description,
             updated_by)
            SELECT ${productId} AS product_id,
                   p.lang_code,
                   p.name,
                   p.title,
                   p.description,
                   ${user.userId}::uuid
            FROM unnest(
                         ARRAY[
                             ${Prisma.join(
                               translations.map(
                                 (t) => Prisma.sql`${t.lang_code}`,
                               ),
                               ',',
                             )}
                        ]::text[],
                         ARRAY[
                             ${Prisma.join(
                               translations.map((t) => Prisma.sql`${t.name}`),
                               ',',
                             )}
                        ]::text[],
                         ARRAY[
                             ${Prisma.join(
                               translations.map((t) => Prisma.sql`${t.title}`),
                               ',',
                             )}
                        ]::text[],
                         ARRAY[
                             ${Prisma.join(
                               translations.map(
                                 (t) => Prisma.sql`${t.description}`,
                               ),
                               ',',
                             )}
                        ]::text[]
                 ) AS p(lang_code, name, title, description) 
                 ON CONFLICT (product_id, lang_code) DO
                 UPDATE SET
                    name = EXCLUDED.name,
                    title = EXCLUDED.title,
                    description = EXCLUDED.description,
                    updated_by = ${user.userId}::uuid,
                    updated_at = NOW();
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
    filesDTO: IProductFileDto[] | undefined,
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

    // uniqueFileIds 和 validFiles 是去重的, 如果数量不一致->说明有文件没找到归属权
    if (validFiles.length !== uniqueFileIds.length) {
      throw new ForbiddenException(
        'You do not have permission to use one or more provided files.',
      );
    }
    const filesForProduct = await this.prisma.files.findMany({
      where: { id: { in: uniqueFileIds } },
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
