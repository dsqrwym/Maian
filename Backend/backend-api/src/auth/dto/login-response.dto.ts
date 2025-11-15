import { ApiProperty, IntersectionType } from '@nestjs/swagger';
import { TokenResponseDto } from './token-response.dto';
import { UserPayload } from '../auth.types';

class UserPayloadDto {
  @ApiProperty({ description: 'User payload' })
  user: UserPayload;
}

export class LoginResponseDto extends IntersectionType(
  TokenResponseDto,
  UserPayloadDto,
) {}
