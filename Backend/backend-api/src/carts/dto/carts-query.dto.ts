import type { TagsUuid } from '#/utils/typia/validators/auth.validator.js';
import type { TagsLanguage } from '#/utils/typia/validators/language.validator.js';
import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
export interface ICartsQueryDto {
  langCode?: TagsLanguage; // 用于指定返回 lang 中的哪个字段，不验证因为不影响系统逻辑
  wholesaler_id?: TagsUuid;
}
export const validateICartsQueryDtoFunction =
  typia.http.createAssertQuery<ICartsQueryDto>();
export const validateICartsQueryDto: IRequestQueryValidator.IAssert<ICartsQueryDto> =
  {
    type: 'assert',
    assert: (input: URLSearchParams) => {
      return validateICartsQueryDtoFunction(input);
    },
  };
