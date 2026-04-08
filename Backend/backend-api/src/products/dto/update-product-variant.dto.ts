import { ICreateVariantDto } from './create-product-variant.dto';
import { TagsIntegerString } from '../../utils/typia/tags/string.tag';
import { isObject } from '../../utils/is.util';
import typia from 'typia';
import { BadRequestException } from '@nestjs/common';
import Decimal from 'decimal.js';

export interface IUpdateVariantDto extends ICreateVariantDto {
  id: TagsIntegerString;
}
export const validateIUpdateVariant = (input: unknown, productIva?: string) => {
  if (isObject(input)) {
    if (typeof input.product_code === 'string') {
      input.product_code = input.product_code.trim();
    }
  }
  const typedBody = typia.assertEquals<IUpdateVariantDto>(input);
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
    typedBody.available_stock &&
    typedBody.low_stock_threshold >= typedBody.available_stock
  ) {
    throw new BadRequestException(
      `Low stock threshold ${typedBody.low_stock_threshold} must be small than available stock ${typedBody.available_stock}`,
    );
  }
  return typedBody;
};
