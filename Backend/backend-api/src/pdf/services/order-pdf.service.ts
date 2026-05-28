import { Injectable } from '@nestjs/common';
import type {
  GeneratedOrderPdfFile,
  OrderPdfAssets,
  OrderPdfOrder,
  UserConfiguration,
} from '#/pdf/pdf.type.js';
import { OrderPdfDataService } from '#/pdf/services/order-pdf-data.service.js';
import { OrderPdfLabelsService } from '#/pdf/services/order-pdf-labels.service.js';
import { OrderPdfRendererService } from '#/pdf/services/order-pdf-renderer.service.js';
import { sanitizeOrderPdfFilename } from '#/utils/pdf/order.pdf.utils.js';

@Injectable()
export class OrderPdfService {
  constructor(
    private readonly orderPdfDataService: OrderPdfDataService,
    private readonly orderPdfLabelsService: OrderPdfLabelsService,
    private readonly orderPdfRendererService: OrderPdfRendererService,
  ) {}

  getOrderForPdf(orderId: bigint) {
    return this.orderPdfDataService.getOrderForPdf(orderId);
  }

  getUserConfiguration(userId?: string | null) {
    return this.orderPdfDataService.getUserConfiguration(userId);
  }

  async generateOrderPdfFile(
    orderId: string,
    languageOwnerId?: string | null,
    options: {
      order?: OrderPdfOrder;
      assets?: OrderPdfAssets;
      config?: UserConfiguration;
    } = {},
  ): Promise<GeneratedOrderPdfFile> {
    const order = options.order ?? (await this.getOrderForPdf(BigInt(orderId)));
    const config =
      options.config ??
      (await this.getUserConfiguration(
        languageOwnerId ?? order.wholesaler_id ?? order.retailer_id,
      ));
    const labels = this.orderPdfLabelsService.getPdfLabels(config.language);
    const data = await this.orderPdfDataService.buildOrderPdfData(
      order,
      config,
      options.assets,
    );

    return {
      filename: sanitizeOrderPdfFilename(order.order_number),
      content: await this.orderPdfRendererService.renderOrderPdf(data, labels),
      order,
      language: config.language,
    };
  }
}
