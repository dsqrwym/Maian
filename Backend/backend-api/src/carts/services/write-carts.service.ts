import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { ICreateCartItemDto } from '#/carts/dto/create-cart-item.dto.js';
import {
  cart_details,
  carts,
  products,
  users,
  variant_products,
} from '#/generated/drizzle/schema.js';
import { and, eq, exists, inArray, sql } from 'drizzle-orm';
import { ProductStatus } from '#/generated/drizzle/enums.js';
import { SQL_NOW } from '#/drizzle/drizzle.constants.js';
import { PinoLogger } from 'nestjs-pino';
import { CART_ERRORS } from '#/carts/cart.constants.js';
import { IUpdateCartItem } from '#/carts/dto/update-cart-item.dto.js';
import { MARKETPLACE_VISIBLE_STATUSES } from '#/user/user-status.constants.js';

@Injectable()
export class WriteCartsService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(WriteCartsService.name);
  }

  /**
   * 加入到购物篮属于购物意图，不需要原子性
   * @param dto
   * @param retailer_id
   */
  async addCartItem(dto: ICreateCartItemDto, retailer_id: string) {
    const { variant_id, quantity } = dto;
    const variantId = BigInt(variant_id);

    await this.drizzle.db.transaction(async (tx) => {
      const [variant] = await tx
        .select({
          wholesaler_id: users.id,
          available_stock: variant_products.available_stock,
          sale_unit_qty: variant_products.sale_unit_qty,
          min_order_qty: variant_products.min_order_qty,
        })
        .from(variant_products)
        .innerJoin(products, eq(products.id, variant_products.product_id))
        .innerJoin(users, eq(users.id, products.user_id))
        .where(
          and(
            eq(variant_products.id, variantId),
            eq(variant_products.status, ProductStatus.ACTIVE),
            eq(products.status, ProductStatus.ACTIVE),
            inArray(users.status, MARKETPLACE_VISIBLE_STATUSES),
          ),
        );

      if (!variant) {
        throw new NotFoundException(CART_ERRORS.VARIANT_NOT_FOUND_OR_INVALID);
      }

      const upsertCart = tx.$with('upsertCart').as(
        tx
          .insert(carts)
          .values({
            wholesaler_id: variant.wholesaler_id,
            retailer_id,
          })
          .onConflictDoUpdate({
            target: [carts.wholesaler_id, carts.retailer_id],
            set: {
              updated_at: SQL_NOW,
            },
          })
          .returning({
            id: carts.id,
          }),
      );

      const [cartDetail] = await tx
        .with(upsertCart)
        .select({
          cart_id: upsertCart.id,
          quantity: cart_details.quantity,
        })
        .from(upsertCart)
        .leftJoin(
          cart_details,
          and(
            eq(cart_details.cart_id, upsertCart.id),
            eq(cart_details.variant_products_id, variantId),
          ),
        );

      if (!cartDetail) {
        this.logger.error('Failed to create cart');
        throw new BadRequestException('Failed to create cart');
      }

      const existingQuantity = cartDetail.quantity ?? 0;
      // quantity 的 max 为 100 0000 结果不可能超过 Js number int 的精确度
      const newQuantity = existingQuantity + quantity;
      // min_order_qty 的 max 为 100 0000 同上
      if (newQuantity < variant.min_order_qty) {
        throw new BadRequestException(CART_ERRORS.QUANTITY_BELOW_MIN_ORDER);
      }
      // sale_unit_qty 的 max 为 100 0000 同上
      const requiredStock = newQuantity * variant.sale_unit_qty;

      if (requiredStock > variant.available_stock) {
        throw new BadRequestException(CART_ERRORS.NOT_ENOUGH_STOCK);
      }
      await tx
        .insert(cart_details)
        .values({
          cart_id: cartDetail.cart_id,
          variant_products_id: variantId,
          quantity: newQuantity,
        })
        .onConflictDoUpdate({
          target: [cart_details.cart_id, cart_details.variant_products_id],
          set: {
            quantity: newQuantity,
            updated_at: SQL_NOW,
          },
        });
    });
  }

  async updateQuantity(
    dto: IUpdateCartItem,
    cartDetailId: string,
    retailer_id: string,
  ) {
    const quantity = dto.quantity;
    const cartDetailIdBigInt = BigInt(cartDetailId);

    await this.drizzle.db.transaction(async (tx) => {
      const [item] = await tx
        .select({
          available_stock: variant_products.available_stock,
          sale_unit_qty: variant_products.sale_unit_qty,
          min_order_qty: variant_products.min_order_qty,
        })
        .from(cart_details)
        .innerJoin(carts, eq(carts.id, cart_details.cart_id))
        .innerJoin(
          variant_products,
          eq(variant_products.id, cart_details.variant_products_id),
        )
        .innerJoin(products, eq(products.id, variant_products.product_id))
        .innerJoin(users, eq(users.id, products.user_id))
        .where(
          and(
            eq(cart_details.id, cartDetailIdBigInt),
            eq(carts.retailer_id, retailer_id),
            eq(variant_products.status, ProductStatus.ACTIVE),
            eq(products.status, ProductStatus.ACTIVE),
            inArray(users.status, MARKETPLACE_VISIBLE_STATUSES),
          ),
        )
        .limit(1);

      if (!item) {
        throw new NotFoundException(CART_ERRORS.VARIANT_NOT_FOUND_OR_INVALID);
      }

      if (quantity < item.min_order_qty) {
        throw new BadRequestException(CART_ERRORS.QUANTITY_BELOW_MIN_ORDER);
      }

      const requiredStock = quantity * item.sale_unit_qty;

      if (requiredStock > item.available_stock) {
        throw new BadRequestException(CART_ERRORS.NOT_ENOUGH_STOCK);
      }

      const updateCartDetail = tx.$with('updateCartDetail').as(
        tx
          .update(cart_details)
          .set({
            quantity,
            updated_at: SQL_NOW,
          })
          .where(eq(cart_details.id, cartDetailIdBigInt))
          .returning({
            cart_id: cart_details.cart_id,
          }),
      );

      const [updated] = await tx
        .with(updateCartDetail)
        .update(carts)
        .set({
          updated_at: SQL_NOW,
        })
        .from(updateCartDetail)
        .where(eq(carts.id, updateCartDetail.cart_id))
        .returning({
          id: carts.id,
        });

      if (!updated) {
        throw new NotFoundException(CART_ERRORS.CART_ITEM_NOT_FOUND);
      }
    });
  }

  async deleteCartItem(cartDetailId: string, retailer_id: string) {
    const [deleted] = await this.drizzle.db
      .delete(cart_details)
      .where(
        and(
          eq(cart_details.id, BigInt(cartDetailId)),
          exists(
            this.drizzle.db
              .select({ one: sql`1` })
              .from(carts)
              .where(
                and(
                  eq(carts.id, cart_details.cart_id),
                  eq(carts.retailer_id, retailer_id),
                ),
              ),
          ),
        ),
      )
      .returning({
        id: cart_details.id,
      });

    if (!deleted) {
      throw new NotFoundException(CART_ERRORS.CART_ITEM_NOT_FOUND);
    }
  }

  async deleteCart(wholesalerId: string, retailerId: string) {
    const [deleted] = await this.drizzle.db
      .delete(carts)
      .where(
        and(
          eq(carts.wholesaler_id, wholesalerId),
          eq(carts.retailer_id, retailerId),
        ),
      )
      .returning({
        id: carts.id,
      });

    if (!deleted) {
      throw new NotFoundException(CART_ERRORS.CART_NOT_FOUND);
    }
  }
}
