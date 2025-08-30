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
} from '@nestjs/swagger';
import { FastifyReply, FastifyRequest } from 'fastify';
import { LoginDto } from './dto/login.dto';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { JwtAuthGuard, LocalAuthGuard } from './guard/auth.guard';
import { RefreshTokenDto } from './dto/refresh-token.dto';
import { ConfigService } from '@nestjs/config';
import { REFRESH_TOKEN_COOKIE_PATH } from 'src/config/constants';

@ApiTags('Authentication')
@Controller('auth')
@ApiExtraModels(RegisterDto, LoginDto, DeleteSessionDto, RefreshTokenDto)
export class AuthController {
  constructor(
    private readonly authService: AuthService,
    private readonly configService: ConfigService,
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
  @ApiBody({ type: RegisterDto })
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
          deviceName: 'Chrome Browser',
          ipAddress: '192.168.1.1',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description: 'User successfully logged in',
    schema: {
      example: {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid login credentials' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  @UseGuards(LocalAuthGuard)
  async login(@Req() req: FastifyRequest, @Body() body: LoginDto) {
    const user = req.user.authenticatedUser;
    if (!user) {
      throw new BadRequestException('User authentication failed');
    }

    return this.authService.login(req, user, body);
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
          deviceName: 'Chrome Browser',
          ipAddress: '192.168.1.1',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
        },
      },
    },
  })
  @ApiOkResponse({
    description:
      'User successfully logged in. Response body 含 accessToken；响应头通过 Set-Cookie 写入 refresh_token。',
    schema: {
      example: {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
      },
    },
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

    const { accessToken, refreshToken } = await this.authService.login(
      req,
      user,
      body,
    );
    // Web: 设置 cookie（httpOnly, secure, sameSite）
    res.setCookie('refresh_token', refreshToken, {
      httpOnly: true,
      secure: true,
      sameSite: 'none', // 跨域前后端分离（不同子域 / 不同域名）
      path: REFRESH_TOKEN_COOKIE_PATH,
      maxAge: this.configService.get<number>('REFRESH_TOKEN_EXPIRES_IN'),
    });

    return {
      accessToken,
    };
  }

  @Post('refresh-token')
  @HttpCode(200)
  @ApiOperation({
    summary: 'Refresh access token (Cookie first, Body as fallback)',
    description:
      '优先从 Cookie 名为 refresh_token 中读取 refresh token；如果没有 Cookie，则可在请求体中提供 { refreshToken }。',
  })
  @ApiBody({
    description:
      '非浏览器/不使用 Cookie 的客户端在 Body 中提供 refreshToken；若存在 Cookie，将优先使用 Cookie 中的 refresh_token。',
    type: RefreshTokenDto,
    examples: {
      cookiePreferred: {
        summary: '使用 Cookie（推荐 Web）',
        value: {},
      },
      bodyFallback: {
        summary: '非 Web 平台通过 Body 提供 refreshToken',
        value: { refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Returns new access token',
    schema: {
      example: {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
      },
    },
  })
  @ApiUnauthorizedResponse({ description: 'Invalid or expired refresh token' })
  @ApiCookieAuth('refresh_token')
  async getAccessToken(
    @Req() req: FastifyRequest,
    @Body() body: RefreshTokenDto,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    @Res({ passthrough: true }) _res: FastifyReply,
  ) {
    // 解析 cookie
    const cookies = req.cookies;
    const fromCookie = cookies['refresh_token'];

    const fromBody = body?.refreshToken;

    const refreshToken = fromCookie || fromBody;
    return this.authService.getAccessToken(refreshToken);
  }

  @Delete('logout')
  @ApiOperation({ summary: 'Logout current session' })
  @ApiOkResponse({ description: 'Successfully logged out' })
  @ApiUnauthorizedResponse({ description: 'Unauthorized' })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  async logout(@Req() req: FastifyRequest) {
    const authHeader = req.headers.authorization;
    if (!authHeader) {
      throw new UnauthorizedException('No access token found');
    }

    const accessToken = authHeader.split(' ')[1];
    if (!accessToken) {
      throw new UnauthorizedException('No access token found');
    }

    const payload = req.user.authTokenPayload;

    if (!payload) {
      throw new UnauthorizedException('No valid token payload found');
    }

    return this.authService.logoutSession(payload, accessToken);
  }

  @Post('delete-session')
  @HttpCode(200)
  @ApiOperation({ summary: 'Delete a specific session' })
  @ApiBody({ type: DeleteSessionDto })
  @ApiOkResponse({ description: 'Session deleted successfully' })
  @ApiBadRequestResponse({ description: 'Invalid session ID or input' })
  async deleteSession(@Body() deleteSessionDto: DeleteSessionDto) {
    return this.authService.deleteSession(deleteSessionDto);
  }
}
