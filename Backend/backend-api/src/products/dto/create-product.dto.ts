import type { ICreateVariantDto } from './create-product-variant.dto';
import { validateICreateVariant } from './create-product-variant.dto';
import type { IProductTranslationDto } from './product-translation.dto';
import { validateProductTranslationDto } from './product-translation.dto';
import type { IProductFileDto } from './product-file.dto';
import type { TagsUuid } from '@/utils/typia/validators/auth.validator';
import type { tags } from 'typia';
import typia from 'typia';
import type {
  TagsIntegerString,
  TagsNotBlank,
} from '@/utils/typia/tags/string.tag';
import type {
  TagsIvaString,
  TagsProductCode,
} from '@/utils/typia/validators/product.validator';
import { cleanString } from '@/utils/string.util';
import { isObject } from '@/utils/is.utils';
import type { IRequestBodyValidator } from '@nestia/core/src/options/IRequestBodyValidator';
import { ProductStatus } from 'src/generated/drizzle/enums';
import { BadRequestException } from '@nestjs/common';

export interface ICreateProductDto {
  user_id: TagsUuid;

  // --- 产品通用信息字段 (products) ---
  name: TagsNotBlank & tags.MaxLength<50> & tags.Example<'Organic Olive Oil'>;

  title:
    | (string &
        tags.MaxLength<100> &
        tags.Example<'Short title of the product'>)
    | null;

  description:
    | (string & tags.Example<'Cold-pressed extra virgin olive oil from Spain.'>)
    | null;

  iva: TagsIvaString; // 产品通用税率

  product_code: TagsProductCode; // 主产品编码

  status: ProductStatus;

  primary_category_id: TagsIntegerString; // 必须选择一个主分类 (对应 product_categories.is_primary = TRUE)
  // (后续添加非主分类)

  // --- 核心业务逻辑字段 (变体) ---
  variants: ICreateVariantDto[] & tags.MinItems<1>;

  // --- 可选关联字段 (翻译和文件) ---
  translations: IProductTranslationDto[] | null;

  files: IProductFileDto[] | null;
}
export const validateICreateProduct: IRequestBodyValidator.IAssert<ICreateProductDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.name === 'string') {
          input.name = cleanString(input.name);
        }
        if (typeof input.title === 'string') {
          input.title = cleanString(input.title);
        }
        if (!input.status) {
          input.status = ProductStatus.ACTIVE;
        }
        if (Array.isArray(input.variants)) {
          input.variants = input.variants.map((it: unknown) =>
            validateICreateVariant(it),
          );
        }
        if (Array.isArray(input.translations)) {
          input.translations = input.translations.map(
            validateProductTranslationDto,
          );
        }
      }
      const body = typia.assertEquals<ICreateProductDto>(input);

      if (body.translations) {
        const codes = body?.translations.map((t) => t.lang_code);
        if (new Set(codes).size !== codes.length) {
          throw new BadRequestException('Duplicate lang_code in translations');
        }
      }

      return body;
    },
  };
