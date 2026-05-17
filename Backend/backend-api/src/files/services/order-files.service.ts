import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { subject } from '@casl/ability';
import { and, eq } from 'drizzle-orm';
import type { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { files, order_pdf_files, orders } from '#/generated/drizzle/schema.js';
import { ORDER_ERRORS } from '#/orders/order.constants.js';
import { FilesService } from '#/files/files.service.js';
import { OrderPdfService } from '#/pdf/services/order-pdf.service.js';
import type { OrderPdfOrder, UserConfiguration } from '#/pdf/pdf.type.js';

export type OrderPdfFile = {
  fileId: string;
  filename: string;
  mimeType: string;
  storageKey: string;
  language: string;
};

type OrderPdfFileState = {
  order_id: bigint;
  retailer_id: string | null;
  wholesaler_id: string | null;
  lang_code: string | null;
  file_id: bigint | null;
  filename: string | null;
  mime_type: string | null;
  storage_key: string | null;
};

@Injectable()
export class OrderFilesService {
  constructor(
    private readonly drizzle: DrizzleService,
    private readonly filesService: FilesService,
    private readonly orderPdfService: OrderPdfService,
  ) {}

  async getOrderPdfFile(
    orderId: string,
    ability: AppAbility,
    languageOwnerId?: string | null,
  ) {
    const order_id = BigInt(orderId);
    const config =
      await this.orderPdfService.getUserConfiguration(languageOwnerId);
    const langCode = this.normalizeLangCode(config.language);
    const state = await this.getOrderPdfFileState(order_id, langCode);

    if (!state) {
      throw new NotFoundException(ORDER_ERRORS.ORDER_NOT_FOUND);
    }

    if (
      !ability.can(
        Action.Read,
        subject('orders', {
          retailer_id: state.retailer_id,
          wholesaler_id: state.wholesaler_id,
        }),
      )
    ) {
      throw new ForbiddenException(
        'You are not allowed to read this order PDF',
      );
    }

    const file =
      this.toOrderPdfFile(state, langCode) ??
      (await this.ensureOrderPdfFile(
        orderId,
        languageOwnerId,
        state,
        undefined,
        config,
      ));

    return {
      stream: await this.filesService.createReadStreamByStorageKey(
        file.storageKey,
      ),
      mime_type: file.mimeType,
      filename: file.filename,
    };
  }

  async ensureOrderPdfFile(
    orderId: string,
    languageOwnerId?: string | null,
    knownState?: OrderPdfFileState | null,
    knownOrder?: OrderPdfOrder,
    knownConfig?: UserConfiguration,
  ): Promise<OrderPdfFile> {
    const order_id = BigInt(orderId);
    const order =
      knownOrder ??
      (!languageOwnerId
        ? await this.orderPdfService.getOrderForPdf(order_id)
        : null);
    const config =
      knownConfig ??
      (await this.orderPdfService.getUserConfiguration(
        languageOwnerId ?? order?.wholesaler_id ?? order?.retailer_id,
      ));
    const langCode = this.normalizeLangCode(config.language);
    const state =
      knownState ?? (await this.getOrderPdfFileState(order_id, langCode));
    const existingFile = state ? this.toOrderPdfFile(state, langCode) : null;

    if (existingFile) return existingFile;

    const orderForPdf =
      order ??
      knownOrder ??
      (await this.orderPdfService.getOrderForPdf(order_id));
    const generated = await this.orderPdfService.generateOrderPdfFile(
      orderId,
      languageOwnerId,
      {
        order: orderForPdf,
        config: {
          ...config,
          language: langCode,
        },
        assets: {
          wholesalerLogoDataUrl: await this.getWholesalerLogoDataUrl(
            orderForPdf.wholesaler_id,
          ),
        },
      },
    );

    const uploadedFile = await this.filesService.uploadGeneratedFile(
      generated.content,
      generated.filename,
    );

    const [linkedFile] = await this.drizzle.db
      .insert(order_pdf_files)
      .values({
        order_id,
        lang_code: langCode,
        file_id: uploadedFile.id,
      })
      .onConflictDoNothing({
        target: [order_pdf_files.order_id, order_pdf_files.lang_code],
      })
      .returning({
        file_id: order_pdf_files.file_id,
      });

    if (!linkedFile?.file_id) {
      const concurrentFile = await this.getExistingOrderPdfFile(
        order_id,
        langCode,
      );
      if (concurrentFile) return concurrentFile;
    }

    return {
      fileId: uploadedFile.id.toString(),
      filename: uploadedFile.file_name,
      mimeType: uploadedFile.mime_type,
      storageKey: uploadedFile.storage_key,
      language: langCode,
    };
  }

  private async getOrderPdfFileState(
    orderId: bigint,
    langCode: string,
  ): Promise<OrderPdfFileState | null> {
    const [orderFile] = await this.drizzle.db
      .select({
        order_id: orders.id,
        retailer_id: orders.retailer_id,
        wholesaler_id: orders.wholesaler_id,
        lang_code: order_pdf_files.lang_code,
        file_id: files.id,
        filename: files.file_name,
        mime_type: files.mime_type,
        storage_key: files.storage_key,
      })
      .from(orders)
      .leftJoin(
        order_pdf_files,
        and(
          eq(order_pdf_files.order_id, orders.id),
          eq(order_pdf_files.lang_code, langCode),
        ),
      )
      .leftJoin(files, eq(files.id, order_pdf_files.file_id))
      .where(eq(orders.id, orderId))
      .limit(1);

    return orderFile ?? null;
  }

  private async getExistingOrderPdfFile(
    orderId: bigint,
    langCode: string,
  ): Promise<OrderPdfFile | null> {
    const state = await this.getOrderPdfFileState(orderId, langCode);
    return state ? this.toOrderPdfFile(state, langCode) : null;
  }

  private toOrderPdfFile(
    state: OrderPdfFileState,
    langCode: string,
  ): OrderPdfFile | null {
    if (
      !state.file_id ||
      !state.filename ||
      !state.mime_type ||
      !state.storage_key
    ) {
      return null;
    }

    return {
      fileId: state.file_id.toString(),
      filename: state.filename,
      mimeType: state.mime_type,
      storageKey: state.storage_key,
      language: state.lang_code ?? langCode,
    };
  }

  private async getWholesalerLogoDataUrl(wholesalerId?: string | null) {
    try {
      return await this.filesService.getUserImageDataUrl(wholesalerId);
    } catch {
      return null;
    }
  }

  private normalizeLangCode(language?: string | null) {
    return language?.trim() || 'en';
  }
}
