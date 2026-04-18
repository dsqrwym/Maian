import { PinoLogger } from 'nestjs-pino';
import { ISendMailOptions, MailerService } from '@nestjs-modules/mailer';
import { maskEmail } from './email.utils';

/**
 * Sends an email using the provided mailer service and logs the result
 * 使用提供的邮件服务发送邮件并记录结果
 *
 * @param logger - Pino logger instance for logging
 * @param mailerService - Mailer service instance for sending emails
 * @param functionName - Name of the calling function for error logging
 * @param to - Recipient email address (will be masked in logs)
 * @param sendMailOption - Email sending options
 * @returns Promise that resolves with the send mail result
 *
 * @throws Will throw an error if sending fails, which can be caught by BullMQ for retries
 *        如果发送失败会抛出错误，可以被 BullMQ 捕获并重试
 */
export async function sendMail(
  logger: PinoLogger,
  mailerService: MailerService,
  functionName: string,
  to: string,
  sendMailOption: ISendMailOptions,
) {
  try {
    // Send the email using the provided mailer service
    // 使用提供的邮件服务发送邮件
    const info: unknown = await mailerService.sendMail(sendMailOption);

    // Log successful email sending with masked recipient email
    // 记录成功发送的邮件日志，收件人邮箱会被部分隐藏
    logger.info(
      `Email sent successfully to ${maskEmail(to)} with info: ${JSON.stringify(info)}`,
    );
    return info;
  } catch (err) {
    // Handle and log any errors that occur during email sending
    // 处理并记录发送邮件过程中发生的错误
    const error = err instanceof Error ? err : new Error(String(err));
    logger.error(`${functionName} failed: ${error.message}`, error.stack);

    // Re-throw the error to allow BullMQ to handle retries
    // 重新抛出错误以便 BullMQ 可以处理重试
    throw error;
  }
}
