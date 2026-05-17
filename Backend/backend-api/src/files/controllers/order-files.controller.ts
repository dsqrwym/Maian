import {
  Controller,
  Get,
  Req,
  StreamableFile,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiProduces, ApiTags } from '@nestjs/swagger';
import type { FastifyRequest } from 'fastify';
import { TypedParam } from '@nestia/core';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { SkipResponseInterceptor } from '#/common/guards/decorator/skip-response-interceptor.decorator.js';
import { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';
import { OrderFilesService } from '#/files/services/order-files.service.js';

@ApiTags('Order Files')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('order-file')
export class OrderFilesController {
  constructor(private readonly orderFilesService: OrderFilesService) {}

  /**
   * @ignore
   */
  @ApiProduces('application/pdf')
  @SkipResponseInterceptor()
  @Get(':order_id/preview')
  async previewOrderPdf(
    @TypedParam('order_id') orderId: TagsIntegerString,
    @Req() req: FastifyRequest,
  ): Promise<StreamableFile> {
    const file = await this.orderFilesService.getOrderPdfFile(
      orderId,
      req.ability,
      req.user.userId,
    );

    return new StreamableFile(file.stream, {
      type: file.mime_type,
      disposition: `inline; filename*=UTF-8''${encodeURIComponent(file.filename)}`,
    });
  }

  /**
   * @ignore
   */
  @ApiProduces('application/pdf')
  @SkipResponseInterceptor()
  @Get(':order_id/download')
  async downloadOrderPdf(
    @TypedParam('order_id') orderId: TagsIntegerString,
    @Req() req: FastifyRequest,
  ): Promise<StreamableFile> {
    const file = await this.orderFilesService.getOrderPdfFile(
      orderId,
      req.ability,
      req.user.userId,
    );

    return new StreamableFile(file.stream, {
      type: file.mime_type,
      disposition: `attachment; filename*=UTF-8''${encodeURIComponent(file.filename)}`,
    });
  }
}
