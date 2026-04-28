import { ApiProperty } from '@nestjs/swagger';
import { tags } from 'typia';

export interface IPaginationQueryDto {
  page: number & tags.Minimum<1>;

  limit: number & tags.Minimum<1>;
}

/**
 * Pagination metadata for response
 */
export class PaginationMetaDto {
  @ApiProperty({ description: 'Total number of items' })
  total: number;

  @ApiProperty({ description: 'Current page number' })
  page: number;

  @ApiProperty({ description: 'Number of items per page' })
  limit: number;
}

/**
 * Paginated response structure
 */
export class PaginatedResponseDto<T> {
  @ApiProperty({ isArray: true, description: 'Items for the current page' })
  items: T[];

  @ApiProperty({ type: PaginationMetaDto, description: 'Pagination metadata' })
  pagination: PaginationMetaDto;
}
