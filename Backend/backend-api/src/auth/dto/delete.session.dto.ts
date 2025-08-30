import { ApiProperty } from '@nestjs/swagger';

export class DeleteSessionDto {
  @ApiProperty({
    description: 'The ID of the session to delete',
    example: '1e2d3c4b-5a6f-7890-abcd-ef1234567890',
  })
  sessionId: string;

  @ApiProperty({
    description: 'User password to confirm the action',
    example: 'StrongPassword123!',
  })
  password: string;
}
