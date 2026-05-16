import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import { cleanString } from '#/utils/string.util.js';
import type { UserRole, UserStatus } from '#/generated/drizzle/enums.js';

import type { TagsSortOrder } from '#/utils/typia/validators/sort.validator.js';

export interface IFindUserQueryDto extends IPaginationQueryDto {
  search?: string;

  role?: UserRole;

  status?: UserStatus;

  selectUserStatus?: boolean;

  selectUserRole?: boolean;

  user_id?: boolean;

  username?: boolean;

  email?: boolean;

  first_name?: boolean;

  last_name?: boolean;

  telephone?: boolean;

  cif?: boolean;

  profile?: boolean;

  orderBy?:
    | 'id'
    | 'user_id'
    | 'username'
    | 'email'
    | 'first_name'
    | 'last_name'
    | 'telephone'
    | 'cif';

  orderDir?: TagsSortOrder;
}

export const validateFindUserQueryFunction =
  typia.http.createAssertQuery<IFindUserQueryDto>();

export const validateFindUserQuery: IRequestQueryValidator.IAssert<IFindUserQueryDto> =
  {
    type: 'assert',
    assert: (input): IFindUserQueryDto => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));

      return validateFindUserQueryFunction(input);
    },
  };
