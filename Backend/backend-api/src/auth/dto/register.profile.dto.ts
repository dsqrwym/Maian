import { ApiProperty } from '@nestjs/swagger';
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
  // 批发商和零售商的个人资料设置
  @IsString()
  @MaxLength(100)
  @Transform(({ value }) =>
    // eslint-disable-next-line @typescript-eslint/no-unsafe-return
    typeof value === 'string' ? value.toUpperCase() : value,
  )
  companyName: string;

  @IsEnum(SpanishCompanyType)
  companyType: SpanishCompanyType;

  @IsString()
  @IsOptional()
  licence?: string; // 储存营业执照的文件id

  // 批发商的个人资料设置
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  @ValidateIf((o) => o.role === UserRole.WHOLESALER)
  @IsString()
  @MaxLength(500)
  @IsOptional()
  companyDescription: string;
}
