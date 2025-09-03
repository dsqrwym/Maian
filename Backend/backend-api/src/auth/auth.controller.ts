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
  ApiConflictResponse,
  ApiCreatedResponse,
  ApiOkResponse,
  ApiUnauthorizedResponse,
  ApiCookieAuth,
  ApiNotFoundResponse,
} from '@nestjs/swagger';
import { FastifyReply, FastifyRequest } from 'fastify';
import { LoginDto } from './dto/login.dto';
import { TokenResponseDto } from './dto/token-response.dto';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { JwtAuthGuard, LocalAuthGuard } from './guard/auth.guard';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { ConfigService } from '@nestjs/config';
import {
  AUTH_ERROR,
  ENV,
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from 'src/config/constants';
import { JwtService } from '@nestjs/jwt';
import { CSRFPayload } from './auth.types';

@ApiTags('Authentication')
@Controller('auth')
@ApiExtraModels(RegisterDto, LoginDto, DeleteSessionDto, RefreshTokenDto)
export class AuthController {
  constructor(
    private readonly authService: AuthService,
    private readonly configService: ConfigService,
    private readonly jwtService: JwtService,
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
  @ApiCreatedResponse({ description: 'User successfully registered' })
  @ApiBadRequestResponse({ description: 'Invalid input data' })
  @ApiConflictResponse({ description: 'Email already exists' })
  async register(@Body() body: RegisterDto) {
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
      throw new BadRequestException('User authentication failed');
    }

    const { token } = await this.authService.login(req, user, body);
    return token;
  }

  @Post('login-web')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Web login: returns accessToken and sets refresh_token cookie',
    description:
      '适用于浏览器场景：返回 accessToken；通过 Set-Cookie 写入 httpOnly/secure 的 refresh_token。',
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
      'User successfully logged in. Web 流程中：响应体的 refreshToken 字段承载 CSRF Token；响应头通过 Set-Cookie 写入真实的 refresh_token。',
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
      throw new BadRequestException('User authentication failed');
    }

    const { token, payload } = await this.authService.login(req, user, body);
    // Web: 设置 cookie（httpOnly, secure, sameSite）
    res.setCookie(REFRESH_COOKIE_NAME, token.refreshToken, {
      httpOnly: true,
      secure: true,
      sameSite: 'none', // 跨域前后端分离（不同子域 / 不同域名）
      path: REFRESH_TOKEN_COOKIE_PATH,
      maxAge: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
    });

    const csrfTokenPayload: CSRFPayload = {
      sessionId: payload.sessionId,
      deviceFinger: payload.deviceFinger,
    };

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
  @ApiOperation({
    summary: 'Refresh tokens (Body only)',
    description:
      '非 Web 场景：在请求体中提供 { refreshToken } 完成刷新；不涉及 Cookie。',
  })
  @ApiBody({
    description: '非浏览器/不使用 Cookie 的客户端在 Body 中提供 refreshToken。',
    type: RefreshTokenDto,
    examples: {
      bodyExample: {
        summary: 'Body 传入 refreshToken',
        value: { refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Returns new accessToken and refreshToken。',
    type: TokenResponseDto,
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized. 可能的认证错误码详见 examples',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              description: '错误码（用于前端区分场景）',
              example: AUTH_ERROR.SESSION_NOT_FOUND,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
        examples: {
          csrfInvalid: {
            summary: 'CSRF 校验失败',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.CSRF_INVALID,
              error: 'Unauthorized',
            },
          },
          sessionNotFound: {
            summary: '会话不存在/被踢出/过期/refresh 无效',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_NOT_FOUND,
              error: 'Unauthorized',
            },
          },
          sessionRevoked: {
            summary: '会话被撤销（用户主动退出）',
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
  async getAccessToken(@Body() body: RefreshTokenDto) {
    const refreshToken = body?.refreshToken;
    if (!refreshToken) {
      throw new BadRequestException('No refresh token provided');
    }

    const result = await this.authService.getAccessToken(refreshToken);
    return result.token;
  }

  @Post('refresh-token-web')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Web 刷新：Cookie+CSRF（Body.refreshToken）并轮换 refresh_token',
    description:
      '浏览器场景：从 Cookie 读取真实 refresh_token；请求体的 refreshToken 字段承载 CSRF Token（与会话绑定）。校验通过后，响应通过 Set-Cookie 写入新的 refresh_token（轮换），响应体返回新的 accessToken 与新的 CSRF（依旧在 refreshToken 字段）。',
  })
  @ApiBody({
    description:
      'Web 刷新必须在 Body.refreshToken 传入 CSRF Token。真实 refresh_token 从 Cookie 自动发送（名称：refresh_token）。',
    type: RefreshTokenDto,
    examples: {
      webRefresh: {
        summary: 'Web 刷新（Cookie 自动携带 refresh_token；Body 传 CSRF）',
        value: { refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.CSRF...' },
      },
    },
  })
  @ApiOkResponse({
    description:
      'Returns new accessToken and refreshToken. 若使用 Cookie，响应头 Set-Cookie 会写入新的 refresh_token（轮换）。',
    type: TokenResponseDto,
    headers: {
      'Set-Cookie': {
        description: `当请求携带 Cookie 时，返回新的 refresh_token；HttpOnly; Secure; SameSite=None; Path='${REFRESH_TOKEN_COOKIE_PATH}'`,
        schema: { type: 'string' },
      },
    },
  })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized. 可能的认证错误码详见 examples',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              description: '错误码（用于前端区分场景）',
              example: AUTH_ERROR.SESSION_NOT_FOUND,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
        examples: {
          csrfInvalid: {
            summary: 'CSRF 校验失败',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.CSRF_INVALID,
              error: 'Unauthorized',
            },
          },
          sessionNotFound: {
            summary: '会话不存在/被踢出/过期/refresh 无效',
            value: {
              statusCode: 401,
              message: AUTH_ERROR.SESSION_NOT_FOUND,
              error: 'Unauthorized',
            },
          },
          sessionRevoked: {
            summary: '会话被撤销（用户主动退出）',
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
      throw new BadRequestException('No refresh token provided');
    }
    const result = await this.authService.getAccessToken(
      refreshToken,
      csrfToken,
    );

    const csrfPayload: CSRFPayload = {
      sessionId: result.payload.sessionId,
      deviceFinger: result.payload.deviceFinger,
    };

    // 从Cookie 中读取到 refresh token，并将新的 refresh token 回写到 Cookie（轮换）

    res.setCookie(REFRESH_COOKIE_NAME, result.token.refreshToken, {
      httpOnly: true,
      secure: true,
      sameSite: 'none',
      path: REFRESH_TOKEN_COOKIE_PATH,
      maxAge: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
    });

    result.token.refreshToken = await this.jwtService.signAsync(csrfPayload, {
      expiresIn: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
      secret: this.configService.get(ENV.CSRF_TOKEN_SECRET),
    });

    return result.token;
  }

  @Delete('logout')
  @ApiOperation({ summary: 'Logout current session' })
  @ApiOkResponse({ description: 'Successfully logged out' })
  @ApiUnauthorizedResponse({
    description: 'Unauthorized. 可能的认证错误码详见 examples',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              description: '错误码（用于前端区分场景）',
              example: AUTH_ERROR.SESSION_REVOKED,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
        examples: {
          revoked: {
            summary: '会话被撤销（黑名单命中/用户主动退出后）',
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
      throw new UnauthorizedException('No valid token payload found');
    }

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
  @ApiOkResponse({ description: 'Session deleted successfully' })
  @ApiBadRequestResponse({ description: 'Invalid session ID or input' })
  @ApiUnauthorizedResponse({ description: 'Invalid password' })
  @ApiNotFoundResponse({ description: 'Session not found' })
  async deleteSession(
    @Body() deleteSessionDto: DeleteSessionDto,
    @Res({ passthrough: true }) res: FastifyReply,
  ) {
    const result = await this.authService.deleteSession(deleteSessionDto);
    if (result) {
      res.clearCookie(REFRESH_COOKIE_NAME, {
        path: REFRESH_TOKEN_COOKIE_PATH,
        httpOnly: true,
        secure: true,
        sameSite: 'none',
      });
    }
  }
}
