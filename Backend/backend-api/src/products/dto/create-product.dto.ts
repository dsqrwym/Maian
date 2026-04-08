import {
  ICreateVariantDto,
  validateICreateVariant,
} from './create-product-variant.dto';
import {
  IProductTranslationDto,
  validateProductTranslationDto,
} from './product-translation.dto';
import { IProductFileDto } from './product-file.dto';
import { TagsUuid } from '../../utils/typia/validators/auth.validator';
import typia, { tags } from 'typia';
import {
  TagsIntegerString,
  TagsNotBlank,
} from '../../utils/typia/tags/string.tag';
import {
  TagsIvaString,
  TagsProductCode,
} from '../../utils/typia/validators/product.validator';
import { cleanString } from '../../utils/string.util';
import { isObject } from '../../utils/is.util';
import { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';

export interface ICreateProductDto {
  user_id: TagsUuid;

  // --- 产品通用信息字段 (products) ---
  name: TagsNotBlank & tags.MaxLength<50> & tags.Example<'Organic Olive Oil'>;

  title?: string &
    tags.MaxLength<100> &
    tags.Example<'Short title of the product'>;

  description?: string &
    tags.Example<'Cold-pressed extra virgin olive oil from Spain.'>;

  iva: TagsIvaString; // 产品通用税率

  product_code: TagsProductCode; // 主产品编码

  primary_category_id: TagsIntegerString; // 必须选择一个主分类 (对应 product_categories.is_primary = TRUE)
  // (后续添加非主分类)

  // --- 核心业务逻辑字段 (变体) ---
  variants: ICreateVariantDto[] & tags.MinItems<1>;

  // --- 可选关联字段 (翻译和文件) ---
  translations?: IProductTranslationDto[];

  files?: IProductFileDto[];
}
export const validateICreateProduct: IRequestBodyValidator.IAssert<ICreateProductDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.name === 'string')
          input.name = cleanString(input.name);
        if (typeof input.title === 'string')
          input.title = cleanString(input.title);
        if (Array.isArray(input.variants) && typeof input.iva === 'string') {
          input.variants = input.variants.map((it: unknown) =>
            validateICreateVariant(it, input.iva as string),
          );
        }
        if (Array.isArray(input.translations)) {
          input.translations = input.translations.map(
            validateProductTranslationDto,
          );
        }
      }
      return typia.assertEquals<ICreateProductDto>(input);
    },
  };
