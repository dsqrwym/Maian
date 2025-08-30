import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsEnum,
  IsOptional,
  IsString,
  MaxLength,
  ValidateIf,
} from 'class-validator';
import { Transform } from 'class-transformer';
import { SpanishCompanyType } from '../auth.types';
import { UserRole } from 'prisma/generated/prisma/client';

export class RegisterProfileDto {
  @ApiProperty({
    description: 'Company legal or trading name',
    maxLength: 100,
    example: 'MAIAN DISTRIBUCIONES SL',
  })
  @IsString()
  @MaxLength(100)
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  companyName: string;

  @ApiProperty({
    description: 'Spanish company type',
    enum: SpanishCompanyType,
    example: 'SL',
  })
  @IsEnum(SpanishCompanyType)
  companyType: SpanishCompanyType;

  @ApiPropertyOptional({
    description: 'License file ID if applicable',
    example: 'file_abc123',
  })
  @IsString()
  @IsOptional()
  licence?: string; // 储存营业执照的文件id

  @ApiPropertyOptional({
    description: 'Company description (required for wholesalers)',
    maxLength: 500,
    example: 'Wholesale distributor focused on fresh produce and beverages.',
  })
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  @ValidateIf((o) => o.role === UserRole.WHOLESALER)
  @IsString()
  @MaxLength(500)
  @IsOptional()
  companyDescription: string;
}
