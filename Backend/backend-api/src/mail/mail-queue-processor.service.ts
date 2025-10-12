import { Processor, WorkerHost } from '@nestjs/bullmq';
import { Job } from 'bullmq';
import { Injectable } from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import {
  ActiveAdminWithPasswordEmailJob,
  ActiveEmployeeWithPasswordEmailJob,
  BaseEmailJobWithLink,
  RegisterEmailJob,
  ResetPasswordJob,
  VerifyEmployeeEmailJob,
} from './mail.types';
import { VerifyRegistrationProcessorService } from './verification-processor/verify-registration.processor.service';
import { VerifyResetPasswordProcessorService } from './verification-processor/verify-reset-password.processor.service';
import { VerifyEmployeeMailProcessorService } from './verification-processor/verify-employee-mail-processor.service';
import { VerifyAdminMailProcessorService } from './verification-processor/verify-admin-mail-processor.service';

@Processor('mail')
@Injectable()
export class MailQueueProcessorService extends WorkerHost {
  constructor(
    private readonly verifyEmployeeMailProcessorService: VerifyEmployeeMailProcessorService,
    private readonly verificationMailProcessorService: VerifyRegistrationProcessorService,
    private readonly verifyAdminMailProcessorService: VerifyAdminMailProcessorService,
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
      case 'sendVerifyAdminEmail': {
        return this.sendVerifyAdminEmail(job.data as BaseEmailJobWithLink);
      }
      case 'sendVerifyEmployeeEmail': {
        return this.sendVerifyEmployeeEmail(job.data as VerifyEmployeeEmailJob);
      }
      case 'sendActiveEmployeeWithTempPassword': {
        return this.sendActiveEmployeeWithTempPassword(
          job.data as ActiveEmployeeWithPasswordEmailJob,
        );
      }
      case 'sendActiveAdminWithTempPassword': {
        return this.sendActiveAdminWithTempPassword(
          job.data as ActiveAdminWithPasswordEmailJob,
        );
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

  async sendVerifyEmployeeEmail(data: VerifyEmployeeEmailJob) {
    return this.verifyEmployeeMailProcessorService.sendVerifyEmployeeEmail(
      data,
    );
  }

  async sendVerifyAdminEmail(data: BaseEmailJobWithLink) {
    return this.verifyAdminMailProcessorService.sendVerifyAdminEmail(data);
  }

  async sendActiveEmployeeWithTempPassword(
    data: ActiveEmployeeWithPasswordEmailJob,
  ) {
    return this.verifyEmployeeMailProcessorService.sendActiveEmployeeWithPasswordEmail(
      data,
    );
  }

  async sendActiveAdminWithTempPassword(data: ActiveAdminWithPasswordEmailJob) {
    return this.verifyAdminMailProcessorService.sendActiveAdminWithPasswordEmail(
      data,
    );
  }
}
