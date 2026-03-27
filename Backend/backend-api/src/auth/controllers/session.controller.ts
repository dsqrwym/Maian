import {
  Controller,
  HttpCode,
  Req,
  Res,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { AUTH_ERROR } from '../auth.constants';
import {
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from '../../config/constants.config';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';
import { JwtAuthGuard } from '../guard/auth.guard';
import { IDeleteSessionDto } from '../dto/delete.session.dto';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from '../../utils/typia/typed-body.typia';
import typia from 'typia';

@ApiTags('Session')
@Controller('session')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
export class SessionController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

  @TypedRoute.Delete('logout')
  @ApiBearerAuth()
  async logout(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
  ): Promise<{ message: string }> {
    const payload = req.user;

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

  @TypedRoute.Post('delete-session')
  @HttpCode(200)
  @ApiBearerAuth()
  async deleteSession(
    @Req() req: FastifyRequest,
    @TypedBody({
      type: 'assert',
      assert: typia.createAssertEquals<IDeleteSessionDto>(),
    })
    deleteSessionDto: IDeleteSessionDto,
    @Res({ passthrough: true }) res: FastifyReply,
  ): Promise<void> {
    const userId = req.user.userId;
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
}
