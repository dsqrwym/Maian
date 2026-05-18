import typia from 'typia';
import { isObject } from '#/utils/is.utils.js';
import { cleanString } from '#/utils/string.util.js';
import parsePhoneNumberFromString from 'libphonenumber-js';
import { BadRequestException } from '@nestjs/common';
import type { IRequestBodyValidator } from '#/utils/typia/typia-type.js';
import { isValidSpanishTaxId } from '#/utils/is-spain-tax-id.js';
import type { ICreateEmployeeDto } from '#/enterprise/dto/create-employee.dto.js';

export type IUpdateEmployeeDto = Partial<Omit<ICreateEmployeeDto, 'email'>>;
export const validateUpdateEmployeeFunction =
  typia.createAssertEquals<IUpdateEmployeeDto>();
export const validateIUpdateEmployee: IRequestBodyValidator.IAssert<IUpdateEmployeeDto> =
  {
    type: 'assert',
    assert: (input: unknown) => {
      if (isObject(input)) {
        const obj = input;
        if (typeof obj.username === 'string') {
          obj.username = cleanString(obj.username);
        }
        if (typeof obj.first_name === 'string') {
          obj.first_name = cleanString(obj.first_name);
        }
        if (typeof obj.last_name === 'string') {
          obj.last_name = cleanString(obj.last_name);
        }
        if (typeof obj.telephone === 'string') {
          const phone: string = cleanString(obj.telephone);
          const phoneNumber = parsePhoneNumberFromString(phone);
          if (!phoneNumber || !phoneNumber.isValid()) {
            throw new BadRequestException('Invalid phone number format');
          }
          obj.telephone = phone;
        }
        if (typeof obj.cif === 'string') {
          obj.cif = cleanString(obj.cif);
        }
      }
      const typedBody = validateUpdateEmployeeFunction(input);
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
