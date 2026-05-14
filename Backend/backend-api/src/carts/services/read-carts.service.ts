import { ForbiddenException, Injectable } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { PinoLogger } from 'nestjs-pino';
import { UserPayload } from '#/auth/auth.types.js';
import { ICartsQueryDto } from '#/carts/dto/carts-query.dto.js';
import {
  cart_details,
  carts,
  files,
  product_translations,
  products,
  products_files,
  users,
  variant_products,
} from '#/generated/drizzle/schema.js';
import { and, asc, desc, eq, like, SQL, sql } from 'drizzle-orm';
import { SQL_TRUE } from '#/drizzle/drizzle.constants.js';
import { ProductStatus, UserStatus } from '#/generated/drizzle/enums.js';
import { Decimal } from 'decimal.js';
import { ICartGroup, ICartResponse } from '#/carts/dto/carts-response.dto.js';
import {
  CART_GROUP_STATUS,
  CART_ITEM_STATUS,
  CartItemStatus,
} from '#/carts/cart.constants.js';

@Injectable()
export class ReadCartsService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(ReadCartsService.name);
  }

  async getMyCartInfo(
    query: ICartsQueryDto,
    retailer: UserPayload,
    ability: AppAbility,
  ): Promise<ICartResponse> {
    if (!ability.can(Action.Read, 'carts')) {
      throw new ForbiddenException('You do not permission to read carts');
    }

    const { langCode, wholesaler_id } = query;

    const profile = users.profile;
    const companyNameExpr = sql<string>`${profile}->>'company_name'`;
    const displayNameExpr = sql<
      string | null | undefined
    >`${profile}->>'display_name'`;
    const minimumOrderAmountExpr = sql<
      string | null | undefined
    >`${profile}->>'minimum_order_amount'`;

    const translationsLateral = this.drizzle.db
      .select({
        product_translations: sql<
          | { lang_code: string; name: string | null; title: string | null }[]
          | null
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

    let cartsInfoQuery = this.drizzle.db
      .select({
        // 批发商信息
        wholesaler_id: users.id,
        wholesaler_status: users.status,
        profile_image_file_id: users.profile_image_file_id,
        display_name: displayNameExpr,
        company_name: companyNameExpr,
        minimum_order_amount: minimumOrderAmountExpr,
        // 购物篮 item
        cart_details_id: cart_details.id,
        quantity: cart_details.quantity,
        // 选择的产品变体信息
        variant_id: variant_products.id,
        variant_code: variant_products.product_code,
        variant_status: variant_products.status,
        sale_unit_qty: variant_products.sale_unit_qty,
        min_order_qty: variant_products.min_order_qty,
        available_stock: variant_products.available_stock,
        price: variant_products.price,
        price_iva: variant_products.price_iva,
        // 产品信息
        product_id: products.id,
        product_name: products.name,
        product_main_image: mainImgLateral.main_image,
        ...(langCode && {
          product_translations: translationsLateral.product_translations,
        }),
        product_title: products.title,
        product_code: products.product_code,
        product_iva: products.iva,
        product_status: products.status,
      })
      .from(carts)
      .innerJoin(cart_details, eq(cart_details.cart_id, carts.id))
      .innerJoin(
        variant_products,
        eq(variant_products.id, cart_details.variant_products_id),
      )
      .innerJoin(products, eq(products.id, variant_products.product_id))
      .innerJoin(users, eq(users.id, carts.wholesaler_id))
      .leftJoinLateral(mainImgLateral, SQL_TRUE)
      .$dynamic();

    if (langCode) {
      cartsInfoQuery = cartsInfoQuery.leftJoinLateral(
        translationsLateral,
        SQL_TRUE,
      );
    }

    const whereConditions: (SQL | undefined)[] = [
      eq(carts.retailer_id, retailer.userId),
    ];

    if (wholesaler_id) {
      whereConditions.push(eq(carts.wholesaler_id, wholesaler_id));
    }

    const cartsInfo = await cartsInfoQuery
      .where(and(...whereConditions))
      .orderBy(desc(carts.updated_at), asc(cart_details.created_at));

    const groupMap = new Map<string, ICartGroup>();

    let summaryItemCount = 0;
    let summaryTotalQuantity = 0;
    let summarySubtotal = new Decimal(0);
    let summaryIvaTotal = new Decimal(0);
    let summaryTotal = new Decimal(0);

    cartsInfo.forEach((row) => {
      const wholesalerId = row.wholesaler_id;
      let group = groupMap.get(wholesalerId);
      if (!group) {
        group = {
          wholesaler: {
            id: wholesalerId,
            company_name: row.company_name,
            display_name: row.display_name,
            profile_image_file_id: row.profile_image_file_id,
            minimum_order_amount: row.minimum_order_amount,
          },

          status: CART_GROUP_STATUS.AVAILABLE,

          item_count: 0,
          total_quantity: 0,

          subtotal: new Decimal(0).toFixed(2),
          iva_total: new Decimal(0).toFixed(2),
          total: new Decimal(0).toFixed(2),

          items: [],
        };

        groupMap.set(wholesalerId, group);
      }

      // line
      let status: CartItemStatus = CART_ITEM_STATUS.AVAILABLE;
      if (
        row.wholesaler_status === UserStatus.BANNED ||
        row.wholesaler_status === UserStatus.PENDING_VERIFICATION ||
        row.wholesaler_status === UserStatus.INACTIVE ||
        row.wholesaler_status === UserStatus.PENDING_REVIEW
      ) {
        status = CART_ITEM_STATUS.WHOLESALER_UNAVAILABLE;
      } else if (row.product_status !== ProductStatus.ACTIVE) {
        status = CART_ITEM_STATUS.PRODUCT_INACTIVE;
      } else if (row.variant_status !== ProductStatus.ACTIVE) {
        status = CART_ITEM_STATUS.VARIANT_INACTIVE;
      } else if (row.quantity < row.min_order_qty) {
        status = CART_ITEM_STATUS.BELOW_MIN_ORDER_QTY;
      } else if (row.quantity * row.sale_unit_qty > row.available_stock) {
        status = CART_ITEM_STATUS.INSUFFICIENT_STOCK;
      }
      const max_order_quantity = Math.floor(
        row.available_stock / row.sale_unit_qty,
      );
      const quantity = new Decimal(row.quantity);
      const unitPrice = new Decimal(row.price);
      const unitPriceIva = new Decimal(row.price_iva);

      const lineSubtotal = quantity.mul(unitPrice);
      const lineTotal = quantity.mul(unitPriceIva);
      const lineIva = lineTotal.minus(lineSubtotal);

      let productName = row.product_name;
      let productTitle = row.product_title;

      if (langCode) {
        const translation = row.product_translations?.find(
          (it) => it.lang_code === langCode,
        );
        productName = translation?.name ?? productName;
        productTitle = translation?.title ?? productTitle;
      }
      if (status !== CART_ITEM_STATUS.AVAILABLE) {
        group.status = CART_GROUP_STATUS.HAS_INVALID_ITEMS;
      }

      const item = {
        cart_detail_id: row.cart_details_id,

        product_id: row.product_id,
        variant_id: row.variant_id,

        main_image: row.product_main_image,
        product_name: productName,
        product_title: productTitle,
        product_code: row.product_code,
        variant_code: row.variant_code,

        quantity: row.quantity,
        sale_unit_qty: row.sale_unit_qty,
        min_order_qty: row.min_order_qty,
        max_order_quantity,

        price: row.price,
        price_iva: row.price_iva,
        iva: row.product_iva,

        line_subtotal: lineSubtotal.toFixed(2),
        line_iva: lineIva.toFixed(2),
        line_total: lineTotal.toFixed(2),

        status,
      };

      group.items.push(item);

      group.item_count += 1;
      group.total_quantity += row.quantity;

      group.subtotal = new Decimal(group.subtotal)
        .plus(lineSubtotal)
        .toFixed(2);
      group.iva_total = new Decimal(group.iva_total).plus(lineIva).toFixed(2);
      group.total = new Decimal(group.total).plus(lineTotal).toFixed(2);

      summaryItemCount += 1;
      summaryTotalQuantity += row.quantity;
      summarySubtotal = summarySubtotal.plus(lineSubtotal);
      summaryIvaTotal = summaryIvaTotal.plus(lineIva);
      summaryTotal = summaryTotal.plus(lineTotal);
    });

    const groups = Array.from(groupMap.values());

    for (const group of groups) {
      const minimumOrderAmount = group.wholesaler.minimum_order_amount;

      if (
        group.status === CART_GROUP_STATUS.AVAILABLE &&
        minimumOrderAmount &&
        new Decimal(group.total).lessThan(new Decimal(minimumOrderAmount))
      ) {
        group.status = CART_GROUP_STATUS.BELOW_MINIMUM_ORDER_AMOUNT;
      }
    }

    return {
      groups,
      summary: {
        wholesaler_count: groups.length,
        item_count: summaryItemCount,
        total_quantity: summaryTotalQuantity,
        subtotal: summarySubtotal.toFixed(2),
        iva_total: summaryIvaTotal.toFixed(2),
        total: summaryTotal.toFixed(2),
      },
    };
  }
}
