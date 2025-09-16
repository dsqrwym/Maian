import {
  BadRequestException,
  Body,
  Controller,
  HttpCode,
  Post,
  Req,
  Res,
} from '@nestjs/common';
import {
  ENV,
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from '../../config/constants.config';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { seconds, Throttle } from '@nestjs/throttler';
import {
  ApiBody,
  ApiCookieAuth,
  ApiExtraModels,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { RefreshTokenDto } from '../dto/refresh-token.dto';
import { TokenResponseDto } from '../dto/token-response.dto';
import { AUTH_ERROR } from '../auth.constants';
import { FastifyReply, FastifyRequest } from 'fastify';
import { CSRFPayload } from '../auth.types';

@Controller('')
@ApiTags('RefreshToken')
@ApiExtraModels(RefreshTokenDto, TokenResponseDto)
export class RefreshTokenController {
  private static ACCESS_TOKEN_TTL = Number(
    process.env[ENV.ACCESS_TOKEN_EXPIRES_IN],
  );
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
    private readonly configService: ConfigService,
    private readonly jwtService: JwtService,
  ) {}

  @Post('refresh-token')
  @HttpCode(200)
  @Throttle({
    default: {
      limit: 1,
      ttl: seconds(RefreshTokenController.ACCESS_TOKEN_TTL),
    },
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
    default: {
      limit: 1,
      ttl: seconds(RefreshTokenController.ACCESS_TOKEN_TTL),
    },
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
}
