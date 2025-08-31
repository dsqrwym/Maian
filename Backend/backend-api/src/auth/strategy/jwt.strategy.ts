import { PassportStrategy } from '@nestjs/passport';
import { Inject, Injectable, UnauthorizedException } from '@nestjs/common';
import { Cache } from 'cache-manager';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';
import { AuthTokenPayload, ReqUser } from '../auth.types';
import { FastifyRequest } from 'fastify';
import { Logger } from 'nestjs-pino';
import { REDIS_CACHE } from '../../redis/redis.module';
import { HashService } from '../../common/hash/hash.service';
import { ENV } from '../../config/constants';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy, 'my-jwt') {
  constructor(
    readonly configService: ConfigService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache, // 注入 Redis 缓存
    private readonly logger: Logger,
    private readonly hashService: HashService,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get<string>(ENV.AUTH_JWT_SECRET) as string,
      passReqToCallback: true, // 允许访问请求对象
    });
  }

  async validate(req: FastifyRequest, payload: AuthTokenPayload) {
    this.logger.debug(
      `[JwtStrategy] Validating token for userId: ${payload.userId}`,
    );

    const authHeader = req.headers.authorization;
    if (!authHeader) {
      throw new UnauthorizedException('Authorization header is missing');
    }

    const accessToken = authHeader.split(' ')[1];
    const hashedAccessToken =
      await this.hashService.hashWithCrypto(accessToken);

    // 检查黑名单
    const isBlacklisted = await this.redisCache.get(
      `blacklist:${hashedAccessToken}`,
    );
    if (isBlacklisted) {
      this.logger.warn(
        `[JwtStrategy] Access token is blacklisted for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Access token has been revoked');
    }

    const reqUser: ReqUser = {
      authenticatedUser: null,
      authTokenPayload: payload,
    };
    return reqUser;
  }
}
