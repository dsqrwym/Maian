import { Global, Module } from '@nestjs/common';
import { MailerModule } from '@nestjs-modules/mailer';
import { join } from 'path';
import { MailService } from './mail.service.js';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { ENV } from '#/config/constants.config.js';
import { MyI18nModule } from '#/i18n/i18n.module.js';
import { BullModule } from '@nestjs/bullmq';
import { MailQueueProcessorService } from './mail-queue-processor.service.js';
import { VerifyRegistrationProcessorService } from './verification-processor/verify-registration.processor.service.js';
import { VerifyResetPasswordProcessorService } from './verification-processor/verify-reset-password.processor.service.js';
import { VerifyEmployeeMailProcessorService } from './verification-processor/verify-employee-mail-processor.service.js';
import { VerifyAdminMailProcessorService } from './verification-processor/verify-admin-mail-processor.service.js';
import { Logger } from 'nestjs-pino';
import { HandlebarsAdapter } from '@nestjs-modules/mailer/adapters/handlebars.adapter';
import { FilesModule } from '#/files/files.module.js';
import { OrderPdfNotificationProcessorService } from '#/mail/verification-processor/order-pdf-notification-processor.service.js';

@Global()
@Module({
  imports: [
    ConfigModule,
    MyI18nModule,
    FilesModule,
    BullModule.registerQueueAsync({
      name: 'mail',
      useFactory: (configService: ConfigService, logger: Logger) => ({
        connection: {
          url: configService.get<string>(
            ENV.REDIS_BULL_URL,
            'redis://localhost:6379',
          ),
          reconnectOnError: (error) => {
            logger.error('[Bull Redis error]:', error);
            return true;
          },
          maxRedirections: null,
          retryStrategy: (times) => {
            const delay = Math.min(times * 100, 3000);
            logger.warn(`[BullMQ Redis Retry] reconnecting in ${delay}ms`);
            return delay;
          },
        },
      }),
      inject: [ConfigService, Logger],
    }),
    MailerModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        transport: {
          host: configService.get<string>(ENV.SMTP_HOST),
          port: parseInt(configService.get<string>(ENV.SMTP_PORT, '587')),
          secure: false,
          auth: {
            user: configService.get<string>(ENV.SMTP_USER),
            pass: configService.get<string>(ENV.SMTP_PASS),
          },
        },
        defaults: {
          name: 'MaiAn',
          from: configService.get<string>(
            ENV.FROM_EMAIL,
            'noreply@dsqrwym.com',
          ),
        },
        template: {
          dir: join(process.cwd(), 'src', 'mail', 'templates'),
          adapter: new HandlebarsAdapter(),
          options: { strict: true },
        },
      }),
    }),
  ],
  providers: [
    MailService,
    MailQueueProcessorService,
    VerifyResetPasswordProcessorService,
    VerifyRegistrationProcessorService,
    VerifyEmployeeMailProcessorService,
    VerifyAdminMailProcessorService,
    OrderPdfNotificationProcessorService,
  ],
  exports: [MailService],
})
export class MailModule {}
