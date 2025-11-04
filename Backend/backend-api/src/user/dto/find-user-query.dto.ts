import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsEnum, IsString } from 'class-validator';
import { UserStatus, UserRole } from '@prisma/client';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto'; // 假设你定义了这两个 enum

export class FindUserQueryDto extends PaginationQueryDto {
  @ApiProperty({ description: 'Keywords for name search', required: false })
  @IsString()
  @IsOptional()
  search?: string;

  @ApiPropertyOptional({
    description: '（admin, retailer, wholesaler ）',
  })
  @IsOptional()
  @IsEnum(UserRole)
  role?: UserRole;

  @ApiPropertyOptional({
    description: '（ACTIVE, PENDING_VERIFICATION ）',
  })
  @IsOptional()
  @IsEnum(UserStatus)
  status?: UserStatus;
}
