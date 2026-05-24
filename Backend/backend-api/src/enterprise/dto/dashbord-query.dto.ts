import type { TagsDate } from '#/utils/typia/validators/date.validator.js';
import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import { BadRequestException } from '@nestjs/common';

export interface IDashboardQuery {
  startDate?: TagsDate;
  endDate?: TagsDate;
  topLimit: number;
}

export const validateDashboardQueryFunction =
  typia.http.createAssertQuery<IDashboardQuery>();
export const validateDashboardQuery: IRequestQueryValidator.IAssert<IDashboardQuery> =
  {
    type: 'assert',
    assert: (input): IDashboardQuery => {
      if (!input.get('topLimit')) {
        input.set('topLimit', '5');
      }
      const body = validateDashboardQueryFunction(input);
      if (
        body.startDate !== undefined &&
        body.endDate !== undefined &&
        new Date(body.startDate) > new Date(body.endDate)
      ) {
        throw new BadRequestException('Start date must be less than end date');
      }
      if (body.topLimit < 1 || body.topLimit > 20) {
        throw new BadRequestException('topLimit must be between 1 and 20');
      }
      return body;
    },
  };
