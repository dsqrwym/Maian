import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Injectable } from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import { RegisterEmailJob, ResetPasswordJob } from './mail.types';
import { VerifyRegistrationProcessorService } from './verification-processor/verify-registration-processor.service';
import { VerifyResetPasswordProcessorService } from './verification-processor/verify-reset-password-processor.service';

@Processor('mail')
@Injectable()
export class MailQueueProcessorService extends WorkerHost {
  constructor(
    private readonly verificationMailProcessorService: VerifyRegistrationProcessorService,
    private readonly resetPasswordProcessorService: VerifyResetPasswordProcessorService,
    private readonly logger: PinoLogger,
  ) {
    super();
  }

  async process(job: Job): Promise<any> {
    switch (job.name) {
      case 'sendResetPassword': {
        return this.sendResetPassword(job.data as ResetPasswordJob);
      }
      case 'sendNormalRegisterEmail': {
        return this.sendNormalRegisterEmail(job.data as RegisterEmailJob);
      }
      default: {
        this.logger.warn(`Unknown mail job: ${job.name}`);
        return;
      }
    }
  }

  async sendNormalRegisterEmail(data: RegisterEmailJob) {
    return this.verificationMailProcessorService.sendNormalRegisterEmail(data);
  }

  async sendResetPassword(data: ResetPasswordJob) {
    return this.resetPasswordProcessorService.sendResetPassword(data);
  }
}
