import type { IPaginationQueryDto } from '@/utils/dto/pagination.dto';
import type {
  ProductListSelectField,
  ProductSortField,
} from '../product.enums';
import type { TagsIntegerString } from '@/utils/typia/tags/string.tag';
import type { TagsUuid } from '@/utils/typia/validators/auth.validator';
import type { tags } from 'typia';
import typia from 'typia';
import type { OrderByEnum } from '@/common/enums/sort.enum';
import type { IRequestQueryValidator } from '@nestia/core/src/options/IRequestQueryValidator';
import { cleanString } from '@/utils/string.util';
import type { ProductStatus } from '@/generated/drizzle/enums';

export interface IProductListQueryDto extends IPaginationQueryDto {
  search?: string; // 搜索关键字 (用于 name, title, product_code)

  langCode?: string; // 用于指定返回 lang 中的哪个字段

  category_id?: TagsIntegerString; // 按主分类或关联分类过滤

  wholesaler_id?: TagsUuid; // 零售商想过滤特定批发商的产品 (仅对零售商开放)

  sort_by?: ProductSortField &
    tags.Example<
      ['name', 'product_code', 'available_stock', 'price_iva', 'price']
    >;

  sort_order: OrderByEnum & tags.Example<'asc'>;

  status?: ProductStatus & tags.Example<'ACTIVE'>;

  fields?: ProductListSelectField[] &
    tags.Example<['iva', 'status', 'user_id', 'category']>;
}
export const validateProductListQuery: IRequestQueryValidator.IAssert<IProductListQueryDto> =
  {
    type: 'assert',
    assert: (input): IProductListQueryDto => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));

      return typia.http.assertQuery<IProductListQueryDto>(input);
    },
  };
