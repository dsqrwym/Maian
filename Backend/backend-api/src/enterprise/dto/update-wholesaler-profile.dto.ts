import type { IWholesalerProfile } from '#/enterprise/types/IWholesalerProfile.js';
import type { TagsUsername } from '#/utils/typia/validators/auth.validator.js';
import type { TagsBasicTelephone } from '#/utils/typia/validators/telephone.validator.js';
import type {
  TagsFirstName,
  TagsLastName,
  TagsTaxId,
} from '#/utils/typia/validators/user.validator.js';
import parsePhoneNumberFromString from 'libphonenumber-js';
import { BadRequestException } from '@nestjs/common';
import typia from 'typia';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';
import { isValidSpanishTaxId } from '#/utils/is-spain-tax-id.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';

export interface IUpdateWholesalerProfileDto extends Partial<IWholesalerProfile> {
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

  /**
   * Username (optional)
   * 用户名（可选）
   */
  username?: TagsUsername | null;

  /**
   * Telephone number
   * 联系电话
   */
  telephone?: TagsBasicTelephone | null;
}

export const validateUpdateWholesalerProfileDtoFunction =
  typia.createAssertEquals<IUpdateWholesalerProfileDto>();
export const validateUpdateWholesalerProfile: IRequestBodyValidator.IAssert<IUpdateWholesalerProfileDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        const obj = input;
        if (typeof obj.first_name === 'string') {
          obj.first_name = cleanString(obj.first_name);
        }
        if (typeof obj.last_name === 'string') {
          obj.last_name = cleanString(obj.last_name);
        }
        if (typeof obj.username === 'string') {
          obj.username = cleanString(obj.username);
        }
        if (typeof obj.company_name === 'string') {
          obj.company_name = cleanString(obj.company_name);
        }
        if (typeof obj.display_name === 'string') {
          obj.display_name = cleanString(obj.display_name);
        }
        if (typeof obj.description === 'string') {
          obj.description = cleanString(obj.description);
        }
        if (typeof obj.delivery_area_description === 'string') {
          obj.delivery_area_description = cleanString(
            obj.delivery_area_description,
          );
        }
        if (typeof obj.tax_id === 'string') {
          obj.tax_id = cleanString(obj.tax_id).toUpperCase();
        }
        if (typeof obj.telephone === 'string') {
          obj.telephone = cleanString(obj.telephone);
        }
      }
      const typedBody = validateUpdateWholesalerProfileDtoFunction(input);

      if (typedBody.tax_id) {
        if (isValidSpanishTaxId(typedBody.tax_id) === null)
          throw new BadRequestException('Invalid Spain DNI/NIE/NIF format');
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
