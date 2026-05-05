import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PinoLogger } from 'nestjs-pino';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { AppAbility } from '#/casl/casl-types.js';
import { UserPayload } from '#/auth/auth.types.js';
import {
  categories,
  category_translations,
  files,
  product_categories,
  product_translations,
  products,
  products_files,
  variant_products,
} from '#/generated/drizzle/schema.js';
import {
  and,
  asc,
  count,
  eq,
  exists,
  ilike,
  like,
  or,
  SQL,
  sql,
} from 'drizzle-orm';
import { Action } from '#/casl/actions.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { ProductListSelectField, ProductSortField } from '../product.enums.js';
import { IProductListQueryDto } from '../dto/product-list-query.dto.js';
import { IProductResponse } from '../dto/product-response.js';
import { escapeLike, toUnaccent } from '#/utils/string.util.js';
import { ENV } from '#/config/constants.config.js';
import { subject } from '@casl/ability';

@Injectable()
export class ProductsReadService {
  private readonly MAX_SEARCH_TERMS: number;
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
    private readonly configService: ConfigService,
  ) {
    this.MAX_SEARCH_TERMS = Number(
      this.configService.get<number>(ENV.MAX_SEARCH_TERMS, 10),
    );
    this.logger.setContext(ProductsReadService.name);
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

  getSortFieldDrizzle(sortBy?: ProductSortField, langCode?: string) {
    // COALESCE 代表 如果左值为 null 则返回右值，缺点是没有索引 以后优化
    switch (sortBy) {
      case ProductSortField.NAME:
        return langCode
          ? sql`COALESCE("localizedSortLateral"."sort_name", ${products.name})`
          : products.name;
      case ProductSortField.TITLE:
        return langCode
          ? sql`COALESCE("localizedSortLateral"."sort_title", ${products.title})`
          : products.title;
      case ProductSortField.CATEGORY:
        return sql.raw(`("mainCategoryLateral"."main_category"->>'name')`);
      case ProductSortField.PRODUCT_CODE:
        return products.product_code;
      case ProductSortField.MIN_ORDER_QTY:
        return sql.raw(`"variantAggregates"."min_order_qty"`);
      case ProductSortField.AVAILABLE_STOCK:
        return sql.raw(`"variantAggregates"."total_stock"`);
      case ProductSortField.PRICE_IVA:
        return sql.raw(`"variantAggregates"."min_price_iva"::numeric`);
      case ProductSortField.PRICE:
        return sql.raw(`"variantAggregates"."min_price"::numeric`);
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
    const isRetailer = user.userRole === UserRole.RETAILER;
    // 根据用户角色获取读取权限
    const permissionCondition = this.getReadListPermission(user);

    const { search, langCode, wholesaler_id, status } = query;
    const category_id = query.category_id
      ? BigInt(query.category_id)
      : undefined;
    const fields = query.fields;
    const iva = fields?.includes(ProductListSelectField.IVA);
    const selectedStatus =
      fields?.includes(ProductListSelectField.STATUS) && !isRetailer;
    const user_id =
      fields?.includes(ProductListSelectField.USER_ID) && !isRetailer;
    const category = fields?.includes(ProductListSelectField.CATEGORY);

    const { page, limit, sort_by, sort_order } = query;
    const offset = (page - 1) * limit;
    const sortField = this.getSortFieldDrizzle(sort_by, langCode);

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
        main_image: sql<{ id: string; mime_type: string } | null>`
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
      .where(
        and(
          eq(product_translations.product_id, products.id),
          langCode ? eq(product_translations.lang_code, langCode) : undefined,
        ),
      )
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

    if (isRetailer) {
      whereConditions.push(eq(products.status, 'ACTIVE'));
    } else if (status) {
      whereConditions.push(eq(products.status, status));
    }

    // 如果存在语言过滤 那么添加按照当前语言排序
    if (langCode) {
      const localizedSortLateral = this.drizzle.db
        .select({
          sort_name: sql<string>`${product_translations.name}`.as('sort_name'),
          sort_title: sql<string>`${product_translations.title}`.as(
            'sort_title',
          ),
        })
        .from(product_translations)
        .where(
          and(
            eq(product_translations.product_id, products.id),
            eq(product_translations.lang_code, langCode),
          ),
        )
        .limit(1)
        .as('localizedSortLateral');

      productQuery = productQuery.leftJoinLateral(
        localizedSortLateral,
        sql`TRUE`,
      );
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
