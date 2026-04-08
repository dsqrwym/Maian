import { SaleVariant } from 'src/generated/prisma/client';
import { TagsSort } from '../../utils/typia/validators/sort.validator';
import typia, { tags } from 'typia';
import {
  TagsPrice,
  TagsPriceIva,
  TagsProductCode,
} from '../../utils/typia/validators/product.validator';
import { TagsUInt4 } from '../../utils/typia/tags/number.tags';
import { isObject } from '../../utils/is.util';
import { BadRequestException } from '@nestjs/common';
import Decimal from 'decimal.js';

export interface ICreateVariantDto {
  // --- 核心销售和定价字段 (variant_products) ---
  type_sale: SaleVariant;

  sort: TagsSort; // 显示顺序，数量越小越靠前

  price?: TagsPrice; // 不含税价格

  price_iva?: TagsPriceIva; // 含税价格

  product_code: TagsProductCode; // 变体的编码
  // --- 库存和销售配置字段 (variant_products) ---

  available_stock: TagsUInt4; // 初始库存

  sale_unit_qty: TagsUInt4 & tags.Minimum<1> & tags.Maximum<1000000>; // 换算因子 (例如：1 箱 = 24 件)， 最大一百万应该足够了

  min_order_qty: TagsUInt4 & tags.Minimum<1>; // 最小起订量 (以销售单位计)

  low_stock_threshold?: TagsUInt4; // 低库存预警阈值

  attributes?: string; // 暂时不用
}
export const validateICreateVariant = (input: unknown, productIva?: string) => {
  if (isObject(input)) {
    if (typeof input.product_code === 'string') {
      input.product_code = input.product_code.trim();
    }
  }

  const typedBody = typia.assertEquals<ICreateVariantDto>(input);

  if (
    !typedBody.price &&
    Number(typedBody.price) !== 0 &&
    !typedBody.price_iva &&
    Number(typedBody.price_iva) !== 0
  ) {
    throw new BadRequestException(
      'At least one of price and price_iva is required',
    );
  }

  // 联动验证：price 与 price_iva 最大值
  const maxPrice = new Decimal(10000000);
  const maxPriceIva = new Decimal(20000000);
  const iva = new Decimal(productIva ?? 0);
  const onePlusIva = iva.div(100).add(1);

  if (typedBody.price) {
    const priceDec = new Decimal(typedBody.price);
    const calcPriceIva = priceDec.mul(onePlusIva);
    if (calcPriceIva.gt(maxPriceIva)) {
      throw new BadRequestException(
        `Price + IVA exceeds maximum allowed price_iva ${maxPriceIva.toFixed(2)}`,
      );
    }
  }

  if (typedBody.price_iva) {
    const priceIvaDec = new Decimal(typedBody.price_iva);
    const calcPrice = priceIvaDec.div(onePlusIva);
    if (calcPrice.gt(maxPrice)) {
      throw new BadRequestException(
        `Price derived from price_iva exceeds maximum allowed price ${maxPrice.toFixed(2)}`,
      );
    }
  }

  if (
    typedBody.low_stock_threshold &&
    typedBody.low_stock_threshold >= typedBody.available_stock
  ) {
    throw new BadRequestException(
      `Low stock threshold ${typedBody.low_stock_threshold} must be small than available stock ${typedBody.available_stock}`,
    );
  }

  return typedBody;
};
