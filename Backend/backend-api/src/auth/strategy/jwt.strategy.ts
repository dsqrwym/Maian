import { PassportStrategy } from '@nestjs/passport';
import { Inject, Injectable, UnauthorizedException } from '@nestjs/common';
import { Cache } from 'cache-manager';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';
import { AuthTokenPayload, ReqUser } from '../auth.types';
import { Logger } from 'nestjs-pino';
import { REDIS_CACHE } from '../../redis/redis.module';
import { AUTH_ERROR, ENV, REDIS_KEYS } from '../../config/constants';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy, 'my-jwt') {
  constructor(
    readonly configService: ConfigService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache, // 注入 Redis 缓存
    private readonly logger: Logger,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get<string>(ENV.AUTH_JWT_SECRET) as string,
      passReqToCallback: false, // 不允许访问请求对象
    });
  }

  async validate(payload: AuthTokenPayload) {
    this.logger.debug(
      `[JwtStrategy] Validating token for userId: ${payload.userId}`,
    );

    // 检查黑名单
    if (
      await this.redisCache.get(REDIS_KEYS.sessionRevokedKey(payload.sessionId))
    ) {
      this.logger.warn(
        `[JwtStrategy] Session is blacklisted for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException(AUTH_ERROR.SESSION_REVOKED);
    }

    const reqUser: ReqUser = {
      authenticatedUser: null,
      authTokenPayload: payload,
    };
    return reqUser;
  }
}
