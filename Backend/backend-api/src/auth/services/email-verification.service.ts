import { Injectable } from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { Logger } from 'nestjs-pino';
import { getVerificationResponseContent } from '../../mail/templates/varification-response-content';
import { getVerificationResponseHtml } from '../../mail/templates/verification-response.tmplates';
import { UserStatus } from 'prisma/generated';
import { PrismaService } from 'src/prisma/prisma.service';
import { JwtService } from '@nestjs/jwt';

@Injectable()
export class EmailVerificationService {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly jwtService: JwtService,
    private readonly logger: Logger,
  ) {}
  async verifyEmail(token: string, lang: string, reply: FastifyReply) {
    const sendHtml = (key: 'invalid' | 'alreadyVerified' | 'success') => {
      const content = getVerificationResponseContent(lang)[key];
      return reply.type('text/html').send(getVerificationResponseHtml(content));
    };

    try {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
      const payload = await this.jwtService.verifyAsync(token);

      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
      const userId: string = payload.id;
      this.logger.debug({ userId }, '[verifyEmail] token verified');
      if (!userId) return sendHtml('invalid');

      const user = await this.prismaService.users.findUnique({
        where: { id: userId },
        select: { status: true },
      });

      if (!user) return sendHtml('invalid');
      if (user.status !== UserStatus.INACTIVE)
        return sendHtml('alreadyVerified');

      await this.prismaService.users.update({
        where: { id: userId, status: UserStatus.INACTIVE },
        data: { status: UserStatus.PENDING_REVIEW },
      });

      this.logger.debug({ userId }, '[verifyEmail] status -> PENDING_REVIEW');

      return sendHtml('success');
    } catch (error: unknown) {
      this.logger.warn({ err: error }, '[verifyEmail] invalid token');
      return sendHtml('invalid');
    }
  }
}
