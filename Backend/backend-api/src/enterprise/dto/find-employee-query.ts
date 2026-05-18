import type { tags } from 'typia';
import typia from 'typia';
import type { TagsSortOrder } from '#/utils/typia/validators/sort.validator.js';
import type { EmployeeSortByFields } from '#/enterprise/enterprise.enums.js';
import { EmployeeRole } from '#/enterprise/enterprise.enums.js';
import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import { cleanString } from '#/utils/string.util.js';
import { BadRequestException } from '@nestjs/common';
export interface IFindEmployeeQuery extends IPaginationQueryDto {
  search?: string & tags.Example<'Keywords for name search'>;
  role?: EmployeeRole;
  sortBy?: EmployeeSortByFields;
  sortOrder?: TagsSortOrder;
}
export const validateFindEmployeeQueryFunction =
  typia.http.createAssertQuery<IFindEmployeeQuery>();
export const validateFindEmployeeQuery: IRequestQueryValidator.IAssert<IFindEmployeeQuery> =
  {
    type: 'assert',
    assert: (input): IFindEmployeeQuery => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));
      const role = input.get('role');
      if (role && !Object.values(EmployeeRole).includes(role as EmployeeRole)) {
        throw new BadRequestException('Invalid role');
      }
      return validateFindEmployeeQueryFunction(input);
    },
  };
