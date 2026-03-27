import {
  BadRequestException,
  Controller,
  HttpCode,
  Req,
  Res,
  UnauthorizedException,
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
import { ApiCookieAuth, ApiExtraModels, ApiTags } from '@nestjs/swagger';
import { IRefreshTokenDto } from '../dto/refresh-token.dto';
import { TokenResponseDto } from '../dto/token-response.dto';
import { AUTH_ERROR } from '../auth.constants';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { CSRFPayload } from '../auth.types';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from '../../utils/typia/typed-body.typia';
import typia from 'typia';

@Controller('token')
@ApiTags('RefreshToken')
@ApiExtraModels(TokenResponseDto)
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

  @TypedRoute.Post('refresh')
  @HttpCode(200)
  @Throttle({
    default: {
      limit: 1,
      ttl: seconds(RefreshTokenController.ACCESS_TOKEN_TTL),
    },
  })
  async getAccessToken(
    @TypedBody({
      type: 'assert',
      assert: typia.createAssertEquals<IRefreshTokenDto>(),
    })
    body: IRefreshTokenDto,
  ): Promise<TokenResponseDto> {
    const refreshToken = body?.refreshToken;
    if (!refreshToken) {
      this.logger.warn({}, '[AuthController] refresh-token missing');
      throw new BadRequestException(AUTH_ERROR.NO_REFRESH_TOKEN);
    }

    this.logger.debug({}, '[AuthController] refresh-token');
    const result = await this.authService.getAccessToken(refreshToken);
    return result.token;
  }

  @TypedRoute.Post('refresh-web')
  @HttpCode(200)
  @Throttle({
    default: {
      limit: 1,
      ttl: seconds(RefreshTokenController.ACCESS_TOKEN_TTL),
    },
  })
  @ApiCookieAuth(REFRESH_COOKIE_NAME)
  async getAccessTokenWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @TypedBody({
      type: 'assert',
      assert: typia.createAssertEquals<IRefreshTokenDto>(),
    })
    body: IRefreshTokenDto,
  ): Promise<TokenResponseDto> {
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
      throw new UnauthorizedException(AUTH_ERROR.NO_REFRESH_TOKEN);
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
      domain: 'dsqrwym.es',
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
