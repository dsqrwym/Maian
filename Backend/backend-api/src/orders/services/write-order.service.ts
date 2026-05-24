import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import { ICreateOrderDto } from '#/orders/dto/create-order.dto.js';
import {
  ORDER_CURRENCY,
  ORDER_DOCUMENT_TYPE,
  ORDER_ERRORS,
  RETAILER_PROFILE,
  RETAILER_TABLE,
  WHOLESALER_PROFILE,
  WHOLESALER_TABLE,
} from '../order.constants.js';
import { and, eq, exists, gte, inArray, sql } from 'drizzle-orm';
import {
  AddressType,
  OrderStatus,
  ProductStatus,
  UserRole,
} from '#/generated/drizzle/enums.js';
import { ORDER_ALLOWED_STATUSES } from '#/user/user-status.constants.js';
import {
  cart_details,
  carts,
  cities,
  countries,
  directions,
  document_sequences,
  order_details,
  orders,
  product_translations,
  products,
  provinces,
  variant_products,
} from '#/generated/drizzle/schema.js';
import { PinoLogger } from 'nestjs-pino';
import { SQL_NOW, SQL_TRUE } from '#/drizzle/drizzle.constants.js';
import {
  IOrderLine,
  IRetailerSnapshot,
  IShippingAddressSnapshot,
  IWholesalerSnapshot,
} from '#/orders/order.types.js';
import { Decimal } from 'decimal.js';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { subject } from '@casl/ability';
import {
  ICancelOrderDto,
  IRejectOrderDto,
} from '#/orders/dto/change-order-status.dto.js';
import { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import { IUpdateOrderDto } from '../dto/update-order.dto.js';
import { OrderPdfNotificationService } from '#/orders/services/order-pdf-notification.service.js';
import { IProductTranslationDto } from '#/products/dto/product-translation.dto.js';

@Injectable()
export class WriteOrderService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
    private readonly orderPdfNotificationService: OrderPdfNotificationService,
  ) {
    this.logger.setContext(WriteOrderService.name);
  }

  async updateDeliveryDate(
    id: TagsIntegerString,
    wholesalerId: string,
    dto: IUpdateOrderDto,
    ability: AppAbility,
  ) {
    if (
      !ability.can(
        Action.Update,
        subject('orders', { wholesaler_id: wholesalerId }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to update this order',
      );
    }
    const { estimatedDeliveryDate } = dto;
    const estimatedDate = estimatedDeliveryDate
      ? new Date(estimatedDeliveryDate)
      : null;
    if (estimatedDate) {
      const today = new Date().setUTCHours(0, 0, 0, 0);
      if (estimatedDate.setUTCHours(0, 0, 0, 0) < today) {
        throw new BadRequestException(
          'Estimated delivery date cannot be in the past',
        );
      }
    }

    const [updatedOrder] = await this.drizzle.db
      .update(orders)
      .set({
        estimated_delivery_date: estimatedDate?.toISOString() ?? null,
        updated_at: SQL_NOW,
      })
      .where(
        and(
          eq(orders.id, BigInt(id)),
          eq(orders.wholesaler_id, wholesalerId),
          eq(orders.status, OrderStatus.ACCEPTED),
        ),
      )
      .returning({ id: orders.id });

    if (!updatedOrder) {
      throw new ConflictException(ORDER_ERRORS.ORDER_STATUS_INVALID);
    }
  }

  async rejectByWholesaler(
    id: TagsIntegerString,
    wholesalerId: string,
    userId: string,
    dto: IRejectOrderDto,
    ability: AppAbility,
  ) {
    if (
      !ability.can(
        Action.Update,
        subject('orders', { wholesaler_id: wholesalerId }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to reject this order',
      );
    }

    const { actionReason } = dto;
    const order_id = BigInt(id);
    await this.drizzle.db.transaction(async (tx) => {
      const order = await this.getOrderInfoForStatusChange(
        order_id,
        tx,
        undefined,
        wholesalerId,
      );

      for (const line of order) {
        if (line.variant_product_id === null) continue;
        const reservedQuantity = line.quantity * line.sale_unit_qty;
        const [updatedStock] = await tx
          .update(variant_products)
          .set({
            available_stock: sql`${variant_products.available_stock} + ${reservedQuantity}`,
            reserved_stock: sql`${variant_products.reserved_stock} - ${reservedQuantity}`,
            updated_at: SQL_NOW,
          })
          .where(
            and(
              eq(variant_products.id, line.variant_product_id),
              gte(variant_products.reserved_stock, reservedQuantity),
            ),
          )
          .returning({ id: variant_products.id });

        if (!updatedStock) {
          this.logger.error(
            {
              orderId: order_id,
              variantProductId: line.variant_product_id,
              reservedQuantity,
              line,
            },
            'Reserved stock could not be released',
          );
          throw new ConflictException(ORDER_ERRORS.RESERVED_STOCK_INCONSISTENT);
        }
      }

      const [updatedOrder] = await tx
        .update(orders)
        .set({
          status: OrderStatus.REJECTED,
          rejected_reason: actionReason,
          rejected_at: SQL_NOW,
          rejected_by: userId,
          updated_at: SQL_NOW,
        })
        .where(
          and(
            eq(orders.id, order_id),
            eq(orders.status, OrderStatus.PENDING),
            eq(orders.wholesaler_id, wholesalerId),
          ),
        )
        .returning({ id: orders.id });

      if (!updatedOrder) {
        throw new ConflictException(ORDER_ERRORS.ORDER_STATUS_INVALID);
      }
    });

    this.dispatchOrderPdfTask(id, 'notifyOrderRejected', () =>
      this.orderPdfNotificationService.notifyOrderRejected(id),
    );
  }

  async acceptOrderByWholesaler(
    id: string,
    wholesalerId: string,
    userId: string,
    ability: AppAbility,
  ) {
    if (
      !ability.can(
        Action.Update,
        subject('orders', { wholesaler_id: wholesalerId }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to accept this order',
      );
    }
    const order_id = BigInt(id);
    await this.drizzle.db.transaction(async (tx) => {
      const order = await this.getOrderInfoForStatusChange(
        order_id,
        tx,
        undefined,
        wholesalerId,
      );

      for (const line of order) {
        if (line.variant_product_id === null) continue;
        const reservedQuantity = line.quantity * line.sale_unit_qty;
        const [updatedStock] = await tx
          .update(variant_products)
          .set({
            reserved_stock: sql`${variant_products.reserved_stock} - ${reservedQuantity}`,
            updated_at: SQL_NOW,
          })
          .where(
            and(
              eq(variant_products.id, line.variant_product_id),
              gte(variant_products.reserved_stock, reservedQuantity),
            ),
          )
          .returning({ id: variant_products.id });

        if (!updatedStock) {
          this.logger.error(
            {
              orderId: order_id,
              variantProductId: line.variant_product_id,
              reservedQuantity,
              line,
            },
            'Reserved stock could not be released',
          );
          throw new ConflictException(ORDER_ERRORS.RESERVED_STOCK_INCONSISTENT);
        }
      }

      const [updatedOrder] = await tx
        .update(orders)
        .set({
          status: OrderStatus.ACCEPTED,
          accepted_at: SQL_NOW,
          accepted_by: userId,
          updated_at: SQL_NOW,
        })
        .where(
          and(
            eq(orders.id, order_id),
            eq(orders.status, OrderStatus.PENDING),
            eq(orders.wholesaler_id, wholesalerId),
          ),
        )
        .returning({ id: orders.id });

      if (!updatedOrder) {
        throw new ConflictException(ORDER_ERRORS.ORDER_STATUS_INVALID);
      }
    });

    this.dispatchOrderPdfTask(id, 'notifyOrderAccepted', () =>
      this.orderPdfNotificationService.notifyOrderAccepted(id),
    );
  }

  async cancelOrderByRetailer(
    orderId: string,
    retailerId: string,
    dto: ICancelOrderDto,
    ability: AppAbility,
  ) {
    if (
      !ability.can(
        Action.Update,
        subject('orders', { retailer_id: retailerId }),
      )
    ) {
      throw new ForbiddenException(
        'You do not have permission to cancel this order',
      );
    }
    const { actionReason } = dto;
    const order_id = BigInt(orderId);

    await this.drizzle.db.transaction(async (tx) => {
      const order = await this.getOrderInfoForStatusChange(
        order_id,
        tx,
        retailerId,
      );

      for (const line of order) {
        if (line.variant_product_id === null) continue;
        const reservedQuantity = line.quantity * line.sale_unit_qty;
        const [updatedStock] = await tx
          .update(variant_products)
          .set({
            available_stock: sql`${variant_products.available_stock} + ${reservedQuantity}`,
            reserved_stock: sql`${variant_products.reserved_stock} - ${reservedQuantity}`,
            updated_at: SQL_NOW,
          })
          .where(
            and(
              eq(variant_products.id, line.variant_product_id),
              gte(variant_products.reserved_stock, reservedQuantity),
            ),
          )
          .returning({ id: variant_products.id });

        if (!updatedStock) {
          this.logger.error(
            {
              orderId: order_id,
              variantProductId: line.variant_product_id,
              reservedQuantity,
              line,
            },
            'Reserved stock could not be released',
          );
          throw new ConflictException(ORDER_ERRORS.RESERVED_STOCK_INCONSISTENT);
        }
      }

      const [cancelledOrder] = await tx
        .update(orders)
        .set({
          status: OrderStatus.CANCELLED,
          cancelled_reason: actionReason,
          cancelled_at: SQL_NOW,
          cancelled_by: retailerId,
          updated_at: SQL_NOW,
        })
        .where(
          and(
            eq(orders.id, order_id),
            eq(orders.retailer_id, retailerId),
            eq(orders.status, OrderStatus.PENDING),
          ),
        )
        .returning({ id: orders.id });

      if (!cancelledOrder) {
        throw new ConflictException(ORDER_ERRORS.ORDER_STATUS_INVALID);
      }
    });

    this.dispatchOrderPdfTask(orderId, 'notifyOrderCancelled', () =>
      this.orderPdfNotificationService.notifyOrderCancelled(orderId),
    );
  }

  private async getOrderInfoForStatusChange(
    orderId: bigint,
    tx: DrizzleDb,
    retailerId?: string,
    wholesalerId?: string,
  ) {
    const order = await tx
      .select({
        variant_product_id: order_details.variant_product_id,
        quantity: order_details.quantity,
        sale_unit_qty: order_details.sale_unit_qty,
      })
      .from(orders)
      .innerJoin(order_details, eq(order_details.order_id, orders.id))
      .where(
        and(
          eq(orders.id, orderId),
          eq(orders.status, OrderStatus.PENDING),
          retailerId ? eq(orders.retailer_id, retailerId) : undefined,
          wholesalerId ? eq(orders.wholesaler_id, wholesalerId) : undefined,
        ),
      )
      .for('update', { of: orders });

    if (order?.length == 0) {
      throw new NotFoundException(ORDER_ERRORS.ORDER_STATUS_INVALID);
    }
    return order;
  }

  async createFromCart(
    retailerId: string,
    dto: ICreateOrderDto,
    ability: AppAbility,
  ) {
    const { wholesalerId } = dto;
    if (!ability.can(Action.Create, 'orders')) {
      throw new ForbiddenException(
        'You do not have permission to create order',
      );
    }
    const createdOrderResult = await this.drizzle.db.transaction(async (tx) => {
      // 先锁 cart 防止重复提交一样的订单
      const [cart] = await tx
        .select({ id: carts.id })
        .from(carts)
        .where(
          and(
            eq(carts.retailer_id, retailerId),
            eq(carts.wholesaler_id, wholesalerId),
          ),
        )
        .for('update')
        .limit(1);

      if (!cart) {
        throw new BadRequestException(ORDER_ERRORS.CART_EMPTY);
      }

      const retailerSnapshot = await this.getRetailerInfo(retailerId, tx);

      const wholesalerSnapshot = await this.getWholesalerInfo(wholesalerId, tx);

      const shoppingAddressSnapshot = await this.getShoppingAddress(
        retailerId,
        tx,
      );

      const { orderLines, itemCount, totalSubtotal, totalIva, totalAmount } =
        await this.getCartItemInfo(retailerId, wholesalerId, tx);

      const orderYear = new Date().getFullYear();
      const [sequence] = await this.getSequences(wholesalerId, orderYear, tx);

      // 更新库存
      for (const line of orderLines) {
        const reservedQuantity = line.quantity * line.saleUnitQty;
        const [updatedStock] = await tx
          .update(variant_products)
          .set({
            available_stock: sql`${variant_products.available_stock} - ${reservedQuantity}`,
            reserved_stock: sql`${variant_products.reserved_stock} + ${reservedQuantity}`,
            updated_at: SQL_NOW,
          })
          .where(
            and(
              eq(variant_products.id, line.variantProductId),
              eq(variant_products.status, ProductStatus.ACTIVE),
              gte(variant_products.available_stock, reservedQuantity),
              exists(
                tx
                  .select({ one: sql`1` })
                  .from(products)
                  .where(
                    and(
                      eq(products.id, variant_products.product_id),
                      eq(products.user_id, wholesalerId),
                      eq(products.status, ProductStatus.ACTIVE),
                    ),
                  ),
              ),
            ),
          )
          .returning({ id: variant_products.id });

        if (!updatedStock) {
          throw new BadRequestException(ORDER_ERRORS.NOT_ENOUGH_STOCK);
        }
      }

      const [createdOrder] = await tx
        .insert(orders)
        .values({
          order_series: ORDER_DOCUMENT_TYPE,
          order_year: orderYear,
          order_sequence: sequence.value,
          currency: ORDER_CURRENCY,
          order_number: `${wholesalerSnapshot.user_id}-${ORDER_DOCUMENT_TYPE}-${orderYear}-${String(sequence.value).padStart(6, '0')}`,
          retailer_id: retailerId,
          wholesaler_id: wholesalerId,
          subtotal: totalSubtotal.toFixed(2),
          discount_total: '0.00',
          iva_total: totalIva.toFixed(2),
          total: totalAmount.toFixed(2),
          item_count: itemCount,
          shipping_address_snapshot: shoppingAddressSnapshot,
          retailer_snapshot: retailerSnapshot,
          wholesaler_snapshot: wholesalerSnapshot,
        })
        .returning({ id: orders.id, order_number: orders.order_number });

      await tx.insert(order_details).values(
        orderLines.map((line) => ({
          order_id: createdOrder.id,
          product_id: line.productId,
          variant_product_id: line.variantProductId,

          product_name: line.productName,
          product_title: line.productTitle,
          product_code: line.productCode,
          variant_product_code: line.variantProductCode,

          product_translations_snapshot: line.productTranslationsSnapshot ?? [],
          variant_attributes_snapshot: line.variantAttributesSnapshot ?? {},

          type_sale: line.typeSale,
          sale_unit_qty: line.saleUnitQty,
          quantity: line.quantity,

          unit_price: line.unitPrice,
          unit_price_iva: line.unitPriceIva,
          iva: line.iva,

          subtotal: line.subtotal.toFixed(2),
          iva_total: line.ivaTotal.toFixed(2),
          total: line.total.toFixed(2),
        })),
      );

      await tx.delete(cart_details).where(
        inArray(
          cart_details.id,
          orderLines.map((line) => line.cartDetailsId),
        ),
      );

      await tx
        .update(carts)
        .set({ updated_at: SQL_NOW })
        .where(eq(carts.id, orderLines[0].cartId));

      return {
        id: createdOrder.id.toString(),
        order_number: createdOrder.order_number,
      };
    });

    this.dispatchOrderPdfTask(createdOrderResult.id, 'notifyNewOrder', () =>
      this.orderPdfNotificationService.notifyNewOrder(createdOrderResult.id),
    );

    return createdOrderResult;
  }

  private dispatchOrderPdfTask(
    orderId: string,
    taskName: string,
    task: () => Promise<unknown>,
  ) {
    void task().catch((err: unknown) => {
      this.logger.error(
        { err, orderId, taskName },
        'Order PDF background task failed',
      );
    });
  }

  private async getRetailerInfo(
    retailerId: string,
    tx: DrizzleDb,
  ): Promise<IRetailerSnapshot> {
    const [retailer] = await tx
      .select({
        id: RETAILER_TABLE.id,
        user_id: RETAILER_TABLE.user_id,
        tax_id: RETAILER_TABLE.tax_id,
        email: RETAILER_TABLE.email,
        telephone: RETAILER_TABLE.telephone,
        company_name: RETAILER_PROFILE.companyNameExpr,
        company_type: RETAILER_PROFILE.companyTypeExpr,
        display_name: RETAILER_PROFILE.displayNameExpr,
        contact_name: RETAILER_PROFILE.contactNameExpr,
      })
      .from(RETAILER_TABLE)
      .where(
        and(
          eq(RETAILER_TABLE.id, retailerId),
          eq(RETAILER_TABLE.role, UserRole.RETAILER),
          inArray(RETAILER_TABLE.status, ORDER_ALLOWED_STATUSES),
        ),
      )
      .limit(1);

    if (!retailer) {
      throw new BadRequestException(ORDER_ERRORS.RETAILER_NOT_FOUND_OR_INVALID);
    }

    return {
      id: retailer.id,
      user_id: retailer.user_id,
      display_name: retailer.display_name,
      company_name: retailer.company_name,
      company_type: retailer.company_type,
      contact_name: retailer.contact_name,
      tax_id: retailer.tax_id,
      email: retailer.email,
      telephone: retailer.telephone,
    };
  }

  private async getWholesalerInfo(
    wholesalerId: string,
    tx: DrizzleDb,
  ): Promise<IWholesalerSnapshot> {
    const [wholesaler] = await tx
      .select({
        id: WHOLESALER_TABLE.id,
        user_id: WHOLESALER_TABLE.user_id,
        tax_id: WHOLESALER_TABLE.tax_id,
        email: WHOLESALER_TABLE.email,
        telephone: WHOLESALER_TABLE.telephone,
        company_name: WHOLESALER_PROFILE.companyNameExpr,
        company_type: WHOLESALER_PROFILE.companyTypeExpr,
        display_name: WHOLESALER_PROFILE.displayNameExpr,
      })
      .from(WHOLESALER_TABLE)
      .where(
        and(
          eq(WHOLESALER_TABLE.id, wholesalerId),
          eq(WHOLESALER_TABLE.role, UserRole.WHOLESALER),
          inArray(WHOLESALER_TABLE.status, ORDER_ALLOWED_STATUSES),
        ),
      )
      .limit(1);

    if (!wholesaler) {
      throw new BadRequestException(
        ORDER_ERRORS.WHOLESALER_NOT_FOUND_OR_INVALID,
      );
    }

    return {
      id: wholesaler.id,
      user_id: wholesaler.user_id,
      display_name: wholesaler.display_name,
      company_name: wholesaler.company_name,
      company_type: wholesaler.company_type,
      tax_id: wholesaler.tax_id,
      email: wholesaler.email,
      telephone: wholesaler.telephone,
    };
  }

  private async getShoppingAddress(
    retailerId: string,
    tx: DrizzleDb,
  ): Promise<IShippingAddressSnapshot> {
    const [shoppingAddress] = await tx
      .select({
        id: directions.id,
        street: directions.street,
        zip_code: directions.zip_code,
        latitude: directions.latitude,
        longitude: directions.longitude,

        city_id: cities.id,
        city_name: cities.name,
        city_name_local: cities.name_local,

        province_id: provinces.id,
        province_name: provinces.name,
        province_name_local: provinces.name_local,

        country_iso: countries.iso_numeric,
        country_alpha2: countries.iso_alpha2,
        country_alpha3: countries.iso_alpha3,
        country_name: countries.name,
        country_name_local: countries.name_local,
      })
      .from(directions)
      .innerJoin(cities, eq(cities.id, directions.city_id))
      .innerJoin(provinces, eq(provinces.id, directions.province_id))
      .innerJoin(countries, eq(countries.iso_numeric, directions.country_iso))
      .where(
        and(
          eq(directions.user_id, retailerId),
          eq(directions.type, AddressType.STORE),
        ),
      )
      .limit(1);

    // 根据项目设置，在目前这是不可能的 用户的 STORE 地址永远不会空
    if (!shoppingAddress) {
      this.logger.error(`User:${retailerId} do not have a STORE Address`);
      throw new BadRequestException(ORDER_ERRORS.SHIPPING_ADDRESS_NOT_FOUND);
    }

    return {
      id: shoppingAddress.id.toString(),
      street: shoppingAddress.street,
      zip_code: shoppingAddress.zip_code,

      city_id: shoppingAddress.city_id,
      city_name: shoppingAddress.city_name,
      city_name_local: shoppingAddress.city_name_local,

      province_id: shoppingAddress.province_id,
      province_name: shoppingAddress.province_name,
      province_name_local: shoppingAddress.province_name_local,

      country_iso: shoppingAddress.country_iso,
      country_alpha2: shoppingAddress.country_alpha2,
      country_alpha3: shoppingAddress.country_alpha3,
      country_name: shoppingAddress.country_name,
      country_name_local: shoppingAddress.country_name_local,

      latitude: shoppingAddress.latitude,
      longitude: shoppingAddress.longitude,
    };
  }

  private async getCartItemInfo(
    retailerId: string,
    wholesalerId: string,
    tx: DrizzleDb,
  ) {
    const productTranslationsLateral = tx
      .select({
        product_translations: sql<IProductTranslationDto[] | null>`
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

    const cartItems = await tx
      .select({
        // 购物车信息
        cart_id: carts.id,
        cart_details_id: cart_details.id,
        quantity: cart_details.quantity,
        // 选择的产品变体信息
        variant_id: variant_products.id,
        variant_code: variant_products.product_code,
        variant_status: variant_products.status,
        sale_unit_qty: variant_products.sale_unit_qty,
        min_order_qty: variant_products.min_order_qty,
        type_sale: variant_products.type_sale,
        available_stock: variant_products.available_stock,
        price: variant_products.price,
        price_iva: variant_products.price_iva,
        variant_attributes: variant_products.attributes,
        // 产品的信息
        product_id: products.id,
        product_code: products.product_code,
        product_iva: products.iva,
        product_status: products.status,
        product_name: products.name,
        product_title: products.title,
        product_translations: productTranslationsLateral.product_translations,
      })
      .from(carts)
      .innerJoin(cart_details, eq(cart_details.cart_id, carts.id))
      .innerJoin(
        variant_products,
        eq(variant_products.id, cart_details.variant_products_id),
      )
      .innerJoin(products, eq(products.id, variant_products.product_id))
      .innerJoin(WHOLESALER_TABLE, eq(WHOLESALER_TABLE.id, carts.wholesaler_id))
      .innerJoin(RETAILER_TABLE, eq(RETAILER_TABLE.id, carts.retailer_id))
      .leftJoinLateral(productTranslationsLateral, SQL_TRUE)
      .where(
        and(
          eq(carts.retailer_id, retailerId),
          eq(carts.wholesaler_id, wholesalerId),
          eq(products.user_id, wholesalerId),
          inArray(RETAILER_TABLE.status, ORDER_ALLOWED_STATUSES),
          inArray(WHOLESALER_TABLE.status, ORDER_ALLOWED_STATUSES),
        ),
      );

    if (cartItems.length === 0) {
      throw new BadRequestException(ORDER_ERRORS.CART_EMPTY);
    }

    const orderLines: IOrderLine[] = [];
    let totalSubtotal = new Decimal(0);
    let totalIva = new Decimal(0);
    let totalAmount = new Decimal(0);

    for (const item of cartItems) {
      if (item.product_status !== ProductStatus.ACTIVE) {
        throw new BadRequestException(ORDER_ERRORS.PRODUCT_NOT_AVAILABLE);
      }
      if (item.variant_status !== ProductStatus.ACTIVE) {
        throw new BadRequestException(ORDER_ERRORS.VARIANT_NOT_AVAILABLE);
      }
      if (item.quantity < item.min_order_qty) {
        throw new BadRequestException(ORDER_ERRORS.QUANTITY_BELOW_MIN_ORDER);
      }

      // 此订单行总数量
      const reservedQuantity = item.quantity * item.sale_unit_qty;

      if (reservedQuantity > item.available_stock) {
        throw new BadRequestException(ORDER_ERRORS.NOT_ENOUGH_STOCK);
      }
      const quantity = new Decimal(item.quantity);
      const unitPrice = new Decimal(item.price);
      const ivaRate = new Decimal(item.product_iva).div(100); // 例如 21% 变为 0.21

      //财务计算准则 - 遵循“行级先行，即时舍入”原则 -> 保证所见即所得 -> sum(Subtotal) + sum(Tax) = sum(Total)
      // 计算行不含税金额并截断
      const lineSubtotal = unitPrice
        .mul(quantity)
        .toDecimalPlaces(2, Decimal.ROUND_HALF_UP);
      // 计算行税额并截断
      const lineIvaTotal = lineSubtotal
        .mul(ivaRate)
        .toDecimalPlaces(2, Decimal.ROUND_HALF_UP);
      // 计算行含税总额
      const lineTotal = lineSubtotal.plus(lineIvaTotal);

      // 累加到订单总计
      totalSubtotal = totalSubtotal.plus(lineSubtotal);
      totalIva = totalIva.plus(lineIvaTotal);
      totalAmount = totalAmount.plus(lineTotal);

      orderLines.push({
        cartId: item.cart_id,
        cartDetailsId: item.cart_details_id,

        productId: item.product_id,
        variantProductId: item.variant_id,

        productName: item.product_name,
        productTitle: item.product_title,
        productCode: item.product_code,
        variantProductCode: item.variant_code,
        variantAttributesSnapshot: item.variant_attributes,

        productTranslationsSnapshot: item.product_translations,

        typeSale: item.type_sale,
        saleUnitQty: item.sale_unit_qty,
        quantity: item.quantity,

        unitPrice: item.price,
        unitPriceIva: item.price_iva,
        iva: item.product_iva,

        subtotal: lineSubtotal,
        ivaTotal: lineIvaTotal,
        total: lineTotal,
      });
    }

    const itemCount = orderLines.length;

    return {
      orderLines,
      itemCount,
      totalSubtotal,
      totalIva,
      totalAmount,
    };
  }

  private async getSequences(
    wholesalerId: string,
    year: number,
    tx: DrizzleDb,
  ) {
    return tx
      .insert(document_sequences)
      .values({
        owner_id: wholesalerId,
        document_type: ORDER_DOCUMENT_TYPE,
        year,
        current_value: 1,
      })
      .onConflictDoUpdate({
        target: [
          document_sequences.owner_id,
          document_sequences.document_type,
          document_sequences.year,
        ],
        set: {
          current_value: sql`${document_sequences.current_value} + 1`,
        },
      })
      .returning({
        value: document_sequences.current_value,
      });
  }
}
