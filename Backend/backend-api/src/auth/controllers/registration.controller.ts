import { Body, Controller, Post } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBody,
  ApiCreatedResponse,
  ApiExtraModels,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { RegisterDto } from '../dto/register.dto';
import { AUTH_ERROR } from '../auth.constants';
import { maskEmail } from '../../common/formatter/emial-format';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';

@Controller('registration')
@ApiTags('Registration')
@ApiExtraModels(RegisterDto)
export class RegistrationController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}
  @Post('')
  @ApiOperation({ summary: 'Register new user' })
  @ApiBody({
    description: 'User registration payload',
    type: RegisterDto,
    examples: {
      minimal: {
        summary: 'Minimum required fields',
        value: {
          email: 'new.user@domain.com',
          password: 'SecurePass123',
        },
      },
      full: {
        summary: 'Full payload with optional fields',
        value: {
          email: 'retailer@domain.com',
          password: 'SecurePass123',
          username: 'retailer_01',
          firstName: 'JOHN',
          lastName: 'SMITH',
          cif: 'X1234567L',
          phone: '+34123456789',
          status: 0,
          role: 0,
          language: 'es-ES',
          timezone: 'Europe/Madrid',
          address: [
            {
              country: 'ES',
              state: 'MADRID',
              city: 'MADRID',
              street: 'Calle Mayor 1',
              postalCode: '28013',
            },
          ],
          profile: {
            type: 'RETAILER',
            document: 'B12345678',
            company: 'MY SHOP SL',
          },
        },
      },
    },
  })
  @ApiCreatedResponse({
    description: 'User successfully registered',
    schema: {
      type: 'object',
      properties: {
        id: { type: 'string', example: 'a1b2c3d4-e5f6-7890-abcd-ef1234567890' },
        email: { type: 'string', example: 'retailer@domain.com' },
        username: { type: 'string', nullable: true, example: 'retailer_01' },
        first_name: { type: 'string', nullable: true, example: 'JOHN' },
        last_name: { type: 'string', nullable: true, example: 'SMITH' },
        telephone: { type: 'string', nullable: true, example: '+34123456789' },
        role: { type: 'number', example: 0 },
        profile: {
          type: 'object',
          nullable: true,
          example: {
            type: 'RETAILER',
            document: 'B12345678',
            company: 'MY SHOP SL',
          },
        },
      },
    },
  })
  @ApiBadRequestResponse({
    description: 'Invalid input data or conflicts',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 400 },
            message: {
              type: 'string',
              description: 'Error code',
            },
            error: { type: 'string', example: 'Bad Request' },
          },
        },
        examples: {
          emailConflict: {
            summary: 'Email already exists',
            value: {
              statusCode: 400,
              message: AUTH_ERROR.EMAIL_CONFLICT,
              error: 'Bad Request',
            },
          },
          usernameConflict: {
            summary: 'Username already exists',
            value: {
              statusCode: 400,
              message: AUTH_ERROR.USERNAME_CONFLICT,
              error: 'Bad Request',
            },
          },
        },
      },
    },
  })
  async register(@Body() body: RegisterDto) {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] register',
    );
    return this.authService.register(body);
  }
}
