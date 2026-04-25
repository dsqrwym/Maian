import { IPaginationQueryDto } from '@/utils/dto/pagination.dto';
import { OrderByEnum } from '@/common/enums/sort.enum';
import typia, { tags } from 'typia';
import { IRequestQueryValidator } from '@nestia/core/src/options/IRequestQueryValidator';
import { cleanString } from '@/utils/string.util';
import { UserRole, UserStatus } from '@/generated/drizzle/enums';

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

  orderDir?: OrderByEnum & tags.Example<'asc'>;
}

export const validateFindUserQuery: IRequestQueryValidator.IAssert<IFindUserQueryDto> =
  {
    type: 'assert',
    assert: (input): IFindUserQueryDto => {
      const search = input.get('search');
      if (search) input.set('search', cleanString(search));

      return typia.http.assertQuery<IFindUserQueryDto>(input);
    },
  };
