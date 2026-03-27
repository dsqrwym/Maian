import { IPaginationQueryDto } from '../../utils/dto/pagination.dto';
import { CategorySelectField, CategoryType } from '../category.enums';
import typia, { tags } from 'typia';
import { TagsUuid } from '../../utils/typia/validators/auth.validator';
import { TagsNotBlank } from '../../utils/typia/tags/string.tag';
import { IRequestQueryValidator } from '@nestia/core/src/options/IRequestQueryValidator';
import { cleanString } from '../../utils/string.util';

export interface ICategoryQueryDto extends IPaginationQueryDto {
  search?: string & tags.Example<'Keywords for name search'>; // 用于 name 和 lang 模糊搜索

  langCode?: string & tags.MaxLength<15> & tags.Example<'zh-CH'>; // 用于指定返回 lang 中的哪个字段，不验证因为不影响系统逻辑

  userId?: TagsUuid;

  parentId?: TagsNotBlank;

  maxLevel?: number & tags.Minimum<0> & tags.Maximum<3> & tags.Example<3>;

  withChildrenCount?: boolean;

  type?: CategoryType;

  fields?: CategorySelectField[];
}
export const validateCategoryQuery: IRequestQueryValidator.IAssert<ICategoryQueryDto> =
  {
    type: 'assert',
    assert: (input): ICategoryQueryDto => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));

      return typia.http.assertQuery<ICategoryQueryDto>(input);
    },
  };
