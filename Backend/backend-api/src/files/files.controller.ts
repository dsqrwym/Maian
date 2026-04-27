import {
  BadRequestException,
  Controller,
  Get,
  Req,
  StreamableFile,
  UseGuards,
} from '@nestjs/common';
import { FilesService } from './files.service.js';
import type { FastifyRequest } from 'fastify';
import {
  ApiBearerAuth,
  ApiBody,
  ApiConsumes,
  ApiProduces,
  ApiTags,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { fileTypeFromBuffer } from 'file-type';
import {
  ALLOWED_MIMES,
  CHUNK_SIZE,
} from '#/config/fastify-multipart.config.js';
import type { IUploadFileForWholesalerDto } from './dto/upload-file-for-wholesaler.dto.js';
import type { IProductFilesQueryDto } from './dto/product-files-query.dto.js';
import { SkipResponseInterceptor } from '#/common/guards/decorator/skip-response-interceptor.decorator.js';
import * as mime from 'mime-types';
import * as path from 'path';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { PassThrough } from 'node:stream';

/**
 * Controller for file upload and retrieval
 * @class FilesController
 */
@ApiTags('File Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('files')
export class FilesController {
  constructor(private readonly filesService: FilesService) {}

  /**
   * Upload a raw file via multipart/form-data.
   *
   * Reads the first chunk to detect the real MIME type using file-type,
   * validates against allowed MIME types, sanitizes the filename,
   * and streams the file to storage. Returns the generated file ID.
   *
   * @param {FastifyRequest} req - Request object containing the multipart file
   * @param {IUploadFileForWholesalerDto} query - Optional query parameters for wholesaler-specific upload
   * @returns {Promise<{ id: string }>} The ID of the uploaded file
   */
  @ApiConsumes('multipart/form-data')
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        file: {
          type: 'string',
          format: 'binary',
        },
      },
    },
  })
  @TypedRoute.Post('upload-raw')
  async uploadRawFile(
    @Req() req: FastifyRequest,
    @TypedQuery() query: IUploadFileForWholesalerDto,
  ): Promise<{ id: string }> {
    const multipart = await req.file();
    if (!multipart) throw new BadRequestException('File is required.');
    const { file, filename } = multipart;

    let isProcessed = false; // 标记位：是否已成功交给 service

    // try finally 在结束时使用 resume() 读完释放资源，不使用destroy() 因为会直接中断整个响应链
    try {
      const chunk = (await file.read(CHUNK_SIZE)) as Buffer | null;

      if (!chunk || chunk.length === 0) {
        throw new BadRequestException('Empty file');
      }

      // filename.png
      const extWithDot = path.extname(filename); // ".png"
      let ext = extWithDot.slice(1).toLowerCase(); // "png"
      let baseName = path.basename(filename, extWithDot); // suffix – optionally, an extension to remove from the result.
      // basename = filename.png -> filename

      // 使用 file-type 检测真实 mime
      const type = await fileTypeFromBuffer(chunk);
      const detectedMime = type?.mime ?? multipart.mimetype;

      if (!ALLOWED_MIMES.has(detectedMime)) {
        file.resume();
        throw new BadRequestException(`File type not allowed: ${detectedMime}`);
      }

      // 只用 file-type 推断的 mime 再 type-mimes 投映对应的ext
      ext = mime.extension(detectedMime) || 'bin';

      // 清理非法字符
      baseName = baseName.replace(/[/\\:*?"<>|]/g, '_');

      const newFilename = `${baseName}.${ext}`.slice(0, 255);

      const user = req.user;
      // 合法文件，把 chunk 放回流中
      if (file.unshift) {
        file.unshift(chunk);
        // 标记成功
        isProcessed = true;
        return await this.filesService.uploadFile(
          file,
          newFilename,
          user,
          query,
        );
      } else {
        const fullStream = new PassThrough();
        fullStream.write(chunk);
        file.pipe(fullStream);
        isProcessed = true;
        return await this.filesService.uploadFile(
          fullStream,
          newFilename,
          user,
          query,
        );
      }
    } finally {
      if (!isProcessed) {
        file.resume();
      }
    }
  }

  /**
   * @ignore
   */
  @ApiProduces('application/octet-stream')
  @Get('product-file')
  @SkipResponseInterceptor()
  async getProductFile(
    @TypedQuery() query: IProductFilesQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<StreamableFile> {
    const { stream, mime_type, filename } =
      await this.filesService.getProductFileById(query, req.ability);

    return new StreamableFile(await stream, {
      type: mime_type,
      disposition: `inline; filename="${encodeURIComponent(filename)}"`,
    });
  }
}
