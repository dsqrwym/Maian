import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import type { tags } from 'typia';
import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import { cleanString } from '#/utils/string.util.js';
import type { WholesalerSortField } from '../user.enums.js';
import type { SpanishCompanyType } from '#/auth/dto/register-wholesaler.dto.js';

import type { TagsSortOrder } from '#/utils/typia/validators/sort.validator.js';

export interface IFindWholesalerQueryDto extends IPaginationQueryDto {
  search?: string;

  delivery_available?: boolean;

  pickup_available?: boolean;

  company_type?: SpanishCompanyType;

  orderBy?: WholesalerSortField & tags.Example<WholesalerSortField>;

  orderDir?: TagsSortOrder;
}

export const validateWholesalerQueryFunction =
  typia.http.createAssertQuery<IFindWholesalerQueryDto>();

export const validateWholesalerQuery: IRequestQueryValidator.IAssert<IFindWholesalerQueryDto> =
  {
    type: 'assert',
    assert: (input): IFindWholesalerQueryDto => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));

      return validateWholesalerQueryFunction(input);
    },
  };
