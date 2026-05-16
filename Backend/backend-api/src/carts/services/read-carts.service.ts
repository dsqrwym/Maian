import { Injectable } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { PinoLogger } from 'nestjs-pino';
import { UserPayload } from '#/auth/auth.types.js';
import { ICartsQueryDto } from '#/carts/dto/carts-query.dto.js';
import {
  cart_details,
  carts,
  product_translations,
  products,
  users,
  variant_products,
} from '#/generated/drizzle/schema.js';
import { and, asc, desc, eq, SQL, sql } from 'drizzle-orm';
import { SQL_TRUE } from '#/drizzle/drizzle.constants.js';
import { ProductStatus } from '#/generated/drizzle/enums.js';
import { Decimal } from 'decimal.js';
import {
  ICartGroup,
  ICartGroupWithoutDecimal,
  ICartResponse,
} from '#/carts/dto/carts-response.dto.js';
import {
  CART_GROUP_STATUS,
  CART_ITEM_STATUS,
  CartItemStatus,
} from '#/carts/cart.constants.js';
import { MARKETPLACE_VISIBLE_STATUS_SET } from '#/user/user-status.constants.js';
import { buildWholesalerProfileExpr } from '#/utils/db/user.db.utils.js';
import { buildMainImgLateral } from '#/utils/db/product.db.utils.js';

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
  ): Promise<ICartResponse> {
    const { langCode, wholesaler_id } = query;

    const { companyNameExpr, displayNameExpr, minimumOrderAmountExpr } =
      buildWholesalerProfileExpr(users.profile);

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

    const mainImgLateral = buildMainImgLateral(this.drizzle.db);

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
      .orderBy(
        desc(carts.updated_at),
        desc(cart_details.updated_at),
        asc(products.product_code),
        asc(variant_products.product_code),
      );

    const groupMap = new Map<string, ICartGroup>();

    let summaryItemCount = 0;
    let summaryTotalQuantity = 0;
    let summarySubtotal = new Decimal(0);
    let summaryIvaTotal = new Decimal(0);
    let summaryTotal = new Decimal(0);

    for (const row of cartsInfo) {
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

          subtotal: new Decimal(0),
          iva_total: new Decimal(0),
          total: new Decimal(0),

          items: [],
        };

        groupMap.set(wholesalerId, group);
      }

      // line
      let status: CartItemStatus = CART_ITEM_STATUS.AVAILABLE;
      if (!MARKETPLACE_VISIBLE_STATUS_SET.has(row.wholesaler_status)) {
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
      const ivaRate = new Decimal(row.product_iva).div(100);
      // 数据库是 14,6 的
      const unitPrice = new Decimal(row.price);
      const unitPriceIva = new Decimal(row.price_iva);
      // 财务原则：不含税单价 * 数量 = 行小计 Round 2
      const lineSubtotal = quantity
        .mul(unitPrice)
        .toDecimalPlaces(2, Decimal.ROUND_HALF_UP);
      // 财务原则：行小计 * 税率 = 行税额 Round 2
      const lineIva = lineSubtotal
        .mul(ivaRate)
        .toDecimalPlaces(2, Decimal.ROUND_HALF_UP);
      const lineTotal = lineSubtotal.plus(lineIva);

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

        price: unitPrice.toFixed(2),
        price_iva: unitPriceIva.toFixed(2),
        iva: row.product_iva,

        line_subtotal: lineSubtotal.toFixed(2),
        line_iva: lineIva.toFixed(2),
        line_total: lineTotal.toFixed(2),

        status,
      };

      group.items.push(item);

      group.item_count += 1;
      group.total_quantity += row.quantity;

      group.subtotal = group.subtotal.plus(lineSubtotal);
      group.iva_total = group.iva_total.plus(lineIva);
      group.total = group.total.plus(lineTotal);

      summaryItemCount += 1;
      summaryTotalQuantity += row.quantity;
      summarySubtotal = summarySubtotal.plus(lineSubtotal);
      summaryIvaTotal = summaryIvaTotal.plus(lineIva);
      summaryTotal = summaryTotal.plus(lineTotal);
    }

    const groups: ICartGroupWithoutDecimal[] = Array.from(
      groupMap.values(),
    ).map((g) => ({
      wholesaler: g.wholesaler,
      item_count: g.item_count,
      total_quantity: g.total_quantity,
      subtotal: g.subtotal.toFixed(2),
      iva_total: g.iva_total.toFixed(2),
      total: g.total.toFixed(2),
      status: g.status,
      items: g.items,
    }));

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
