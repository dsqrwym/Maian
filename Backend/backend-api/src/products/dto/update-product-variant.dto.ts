import { ICreateVariantDto } from './create-product-variant.dto';
import { TagsIntegerString } from '../../utils/typia/tags/string.tag';
import { isObject } from '../../utils/is.util';
import typia from 'typia';
import { BadRequestException } from '@nestjs/common';

export interface IUpdateVariantDto extends Partial<ICreateVariantDto> {
  id: TagsIntegerString;
}
export const validateIUpdateVariant = (input: unknown) => {
  if (isObject(input)) {
    if (typeof input.product_code === 'string') {
      input.product_code = input.product_code.trim();
    }
  }
  const typedBody = typia.assertEquals<IUpdateVariantDto>(input);
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
