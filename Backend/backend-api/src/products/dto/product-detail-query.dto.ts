import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import type { TagsLanguage } from '#/utils/typia/validators/language.validator.js';

export interface IProductDetailQueryDto {
  langCode: TagsLanguage;
}

const assertProductDetailQuery =
  typia.http.createAssertQuery<IProductDetailQueryDto>();

export const validateProductDetailQuery: IRequestQueryValidator.IAssert<IProductDetailQueryDto> =
  {
    type: 'assert',
    assert: (input) => assertProductDetailQuery(input),
  };
