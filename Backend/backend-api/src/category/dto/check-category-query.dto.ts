import type { TagsIntegerString } from '@/utils/typia/tags/string.tag';
import type { tags } from 'typia';
import typia from 'typia';
import type { TagsUuid } from '@/utils/typia/validators/auth.validator';
import type { IRequestQueryValidator } from '@nestia/core/src/options/IRequestQueryValidator';
import { cleanString } from '@/utils/string.util';

/**
 * DTO for checking category name availability when creating
 */
export interface ICheckCategoryNameCreateQueryDto {
  name: string &
    tags.MinLength<1> &
    tags.MaxLength<50> &
    tags.Example<'Electronics'>;

  userId?: string & TagsUuid;
}
export const validateCheckCategoryNameCreateQuery: IRequestQueryValidator.IAssert<ICheckCategoryNameCreateQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckCategoryNameCreateQueryDto => {
      const name = input.get('name');
      if (name) {
        input.set('name', cleanString(name));
      }
      return typia.http.assertQuery<ICheckCategoryNameCreateQueryDto>(input);
    },
  };

/**
 * DTO for checking category name availability when updating
 */
export interface ICheckCategoryNameUpdateQueryDto extends ICheckCategoryNameCreateQueryDto {
  id: TagsIntegerString;
}
export const validateCheckCategoryNameUpdateQuery: IRequestQueryValidator.IAssert<ICheckCategoryNameUpdateQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams): ICheckCategoryNameUpdateQueryDto => {
      const name = input.get('name');
      if (name) {
        input.set('name', cleanString(name));
      }
      return typia.http.assertQuery<ICheckCategoryNameUpdateQueryDto>(input);
    },
  };
