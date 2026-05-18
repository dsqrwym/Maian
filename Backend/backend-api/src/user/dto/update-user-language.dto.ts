import typia from 'typia';
import type { TagsBCP47Language } from '#/utils/typia/validators/language.validator.js';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';

export interface IUpdateUserLanguageDto {
  language: TagsBCP47Language;
}

export const validateUpdateUserLanguageFunction =
  typia.createAssertEquals<IUpdateUserLanguageDto>();

export const validateUpdateUserLanguage: IRequestBodyValidator.IAssert<IUpdateUserLanguageDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input) && typeof input.language === 'string') {
        input.language = cleanString(input.language);
      }

      return validateUpdateUserLanguageFunction(input);
    },
  };
