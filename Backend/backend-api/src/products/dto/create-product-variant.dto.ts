import { SaleVariant } from 'src/generated/prisma/client';
import { TagsSort } from '../../utils/typia/validators/sort.validator';
import typia, { tags } from 'typia';
import {
  TagsIva,
  TagsPrice,
  TagsPriceIva,
  TagsProductCode,
} from '../../utils/typia/validators/product.validator';
import { TagsUInt4 } from '../../utils/typia/tags/number.tags';
import { isObject } from '../../utils/is.util';
import { BadRequestException } from '@nestjs/common';

export interface ICreateVariantDto {
  // --- 核心销售和定价字段 (variant_products) ---
  type_sale: SaleVariant;

  sort: TagsSort; // 显示顺序，数量越小越靠前

  price?: TagsPrice; // 不含税价格

  price_iva?: TagsPriceIva; // 含税价格

  iva?: TagsIva; // 变体适用的税率（不存在则继承自 product.iva）

  product_code: TagsProductCode; // 变体的编码
  // --- 库存和销售配置字段 (variant_products) ---

  available_stock: TagsUInt4; // 初始库存

  sale_unit_qty: TagsUInt4 & tags.Minimum<1>; // 换算因子 (例如：1 箱 = 24 件)

  min_order_qty: TagsUInt4 & tags.Minimum<1>; // 最小起订量 (以销售单位计)

  low_stock_threshold?: TagsUInt4; // 低库存预警阈值

  attributes?: string; // 暂时不用
}
export const validateICreateVariant = (input: unknown) => {
  if (isObject(input)) {
    if (typeof input.product_code === 'string') {
      input.product_code = input.product_code.trim();
    }
  }
  const typedBody = typia.assertEquals<ICreateVariantDto>(input);
  if (
    !typedBody.price &&
    typedBody.price === 0 &&
    !typedBody.price_iva &&
    typedBody.price_iva === 0
  ) {
    throw new BadRequestException(
      'At least one of price and price_iva is required',
    );
  }

  return typedBody;
};
