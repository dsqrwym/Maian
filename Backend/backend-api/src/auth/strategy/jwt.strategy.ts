import { PassportStrategy } from '@nestjs/passport';
import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';
import { HashService } from 'src/common/hash/hash.service';
import { AuthTokenPayload, ReqUser } from '../auth.types';
import { FastifyRequest } from 'fastify';
import { Logger } from 'nestjs-pino';
import { PrismaService } from 'src/prisma/prisma.service';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy, 'my-jwt') {
  constructor(
    configService: ConfigService,
    private readonly hashService: HashService,
    private readonly prismaService: PrismaService,
    private readonly logger: Logger,
  ) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get<string>('AUTH_JWT_SECRET') as string,
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

    const session = await this.prismaService.user_sessions.findUnique({
      where: {
        session_id: payload.sessionId,
      },
      select: {
        access_token: true,
        refresh_token: true,
        revoked: true,
        session_id: true,
      },
    });

    if (!session) {
      this.logger.warn(
        `[getAccessToken] Session not found for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Session not found');
    }

    if (session.revoked) {
      this.logger.warn(
        `[getAccessToken] Session revoked for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Session has been revoked');
    }

    if (
      !(await this.hashService.compareCrypto(
        accessToken,
        session.access_token || '',
      ))
    ) {
      this.logger.warn(
        `[getAccessToken] Refresh token mismatch for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Invalid refresh token');
    }

    const reqUser: ReqUser = {
      authenticatedUser: null,
      authTokenPayload: payload,
    };
    return reqUser;
  }
}
