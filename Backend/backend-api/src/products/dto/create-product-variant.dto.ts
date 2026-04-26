import type { SaleVariant } from 'src/generated/drizzle/enums';
import { ProductStatus } from 'src/generated/drizzle/enums';
import type { TagsSort } from '@/utils/typia/validators/sort.validator';
import type { tags } from 'typia';
import typia from 'typia';
import type {
  TagsPrice,
  TagsPriceIva,
  TagsProductCode,
} from '@/utils/typia/validators/product.validator';
import type { TagsUInt4 } from '@/utils/typia/tags/number.tags';
import { isObject } from '@/utils/is.utils';
import { BadRequestException } from '@nestjs/common';

export interface ICreateVariantDto {
  // --- 核心销售和定价字段 (variant_products) ---
  type_sale: SaleVariant;

  sort: TagsSort; // 显示顺序，数量越小越靠前

  price: TagsPrice | null; // 不含税价格

  price_iva: TagsPriceIva | null; // 含税价格

  product_code: TagsProductCode; // 变体的编码
  // --- 库存和销售配置字段 (variant_products) ---

  available_stock: TagsUInt4; // 初始库存

  sale_unit_qty: TagsUInt4 & tags.Minimum<1> & tags.Maximum<1000000>; // 换算因子 (例如：1 箱 = 24 件)， 最大一百万应该足够了

  min_order_qty: TagsUInt4 & tags.Minimum<1>; // 最小起订量 (以销售单位计)

  status: ProductStatus;

  low_stock_threshold: TagsUInt4; // 低库存预警阈值 0 = 不开启

  attributes: string | null; // 暂时不用
}
export const validateICreateVariant = (input: unknown) => {
  if (isObject(input)) {
    if (typeof input.product_code === 'string') {
      input.product_code = input.product_code.trim();
    }
    if (!input.status) {
      input.status = ProductStatus.ACTIVE;
    }
    if (!input.low_stock_threshold) {
      input.low_stock_threshold = 0;
    }
  }

  const typedBody = typia.assertEquals<ICreateVariantDto>(input);

  if (!typedBody.price && !typedBody.price_iva) {
    throw new BadRequestException(
      'At least one of price and price_iva is required',
    );
  }

  return typedBody;
};
