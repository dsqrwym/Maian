import { BadRequestException } from '@nestjs/common';
import parsePhoneNumberFromString from 'libphonenumber-js';
import typia from 'typia';

import type { IRetailerProfile } from '#/user/type/retailer-profile.type.js';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import { isObject } from '#/utils/is.utils.js';
import { isValidSpanishTaxId } from '#/utils/is-spain-tax-id.js';
import { cleanString } from '#/utils/string.util.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import type { TagsUsername } from '#/utils/typia/validators/auth.validator.js';
import type { TagsBasicTelephone } from '#/utils/typia/validators/telephone.validator.js';
import type {
  TagsFirstName,
  TagsLastName,
  TagsTaxId,
} from '#/utils/typia/validators/user.validator.js';

export interface IUpdateRetailerProfileDto extends Partial<IRetailerProfile> {
  first_name?: TagsFirstName | null;

  last_name?: TagsLastName | null;

  /**
   * Company logo file id.
   * 商家 Logo 文件 ID。关联 files.id。
   *
   */
  profile_image_file_id?: TagsIntegerString | null;

  /**
   * Spanish tax/person/business identifier basic format.
   * 西班牙税号/身份号基础格式。
   *
   * 支持：
   * - DNI/NIF: 12345678Z
   * - NIE: X1234567L / Y1234567X / Z1234567R
   * - CIF / business NIF: B12345678 / A12345678 / J1234567A / P1234567A
   *
   * 适合兼容：
   * - autónomo / 个体户：DNI 或 NIE
   * - empresa / 大公司、小公司、协会等：CIF / business NIF
   *
   * 仅做格式校验，不做控制码算法校验。
   */
  tax_id?: TagsTaxId | null;

  username?: TagsUsername | null;

  telephone?: TagsBasicTelephone | null;
}

export const validateUpdateRetailerProfileDtoFunction =
  typia.createAssertEquals<IUpdateRetailerProfileDto>();
export const validateUpdateRetailerProfile: IRequestBodyValidator.IAssert<IUpdateRetailerProfileDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        if (typeof input.first_name === 'string') {
          input.first_name = cleanString(input.first_name);
        }
        if (typeof input.last_name === 'string') {
          input.last_name = cleanString(input.last_name);
        }
        if (typeof input.username === 'string') {
          input.username = cleanString(input.username);
        }
        if (typeof input.company_name === 'string') {
          input.company_name = cleanString(input.company_name);
        }
        if (typeof input.display_name === 'string') {
          input.display_name = cleanString(input.display_name);
        }
        if (typeof input.contact_name === 'string') {
          input.contact_name = cleanString(input.contact_name);
        }
        if (typeof input.tax_id === 'string') {
          input.tax_id = cleanString(input.tax_id).toUpperCase();
        }
        if (typeof input.telephone === 'string') {
          input.telephone = cleanString(input.telephone);
        }
      }
      const typedBody = validateUpdateRetailerProfileDtoFunction(input);

      if (typedBody.tax_id) {
        if (isValidSpanishTaxId(typedBody.tax_id) === null) {
          throw new BadRequestException('Invalid Spain DNI/NIE/NIF format');
        }
      }

      if (typedBody.telephone) {
        const phoneNumber = parsePhoneNumberFromString(typedBody.telephone);
        if (!phoneNumber || !phoneNumber.isValid()) {
          throw new BadRequestException('Invalid phone number format');
        }
      }

      return typedBody;
    },
  };
