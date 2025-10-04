import {
  Body,
  Controller,
  Delete,
  HttpCode,
  Post,
  Req,
  Res,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiBody,
  ApiExtraModels,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { AUTH_ERROR } from '../auth.constants';
import {
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from '../../config/constants.config';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';
import { JwtAuthGuard } from '../guard/auth.guard';
import { DeleteSessionDto } from '../dto/delete.session.dto';

@ApiTags('Session')
@ApiExtraModels(DeleteSessionDto)
@Controller('session')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
export class SessionController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

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
  async logout(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
  ) {
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

  @Post('delete-session')
  @HttpCode(200)
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
