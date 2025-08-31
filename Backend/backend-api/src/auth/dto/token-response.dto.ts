import { ApiProperty } from '@nestjs/swagger';

export class TokenResponseDto {
  @ApiProperty({ description: '短期访问令牌，用于访问受保护资源' })
  accessToken: string;

  @ApiProperty({
    description: '刷新令牌也可能是csrfToken',
  })
  refreshToken: string;
}
