import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import type { CategorySelectField, CategoryType } from '../category.enums.js';
import type { tags } from 'typia';
import typia from 'typia';
import type { TagsUuid } from '#/utils/typia/validators/auth.validator.js';
import type { TagsNotBlank } from '#/utils/typia/tags/string.tag.js';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import { cleanString } from '#/utils/string.util.js';
import type { OrderByEnum } from '#/common/enums/sort.enum.js';
import type { TagsLanguage } from '#/utils/typia/validators/language.validator.js';

export interface ICategoryQueryDto extends IPaginationQueryDto {
  search?: string & tags.Example<'Keywords for name search'>; // 用于 name 和 lang 模糊搜索

  langCode?: TagsLanguage; // 用于指定返回 lang 中的哪个字段，不验证因为不影响系统逻辑

  userId?: TagsUuid;

  parentId?: TagsNotBlank;

  level?: number & tags.Minimum<1> & tags.Maximum<3> & tags.Example<1>;

  maxLevel?: number & tags.Minimum<0> & tags.Maximum<3> & tags.Example<3>;

  withChildrenCount?: boolean;

  includePublic?: boolean;

  onlyWithOwnedChildren?: boolean;

  searchMatchMode: 'self' | 'descendant';

  productFilterMode?: 'self' | 'descendant';

  type?: CategoryType;

  fields?: CategorySelectField[];

  sort_by?: 'level';

  sort_order?: OrderByEnum & tags.Example<'asc'>;
}
export const validateCategoryQueryFunction =
  typia.http.createAssertQuery<ICategoryQueryDto>();
export const validateCategoryQuery: IRequestQueryValidator.IAssert<ICategoryQueryDto> =
  {
    type: 'assert',
    assert: (input): ICategoryQueryDto => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));
      const searchMatchMode = input.get('searchMatchMode');
      // 默认 self mode
      if (!searchMatchMode) input.set('searchMatchMode', 'self');

      return validateCategoryQueryFunction(input);
    },
  };
