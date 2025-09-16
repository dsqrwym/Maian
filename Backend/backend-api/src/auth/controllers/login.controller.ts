import {
  BadRequestException,
  Body,
  Controller,
  HttpCode,
  Post,
  Req,
  Res,
  UseGuards,
} from '@nestjs/common';
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
import { LocalAuthGuard } from '../guard/auth.guard';
import { FastifyReply, FastifyRequest } from 'fastify';
import { AUTH_ERROR } from '../auth.constants';
import {
  ENV,
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from '../../config/constants.config';
import { Logger } from 'nestjs-pino';
import { CSRFPayload } from '../auth.types';
import { AuthService } from '../auth.service';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';

@ApiTags('Authentication')
@ApiExtraModels(LoginDto, TokenResponseDto)
@Controller('')
export class LoginController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
    private readonly configService: ConfigService,
    private readonly jwtService: JwtService,
  ) {}

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
}
