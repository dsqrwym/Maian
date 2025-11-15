import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsEnum, IsString, IsIn } from 'class-validator';
import { UserStatus, UserRole } from '@prisma/client';
import { PaginationQueryDto } from '../../utils/dto/pagination.dto';
import { ToBoolean } from '../../utils/transform/to-boolean.decorator'; // 假设你定义了这两个 enum

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

  @ApiPropertyOptional({
    description: 'return status',
  })
  @IsOptional()
  @ToBoolean()
  selectUserStatus?: boolean;

  @ApiPropertyOptional({
    description: 'return role',
  })
  @IsOptional()
  @ToBoolean()
  selectUserRole?: boolean;

  @ApiPropertyOptional({
    description: 'return user_id',
  })
  @IsOptional()
  @ToBoolean()
  user_id?: boolean;

  @ApiPropertyOptional({
    description: 'return username',
  })
  @IsOptional()
  @ToBoolean()
  username?: boolean;

  @ApiPropertyOptional({
    description: 'return email',
  })
  @IsOptional()
  @ToBoolean()
  email?: boolean;

  @ApiPropertyOptional({
    description: 'return first_name',
  })
  @IsOptional()
  @ToBoolean()
  first_name?: boolean;

  @ApiPropertyOptional({
    description: 'return last_name',
  })
  @IsOptional()
  @ToBoolean()
  last_name?: boolean;

  @ApiPropertyOptional({
    description: 'return telephone',
  })
  @IsOptional()
  @ToBoolean()
  telephone?: boolean;

  @ApiPropertyOptional({
    description: 'return cif',
  })
  @IsOptional()
  @ToBoolean()
  cif?: boolean;

  @ApiPropertyOptional({
    description: 'return profile',
  })
  @IsOptional()
  @ToBoolean()
  profile?: boolean;

  @ApiPropertyOptional({
    description: 'order by field',
  })
  @IsOptional()
  @IsString()
  @IsIn([
    'id',
    'user_id',
    'username',
    'email',
    'first_name',
    'last_name',
    'telephone',
    'cif',
  ])
  orderBy?: string;

  @ApiPropertyOptional({
    description: 'order direction',
  })
  @IsOptional()
  @IsString()
  @IsIn(['asc', 'desc'])
  orderDir?: string;
}
