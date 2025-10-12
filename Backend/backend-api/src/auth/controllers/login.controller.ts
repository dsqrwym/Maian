import { Body, Controller, HttpCode, Post, Req, Res } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBody,
  ApiExtraModels,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { LoginDto } from '../dto/login.dto';
import { TokenResponseDto } from '../dto/token-response.dto';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { REFRESH_TOKEN_COOKIE_PATH } from '../../config/constants.config';
import { AuthService } from '../auth.service';
import { UserRole } from '../../../prisma/generated';

@ApiTags('Authentication')
@ApiExtraModels(LoginDto, TokenResponseDto)
@Controller('login')
export class LoginController {
  constructor(private readonly authService: AuthService) {}

  @Post('standard')
  @HttpCode(200)
  @ApiOperation({ summary: 'Log in a user' })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
      example2: {
        summary: 'Use username instead of email',
        value: {
          username: 'john_doe',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description: 'User successfully logged in',
    type: TokenResponseDto,
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  async loginStandard(@Req() req: FastifyRequest, @Body() body: LoginDto) {
    return await this.authService.login(req, body, [UserRole.RETAILER]);
  }

  @Post('standard/web')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Web login: returns accessToken and sets refresh_token cookie',
    description:
      'For browser-based clients: returns accessToken and sets an httpOnly/secure refresh_token via Set-Cookie.',
  })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
      example2: {
        summary: 'Use username instead of email',
        value: {
          username: 'john_doe',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description:
      'User successfully logged in. Web flow: the response body refreshToken carries the CSRF token; the response header sets the real refresh_token via Set-Cookie.',
    type: TokenResponseDto,
    headers: {
      'Set-Cookie': {
        description: `refresh_token=...; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'; Max-Age=<REFRESH_TOKEN_EXPIRES_IN>`,
        schema: { type: 'string' },
        example: `refresh_token=eyJhbGciOi...; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'; Max-Age=2592000`,
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  async loginStandardWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @Body() body: LoginDto,
  ) {
    return await this.authService.loginWeb(req, res, body, [UserRole.RETAILER]);
  }

  @Post('enterprise')
  @HttpCode(200)
  @ApiOperation({ summary: 'Log in a user' })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
      example2: {
        summary: 'Use username instead of email',
        value: {
          username: 'john_doe',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description: 'User successfully logged in',
    type: TokenResponseDto,
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  async loginEnterprise(@Req() req: FastifyRequest, @Body() body: LoginDto) {
    return await this.authService.login(req, body, [
      UserRole.WHOLESALER,
      UserRole.DELIVERY,
      UserRole.SUPPORT,
      UserRole.WAREHOUSE,
    ]);
  }

  @Post('enterprise/web')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Web login: returns accessToken and sets refresh_token cookie',
    description:
      'For browser-based clients: returns accessToken and sets an httpOnly/secure refresh_token via Set-Cookie.',
  })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
      example2: {
        summary: 'Use username instead of email',
        value: {
          username: 'john_doe',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description:
      'User successfully logged in. Web flow: the response body refreshToken carries the CSRF token; the response header sets the real refresh_token via Set-Cookie.',
    type: TokenResponseDto,
    headers: {
      'Set-Cookie': {
        description: `refresh_token=...; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'; Max-Age=<REFRESH_TOKEN_EXPIRES_IN>`,
        schema: { type: 'string' },
        example: `refresh_token=eyJhbGciOi...; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'; Max-Age=2592000`,
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  async loginEnterpriseWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @Body() body: LoginDto,
  ) {
    return await this.authService.loginWeb(req, res, body, [
      UserRole.WHOLESALER,
      UserRole.DELIVERY,
      UserRole.SUPPORT,
      UserRole.WAREHOUSE,
    ]);
  }

  @Post('admin')
  @HttpCode(200)
  @ApiOperation({ summary: 'Log in a user' })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
      example2: {
        summary: 'Use username instead of email',
        value: {
          username: 'john_doe',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description: 'User successfully logged in',
    type: TokenResponseDto,
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  async loginAdmin(@Req() req: FastifyRequest, @Body() body: LoginDto) {
    return await this.authService.login(req, body, [
      UserRole.ADMIN,
      UserRole.SUPERADMIN,
    ]);
  }

  @Post('admin/web')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Web login: returns accessToken and sets refresh_token cookie',
    description:
      'For browser-based clients: returns accessToken and sets an httpOnly/secure refresh_token via Set-Cookie.',
  })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
      example2: {
        summary: 'Use username instead of email',
        value: {
          username: 'john_doe',
          password: 'StrongPassword123!',
          deviceName: 'CHROME_BROWSER',
          userAgent: 'MOZILLA/5.0 (WINDOWS NT 10.0; WIN64; X64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description:
      'User successfully logged in. Web flow: the response body refreshToken carries the CSRF token; the response header sets the real refresh_token via Set-Cookie.',
    type: TokenResponseDto,
    headers: {
      'Set-Cookie': {
        description: `refresh_token=...; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'; Max-Age=<REFRESH_TOKEN_EXPIRES_IN>`,
        schema: { type: 'string' },
        example: `refresh_token=eyJhbGciOi...; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'; Max-Age=2592000`,
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  async loginAdminWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @Body() body: LoginDto,
  ) {
    return await this.authService.loginWeb(req, res, body, [
      UserRole.ADMIN,
      UserRole.SUPERADMIN,
    ]);
  }
}
