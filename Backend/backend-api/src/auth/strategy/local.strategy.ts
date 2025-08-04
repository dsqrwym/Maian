import { PassportStrategy } from '@nestjs/passport';
import { Injectable, UnauthorizedException } from '@nestjs/common';
import { Strategy } from 'passport-custom';
import { PrismaService } from 'src/prisma/prisma.service';
import { HashService } from 'src/common/hash/hash.service';
import { FastifyRequest } from 'fastify';
import { LoginDto } from '../dto/login.dto';
import { AuthenticatedUser, AuthTokenPayload, ReqUser } from '../auth.types';
import { Logger } from 'nestjs-pino';

@Injectable()
export class LocalStrategy extends PassportStrategy(Strategy, 'custom-local') {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
    private readonly logger: Logger,
  ) {
    super();
  }

  // validate 方法现在接收 req 作为第一个参数
  async validate(req: FastifyRequest) {
    const { username, email, password } = req.body as LoginDto;

    this.logger.debug(
      `[LocalStrategy] Validating user: ${email || 'N/A'}, username: ${username || 'N/A'}`,
    );

    // 在这里执行用户查找和密码验证逻辑
    // 使用 email 或 username 查找用户
    const user = await this.prismaService.users.findFirst({
      where: {
        OR: [{ username }, { email }],
      },
    });

    if (!user) {
      this.logger.warn(`[LocalStrategy] User not found`);
      throw new UnauthorizedException('User does not exist!');
    }

    if (!(await this.hashService.compareWithBcrypt(password, user.password))) {
      this.logger.warn(
        `[LocalStrategy] Incorrect password for userId: ${user.id}`,
      );
      throw new UnauthorizedException('Incorrect password');
    }

    // 验证成功后，返回用户对象，Passport 会将其附加到 request.user 上
    const result: AuthenticatedUser = {
      id: user.id,
      user_id: user.user_id,
      role: user.role,
      status: user.status,
    };

    const reqUser: ReqUser = {
      authTokenPayload: null,
      authenticatedUser: result,
    };
    return reqUser;
  }
}
