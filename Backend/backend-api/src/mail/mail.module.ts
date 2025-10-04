import { Global, Module } from '@nestjs/common';
import { MailerModule } from '@nestjs-modules/mailer';
import { HandlebarsAdapter } from '@nestjs-modules/mailer/dist/adapters/handlebars.adapter';
import { join } from 'path';
import { MailService } from './mail.service';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { ENV } from '../config/constants.config';
import { MyI18nModule } from '../i18n/i18n.module';
import { BullModule } from '@nestjs/bullmq';
import { MailQueueProcessorService } from './mail-queue-processor.service';
import { VerifyRegistrationProcessorService } from './verification-processor/verify-registration-processor.service';
import { VerifyResetPasswordProcessorService } from './verification-processor/verify-reset-password-processor.service';

@Global()
@Module({
  imports: [
    ConfigModule,
    MyI18nModule,
    BullModule.registerQueueAsync({
      name: 'mail',
      useFactory: (configService: ConfigService) => ({
        connection: {
          url: configService.get<string>(
            ENV.REDIS_BULL_URL,
            'redis://localhost:6379',
          ),
        },
      }),
      inject: [ConfigService],
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
          from: `"MaiAn" <${configService.get<string>(ENV.FROM_EMAIL, 'noreply@dsqrwym.com')}>`,
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
  ],
  exports: [MailService],
})
export class MailModule {}
