import type { TagsSortOrder } from '#/utils/typia/validators/sort.validator.js';
import type { TagsDate } from '#/utils/typia/validators/date.validator.js';
import type { OrderStatus } from '#/generated/drizzle/enums.js';
import type { IPaginationQueryDto } from '#/utils/dto/pagination.dto.js';
import type { OrderSortByEnums } from '../order.enums.js';
import type { tags } from 'typia';
import typia from 'typia';
import type { IRequestQueryValidator } from '#/utils/typia/typia-type.js';
import { cleanString } from '#/utils/string.util.js';
import { BadRequestException } from '@nestjs/common';
import type { TagsLanguage } from '#/utils/typia/validators/language.validator.js';
import type { TagsUuid } from '#/utils/typia/validators/auth.validator.js';

export interface IOrderQuery extends IPaginationQueryDto {
  search?: string;
  wholesalerId?: TagsUuid;
  status?: OrderStatus;
  startDate?: TagsDate;
  endDate?: TagsDate;
  sortBy?: OrderSortByEnums;
  orderBy?: TagsSortOrder;
  minTotalPrice?: number & tags.Minimum<0>;
  maxTotalPrice?: number & tags.Minimum<0>;
  minSubtotal?: number & tags.Minimum<0>;
  maxSubtotal?: number & tags.Minimum<0>;
  minTotalIva?: number & tags.Minimum<0>;
  maxTotalIva?: number & tags.Minimum<0>;
  minItemCount?: number & tags.Minimum<0>;
  maxItemCount?: number & tags.Minimum<0>;
}

export const validateOrderQueryFunction =
  typia.http.createAssertQuery<IOrderQuery>();
export const validateOrderQuery: IRequestQueryValidator.IAssert<IOrderQuery> = {
  type: 'assert',
  assert: (input): IOrderQuery => {
    const search = input.get('search');
    if (search) input.set('search', cleanString(search));
    const body = validateOrderQueryFunction(input);
    if (body.startDate && body.endDate) {
      if (new Date(body.startDate) > new Date(body.endDate)) {
        throw new BadRequestException('Start date must be less than end date');
      }
    }
    if (body.minTotalPrice !== undefined && body.maxTotalPrice !== undefined) {
      if (body.minTotalPrice > body.maxTotalPrice) {
        throw new BadRequestException(
          'Min total price must be less than max total price',
        );
      }
    }
    if (body.minSubtotal !== undefined && body.maxSubtotal !== undefined) {
      if (body.minSubtotal > body.maxSubtotal) {
        throw new BadRequestException(
          'Min subtotal must be less than max subtotal',
        );
      }
    }
    if (body.minTotalIva !== undefined && body.maxTotalIva !== undefined) {
      if (body.minTotalIva > body.maxTotalIva) {
        throw new BadRequestException(
          'Min total IVA must be less than max total IVA',
        );
      }
    }
    if (body.minItemCount !== undefined && body.maxItemCount !== undefined) {
      if (body.minItemCount > body.maxItemCount) {
        throw new BadRequestException(
          'Min item count must be less than max item count',
        );
      }
    }

    return body;
  },
};

export interface IOrderDetailQuery {
  langCode: TagsLanguage;
}
export const validateOrderDetailQueryFunction =
  typia.http.createAssertQuery<IOrderDetailQuery>();
export const validateOrderDetailQuery: IRequestQueryValidator.IAssert<IOrderDetailQuery> =
  {
    type: 'assert',
    assert: (input): IOrderDetailQuery => {
      return validateOrderDetailQueryFunction(input);
    },
  };
