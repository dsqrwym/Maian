import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { Logger } from 'nestjs-pino';
import { Cron, CronExpression } from '@nestjs/schedule';
import { addDays, DAY, reduceDay } from '../utils/date.utils';
import { $Enums } from '../../prisma/generated';
import UserStatus = $Enums.UserStatus;
import { MailService } from 'src/mail/mail.service';
import { JwtService } from '@nestjs/jwt';
import { EmailVerificationPayload } from '../auth/auth.types';

@Injectable()
export class EmailTasksService {
  constructor(
    private readonly prismaService: PrismaService,
    private readonly mailService: MailService,
    private readonly jwtService: JwtService,
    private readonly logger: Logger,
  ) {}
  @Cron(CronExpression.EVERY_DAY_AT_MIDNIGHT)
  async sendRepeatVerificationEmail() {
    const now = new Date();

    const usersToRemind = await this.prismaService.users.findMany({
      where: {
        status: UserStatus.ACTIVE,
        email_verified: false,
        created_at: { lt: reduceDay(now, 3) },
        NOT: {
          email_reminder_log: {
            some: {
              sent_at: {
                gte: new Date(now.getFullYear(), now.getMonth(), now.getDate()),
              },
            },
          },
        },
      },
      select: {
        id: true,
        email: true,
        created_at: true,
        configurations: { select: { language: true, timezone: true } },
      },
    });

    this.logger.log(`Found ${usersToRemind.length} users to send reminder.`);

    for (const user of usersToRemind) {
      const payload: EmailVerificationPayload = { id: user.id };
      const token = this.jwtService.sign(payload, { expiresIn: 4 * DAY });

      try {
        await this.mailService.sendVerificationEmail(
          user.email,
          token,
          user.configurations?.language,
          user.configurations?.timezone,
          addDays(user.created_at, 7),
          false,
        );

        await this.prismaService.email_reminder_log.create({
          data: { user_id: user.id },
        });
        await this.prismaService.users.update({
          where: { id: user.id },
          data: { status: UserStatus.INACTIVE },
        });

        this.logger.log(
          `Sent verification email to ${user.email} (ID: ${user.id}).`,
        );
      } catch (e) {
        this.logger.error(
          `Failed to send verification email to ${user.email} (ID: ${user.id})`,
          e,
        );
      }
    }

    this.logger.log(
      `Finished sending verification emails: ${usersToRemind.length} users.`,
    );
  }
}
