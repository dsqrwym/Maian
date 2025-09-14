import {
  Controller,
  Post,
  Get,
  Query,
  Body,
  Res,
  UseGuards,
  Req,
  BadRequestException,
  UnauthorizedException,
  HttpCode,
  Delete,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/register.dto';
import {
  ApiBearerAuth,
  ApiBody,
  ApiExtraModels,
  ApiOperation,
  ApiQuery,
  ApiResponse,
  ApiTags,
  ApiBadRequestResponse,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiUnauthorizedResponse,
  ApiCookieAuth,
  ApiNotFoundResponse,
  ApiTooManyRequestsResponse,
} from '@nestjs/swagger';
import { FastifyReply, FastifyRequest } from 'fastify';
import { LoginDto } from './dto/login.dto';
import { TokenResponseDto } from './dto/token-response.dto';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { JwtAuthGuard, LocalAuthGuard } from './guard/auth.guard';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { ConfigService } from '@nestjs/config';
import {
  ENV,
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from 'src/config/constants.config';
import { AUTH_ERROR } from './auth.constants';
import { JwtService } from '@nestjs/jwt';
import { CSRFPayload } from './auth.types';
import {
  ResetPasswordDto,
  SendVerificationCodeDto,
  VerifyCodeDto,
} from './dto/reset-password.dto';
import { Logger } from 'nestjs-pino';
import { seconds, Throttle } from '@nestjs/throttler';
import { maskEmail } from '../common/formatter/emial-format';
import { VerifyCodeResponseDto } from './dto/reset-password-response.dto';

@ApiTags('Authentication')
@Controller('auth')
@ApiExtraModels(RegisterDto, LoginDto, DeleteSessionDto, RefreshTokenDto)
export class AuthController {
  private static ACCESS_TOKEN_TTL = Number(
    process.env[ENV.ACCESS_TOKEN_EXPIRES_IN],
  );
  constructor(
    private readonly authService: AuthService,
    private readonly configService: ConfigService,
    private readonly jwtService: JwtService,
    private readonly logger: Logger,
  ) {}

  @Get('verify-email')
  @ApiOperation({ summary: 'Verify email address' })
  @ApiQuery({
    name: 'token',
    required: true,
    description: 'JWT token from email',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @ApiQuery({
    name: 'lang',
    required: false,
    description: 'Language code (default: en)',
    example: 'en',
  })
  @ApiResponse({
    status: 200,
    description: 'Returns HTML verification result page',
    content: {
      'text/html': {
        schema: {
          type: 'string',
          example:
            '<html lang="en"><body>Email verified successfully.</body></html>',
        },
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid or expired token' })
  async getVerifyEmail(
    @Query('lang') lang: string,
    @Query('token') token: string,
    @Res() res: FastifyReply,
  ) {
    this.logger.debug({ lang }, '[AuthController] verify-email request');
    return this.authService.verifyEmail(token, lang, res);
  }

  @Post('register')
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

  @Post('login')
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
  @UseGuards(LocalAuthGuard)
  async login(@Req() req: FastifyRequest, @Body() body: LoginDto) {
    const user = req.user.authenticatedUser;
    if (!user) {
      this.logger.warn({ ip: req.ip }, '[AuthController] login user missing');
      throw new BadRequestException(AUTH_ERROR.NO_AUTH_PAYLOAD);
    }

    this.logger.debug(
      { userId: user.id, ip: req.ip, device: body.deviceName },
      '[AuthController] login',
    );
    const { token } = await this.authService.login(req, user, body);
    return token;
  }

  @Post('login-web')
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
  @UseGuards(LocalAuthGuard)
  async loginWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @Body() body: LoginDto,
  ) {
    const user = req.user.authenticatedUser;
    if (!user) {
      this.logger.warn(
        { ip: req.ip },
        '[AuthController] login-web user missing',
      );
      throw new BadRequestException(AUTH_ERROR.NO_AUTH_PAYLOAD);
    }

    this.logger.debug(
      { userId: user.id, ip: req.ip, device: body.deviceName },
      '[AuthController] login-web',
    );

    const { token, payload } = await this.authService.login(req, user, body);
    // Web: 设置 cookie（httpOnly, secure, sameSite）
    res.setCookie(REFRESH_COOKIE_NAME, token.refreshToken, {
      httpOnly: true,
      secure: true,
      sameSite: 'none', // 跨域前后端分离（不同子域 / 不同域名）
      path: REFRESH_TOKEN_COOKIE_PATH,
      maxAge: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
    });

    this.logger.debug(
      { userId: user.id, sessionId: payload.sessionId },
      '[AuthController] login-web set refresh cookie',
    );

    const csrfTokenPayload: CSRFPayload = {
      sessionId: payload.sessionId,
      deviceFinger: payload.deviceFinger,
    };

    // 从Cookie 中读取到 refresh token，并将新的 refresh token 回写到 Cookie（轮换）

    const csrfToken = await this.jwtService.signAsync(csrfTokenPayload, {
      expiresIn: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
      secret: this.configService.get(ENV.CSRF_TOKEN_SECRET),
    });

    const result: TokenResponseDto = {
      accessToken: token.accessToken,
      refreshToken: csrfToken,
    };

    return result;
  }

  @Post('refresh-token')
  @HttpCode(200)
  @Throttle({
    default: { limit: 1, ttl: seconds(AuthController.ACCESS_TOKEN_TTL) },
  })
  @ApiOperation({
    summary: 'Refresh tokens (body-only)',
    description:
      'Non-web clients: provide { refreshToken } in the request body. Cookies are not involved.',
  })
  @ApiBody({
    description:
      'For non-browser clients, provide refreshToken in the request body.',
    type: RefreshTokenDto,
    examples: {
      bodyExample: {
        summary: 'Provide refreshToken in request body',
        value: { refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Returns new accessToken and refreshToken.',
    type: TokenResponseDto,
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized. See examples for possible error codes.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              description: 'Error code (for frontend handling)',
              example: AUTH_ERROR.SESSION_NOT_FOUND,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
        examples: {
          sessionNotFound: {
            summary: 'Session not found/kicked/expired/refresh invalid',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_NOT_FOUND,
              error: 'Unauthorized',
            },
          },
          sessionRevoked: {
            summary: 'Session revoked (user logout)',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_REVOKED,
              error: 'Unauthorized',
            },
          },
          invalidRefresh: {
            summary: 'Refresh token invalid (possible reuse/mismatch)',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.INVALID_REFRESH_TOKEN,
              error: 'Unauthorized',
            },
          },
        },
      },
    },
  })
  async getAccessToken(@Body() body: RefreshTokenDto) {
    const refreshToken = body?.refreshToken;
    if (!refreshToken) {
      this.logger.warn({}, '[AuthController] refresh-token missing');
      throw new BadRequestException(AUTH_ERROR.NO_REFRESH_TOKEN);
    }

    this.logger.debug({}, '[AuthController] refresh-token');
    const result = await this.authService.getAccessToken(refreshToken);
    return result.token;
  }

  @Post('refresh-token-web')
  @HttpCode(200)
  @Throttle({
    default: { limit: 1, ttl: seconds(AuthController.ACCESS_TOKEN_TTL) },
  })
  @ApiOperation({
    summary:
      'Web refresh: Cookie + CSRF (Body.refreshToken) with refresh_token rotation',
    description:
      'Browser flow: read the real refresh_token from cookies; the request body refreshToken carries the CSRF token (bound to the session). After validation, a new refresh_token is set via Set-Cookie (rotation) and the response body returns a new accessToken and new CSRF token (still in refreshToken field).',
  })
  @ApiBody({
    description:
      'Web refresh must pass the CSRF token in Body.refreshToken. The real refresh_token is sent automatically via Cookie (name: refresh_token).',
    type: RefreshTokenDto,
    examples: {
      webRefresh: {
        summary:
          'Web refresh (cookie carries refresh_token; body carries CSRF)',
        value: { refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.CSRF...' },
      },
    },
  })
  @ApiOkResponse({
    description:
      'Returns new accessToken and refreshToken. If using cookies, a rotated refresh_token is set via Set-Cookie.',
    type: TokenResponseDto,
    headers: {
      'Set-Cookie': {
        description: `When cookies are included, a new refresh_token is returned; HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'`,
        schema: { type: 'string' },
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized. See examples for possible error codes.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              description: 'Error code (for frontend handling)',
              example: AUTH_ERROR.SESSION_NOT_FOUND,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
        examples: {
          csrfInvalid: {
            summary: 'CSRF verification failed',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.CSRF_INVALID,
              error: 'Unauthorized',
            },
          },
          sessionNotFound: {
            summary: 'Session not found/kicked/expired/refresh invalid',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_NOT_FOUND,
              error: 'Unauthorized',
            },
          },
          sessionRevoked: {
            summary: 'Session revoked (user logout)',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_REVOKED,
              error: 'Unauthorized',
            },
          },
          invalidRefresh: {
            summary: 'Refresh token invalid (possible reuse/mismatch)',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.INVALID_REFRESH_TOKEN,
              error: 'Unauthorized',
            },
          },
        },
      },
    },
  })
  @ApiCookieAuth(REFRESH_COOKIE_NAME)
  async getAccessTokenWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @Body() body: RefreshTokenDto,
  ) {
    // 解析 cookie
    const cookies = req.cookies;
    const refreshToken = cookies[REFRESH_COOKIE_NAME];
    // 解析 csrfToken
    const csrfToken = body.refreshToken;

    if (!refreshToken) {
      this.logger.warn(
        { ip: req.ip },
        '[AuthController] refresh-token-web no cookie',
      );
      throw new BadRequestException(AUTH_ERROR.NO_REFRESH_TOKEN);
    }
    const result = await this.authService.getAccessToken(
      refreshToken,
      csrfToken,
    );

    this.logger.debug(
      { sessionId: result.payload.sessionId },
      '[AuthController] refresh-token-web rotating cookie',
    );
    // 从Cookie 中读取到 refresh token，并将新的 refresh token 回写到 Cookie（轮换）

    res.setCookie(REFRESH_COOKIE_NAME, result.token.refreshToken, {
      httpOnly: true,
      secure: true,
      sameSite: 'none',
      path: REFRESH_TOKEN_COOKIE_PATH,
      maxAge: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
    });

    const csrfPayload: CSRFPayload = {
      sessionId: result.payload.sessionId,
      deviceFinger: result.payload.deviceFinger,
    };

    result.token.refreshToken = await this.jwtService.signAsync(csrfPayload, {
      expiresIn: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
      secret: this.configService.get(ENV.CSRF_TOKEN_SECRET),
    });

    return result.token;
  }

  @Delete('logout')
  @ApiOperation({ summary: 'Logout current session' })
  @ApiOkResponse({
    description: 'Successfully logged out',
    schema: {
      type: 'object',
      properties: {
        message: {
          type: 'string',
          example: 'Session successfully revoked',
        },
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized. See examples for possible error codes.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              description: 'Error code (for frontend handling)',
              example: AUTH_ERROR.SESSION_REVOKED,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
        examples: {
          revoked: {
            summary: 'Session revoked (blacklist hit / user logout)',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_REVOKED,
              error: 'Unauthorized',
            },
          },
        },
      },
    },
  })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  async logout(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
  ) {
    const payload = req.user.authTokenPayload;

    if (!payload) {
      this.logger.warn(
        { ip: req.ip },
        '[AuthController] logout missing payload',
      );
      throw new UnauthorizedException(AUTH_ERROR.NO_AUTH_PAYLOAD);
    }

    this.logger.debug(
      { userId: payload.userId, sessionId: payload.sessionId },
      '[AuthController] logout',
    );

    const result = await this.authService.logoutSession(payload);

    if (result) {
      res.clearCookie(REFRESH_COOKIE_NAME, {
        path: REFRESH_TOKEN_COOKIE_PATH,
        httpOnly: true,
        secure: true,
        sameSite: 'none',
      });
    }

    return result;
  }

  @Post('delete-session')
  @HttpCode(200)
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Delete a specific session' })
  @ApiBody({
    description: 'Delete a session by id with password confirmation',
    type: DeleteSessionDto,
    examples: {
      example1: {
        summary: 'Valid deletion payload',
        value: {
          sessionId: '1e2d3c4b-5a6f-7890-abcd-ef1234567890',
          password: 'StrongPassword123!',
        },
      },
    },
  })
  @ApiOkResponse({
    description: 'Session deleted successfully',
    schema: { type: 'string', example: 'Session successfully deleted' },
  })
  @ApiBadRequestResponse({ description: 'Invalid session ID or input' })
  @ApiUnauthorizedResponse({ description: 'Invalid password' })
  @ApiNotFoundResponse({ description: 'Session not found' })
  async deleteSession(
    @Req() req: FastifyRequest,
    @Body() deleteSessionDto: DeleteSessionDto,
    @Res({ passthrough: true }) res: FastifyReply,
  ) {
    const userId = req.user.authTokenPayload?.userId;
    if (!userId) {
      throw new UnauthorizedException(AUTH_ERROR.NO_AUTH_PAYLOAD);
    }
    const result = await this.authService.deleteSession(
      deleteSessionDto,
      userId,
    );
    if (result) {
      res.clearCookie(REFRESH_COOKIE_NAME, {
        path: REFRESH_TOKEN_COOKIE_PATH,
        httpOnly: true,
        secure: true,
        sameSite: 'none',
      });
    }
  }

  @Post('send-code')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: seconds(60) } })
  @ApiOperation({
    summary: 'Request a reset-password verification code (1 per minute)',
  })
  @ApiBody({
    description: 'Request body for sending verification code',
    type: SendVerificationCodeDto,
    examples: {
      example: {
        summary: 'Send to email',
        value: { email: 'user@example.com' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Verification code sent to email (if user exists)',
  })
  @ApiNotFoundResponse({ description: 'Email does not exist' })
  @ApiBadRequestResponse({ description: 'Invalid request body' })
  @ApiTooManyRequestsResponse({
    description: 'Too many requests. Try again later.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 429 },
            message: {
              type: 'string',
              example: AUTH_ERROR.VERIFICATION_CODE_RATE_LIMIT,
            },
            error: { type: 'string', example: 'Too Many Requests' },
          },
        },
      },
    },
  })
  async sendVerificationCode(
    @Body() sendVerificationDto: SendVerificationCodeDto,
  ) {
    this.logger.debug(
      { email: maskEmail(sendVerificationDto?.email) },
      '[AuthController] send-code',
    );
    return await this.authService.sendVerificationCode(sendVerificationDto);
  }

  @Post('verify-code')
  @HttpCode(200)
  @Throttle({ default: { limit: 3, ttl: seconds(60) } })
  @ApiOperation({ summary: 'Verify code and return temporary reset token' })
  @ApiBody({
    description: 'Request body for verification code validation',
    type: VerifyCodeDto,
    examples: {
      example: {
        summary: 'Submit email and verification code',
        value: { email: 'user@example.com', code: '123456' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Code verified, reset token issued',
    type: VerifyCodeResponseDto,
  })
  @ApiUnauthorizedResponse({
    description: 'Incorrect verification code',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              example: AUTH_ERROR.VERIFICATION_CODE_INCORRECT,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid request body' })
  @ApiNotFoundResponse({
    description: 'Verification code not found or expired',
  })
  @ApiTooManyRequestsResponse({
    description: 'Too many attempts. Code is blocked.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 429 },
            message: {
              type: 'string',
              example: AUTH_ERROR.VERIFICATION_CODE_TOO_MANY_ATTEMPTS,
            },
            error: { type: 'string', example: 'Too Many Requests' },
          },
        },
      },
    },
  })
  async verifyCode(@Body() verifyCodeDto: VerifyCodeDto) {
    this.logger.debug(
      { email: maskEmail(verifyCodeDto?.email) },
      '[AuthController] verify-code',
    );
    return await this.authService.verifyCode(verifyCodeDto);
  }

  @Post('reset-password')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: seconds(60) } })
  @ApiOperation({
    summary: 'Reset password with temporary token and revoke all sessions',
  })
  @ApiBody({
    description: 'Reset password request body',
    type: ResetPasswordDto,
    examples: {
      example: {
        summary: 'Submit temporary token and new password',
        value: {
          verification_id: 'uuid-xxxx',
          token: 'temporary-token',
          newPassword: 'NewStrongPassword123!',
        },
      },
    },
  })
  @ApiOkResponse({ description: 'Password updated and all sessions revoked' })
  @ApiNotFoundResponse({ description: 'Reset credential invalid or expired' })
  @ApiBadRequestResponse({ description: 'Invalid request body' })
  async resetPassword(@Body() resetPasswordDto: ResetPasswordDto) {
    this.logger.debug(
      { verificationId: resetPasswordDto?.verification_id },
      '[AuthController] reset-password',
    );
    await this.authService.resetPassword(resetPasswordDto);
  }
}
