import { Injectable } from '@nestjs/common';
import { PinoLogger } from 'nestjs-pino';
import { MailService } from '#/mail/mail.service.js';
import { OrderPdfEmailType } from '#/mail/mail.types.js';
import { OrderFilesService } from '#/files/services/order-files.service.js';
import { OrderPdfService } from '#/pdf/services/order-pdf.service.js';
import type { OrderPdfOrder } from '#/pdf/pdf.type.js';
import { getPartyName } from '#/utils/order-pdf.utils.js';

@Injectable()
export class OrderPdfNotificationService {
  constructor(
    private readonly orderPdfService: OrderPdfService,
    private readonly orderFilesService: OrderFilesService,
    private readonly mailService: MailService,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(OrderPdfNotificationService.name);
  }

  async notifyNewOrder(orderId: string) {
    return this.sendOrderPdfNotification(orderId, OrderPdfEmailType.NEW_ORDER);
  }

  async notifyOrderAccepted(orderId: string) {
    return this.sendOrderPdfNotification(
      orderId,
      OrderPdfEmailType.ORDER_ACCEPTED,
    );
  }

  async notifyOrderRejected(orderId: string) {
    return this.sendOrderPdfNotification(
      orderId,
      OrderPdfEmailType.ORDER_REJECTED,
    );
  }

  async notifyOrderCancelled(orderId: string) {
    return this.sendOrderPdfNotification(
      orderId,
      OrderPdfEmailType.ORDER_CANCELLED,
    );
  }

  private async sendOrderPdfNotification(
    orderId: string,
    type: OrderPdfEmailType,
  ) {
    const order = await this.orderPdfService.getOrderForPdf(BigInt(orderId));
    const recipient = this.resolveNotificationRecipient(order, type);

    if (!recipient.to) {
      this.logger.warn(
        { orderId, type },
        'Order PDF email skipped because recipient email is missing',
      );
      return;
    }

    const config = await this.orderPdfService.getUserConfiguration(
      recipient.userId,
    );
    const file = await this.orderFilesService.ensureOrderPdfFile(
      orderId,
      recipient.userId,
      null,
      order,
      config,
    );

    await this.mailService.sendOrderPdfNotification({
      to: recipient.to,
      lang: file.language,
      type,
      orderNumber: order.order_number,
      fileId: file.fileId,
      recipientName: recipient.recipientName,
      counterpartyName: recipient.counterpartyName,
      actionReason: recipient.actionReason,
    });
  }

  private resolveNotificationRecipient(
    order: OrderPdfOrder,
    type: OrderPdfEmailType,
  ) {
    if (
      type === OrderPdfEmailType.NEW_ORDER ||
      type === OrderPdfEmailType.ORDER_CANCELLED
    ) {
      return {
        userId: order.wholesaler_id ?? order.wholesaler_snapshot.id,
        to: order.wholesaler_snapshot.email,
        recipientName: getPartyName(order.wholesaler_snapshot),
        counterpartyName: getPartyName(order.retailer_snapshot),
        actionReason:
          type === OrderPdfEmailType.ORDER_CANCELLED
            ? order.cancelled_reason
            : null,
      };
    }

    return {
      userId: order.retailer_id ?? order.retailer_snapshot.id,
      to: order.retailer_snapshot.email,
      recipientName: getPartyName(order.retailer_snapshot),
      counterpartyName: getPartyName(order.wholesaler_snapshot),
      actionReason:
        type === OrderPdfEmailType.ORDER_REJECTED
          ? order.rejected_reason
          : null,
    };
  }
}
