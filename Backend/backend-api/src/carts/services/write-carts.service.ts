import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { ICreateCartItemDto } from '#/carts/dto/create-cart-item.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import {
  cart_details,
  carts,
  products,
  users,
  variant_products,
} from '#/generated/drizzle/schema.js';
import { and, eq, notInArray } from 'drizzle-orm';
import { ProductStatus, UserStatus } from '#/generated/drizzle/enums.js';
import { SQL_NOW } from '#/drizzle/drizzle.constants.js';
import { PinoLogger } from 'nestjs-pino';
import { CART_ERRORS } from '#/carts/cart.constants.js';

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
   * @param ability
   */
  async addCartItem(
    dto: ICreateCartItemDto,
    retailer_id: string,
    ability: AppAbility,
  ) {
    if (!ability.can(Action.Create, 'carts')) {
      throw new ForbiddenException(
        'You do not have permission to add a cart item',
      );
    }

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
            notInArray(users.status, [
              UserStatus.BANNED,
              UserStatus.INACTIVE,
              UserStatus.PENDING_REVIEW,
              UserStatus.PENDING_VERIFICATION,
            ]),
          ),
        );

      if (!variant) {
        throw new NotFoundException(CART_ERRORS.VARIANT_NOT_FOUND_OR_INVALID);
      }

      const upsertCart = tx.$with('upsert_cart').as(
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
}
