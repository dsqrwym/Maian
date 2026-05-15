import {
  BadRequestException,
  Controller,
  Get,
  Req,
  StreamableFile,
  UseGuards,
  UnauthorizedException,
  Res,
  Headers,
  HttpStatus,
} from '@nestjs/common';
import { FilesService } from './files.service.js';
import { FileVideoPlayTokenService } from './services/file-video-play-token.service.js';
import type { FastifyReply, FastifyRequest } from 'fastify';
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
import type { IVideoStreamQueryDto } from './dto/video-play-query.dto.js';
import { SkipResponseInterceptor } from '#/common/guards/decorator/skip-response-interceptor.decorator.js';
import * as mime from 'mime-types';
import * as path from 'path';
import { TypedParam, TypedQuery, TypedRoute } from '@nestia/core';
import { PassThrough } from 'node:stream';
import { FILE_ERROR } from './constants/files.constants.js';
import { PinoLogger } from 'nestjs-pino';
import { ProductFilesService } from './services/product-files.service.js';
import { TagsUuid } from '#/utils/typia/validators/auth.validator.js';

/**
 * Controller for file upload and retrieval
 * @class FilesController
 */
@ApiTags('File Management')
@Controller()
export class FilesController {
  constructor(
    private readonly filesService: FilesService,
    private readonly productFilesService: ProductFilesService,
    private readonly fileVideoPlayTokenService: FileVideoPlayTokenService,
    private readonly logger: PinoLogger,
  ) {}

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
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
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
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiProduces('application/octet-stream')
  @Get('product-file')
  @SkipResponseInterceptor()
  async getProductFile(
    @TypedQuery() query: IProductFilesQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<StreamableFile> {
    const { stream, mime_type, filename } =
      await this.productFilesService.getProductFileById(query, req.ability);

    return new StreamableFile(await stream, {
      type: mime_type,
      disposition: `inline; filename="${encodeURIComponent(filename)}"`,
    });
  }

  /**
   * @ignore
   */
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @ApiProduces('application/octet-stream')
  @Get('user/:user_id/image')
  @SkipResponseInterceptor()
  async getUserImageByUserId(
    @TypedParam('user_id') userId: TagsUuid,
  ): Promise<StreamableFile> {
    const { stream, mime_type, filename } =
      await this.filesService.getImageByUserId(userId);

    return new StreamableFile(await stream, {
      type: mime_type,
      disposition: `inline; filename*=UTF-8''${encodeURIComponent(filename)}`,
    });
  }

  /**
   * 获取产品视频临时播放 Token
   * 需要用户已登录，校验权限后返回 playToken
   */
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @TypedRoute.Get('video/play-token')
  async getVideoPlayToken(
    @TypedQuery() query: IProductFilesQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<{ playToken: string }> {
    // 复用现有权限检查逻辑 - 检查用户是否有权限访问该文件
    await this.productFilesService.verifyProductFile(query, req.ability);

    const playToken = await this.fileVideoPlayTokenService.createPlayToken(
      query.product_id,
      query.file_id,
    );

    this.logger.debug(
      { query },
      '[getVideoPlayUrl] Video play Token generated',
    );

    return { playToken };
  }

  /**
   * 临时 Token 视频访问接口
   * 不使用普通 Authorization header，通过 playToken 验证权限
   * @ignore
   */
  @TypedRoute.Get('video/stream')
  @ApiProduces('video/*')
  @SkipResponseInterceptor()
  async streamVideo(
    @TypedQuery() query: IVideoStreamQueryDto,
    @Headers('range') range: string | undefined,
    @Res() reply: FastifyReply,
  ) {
    const { playToken, file_id, product_id } = query;

    if (!playToken) {
      throw new UnauthorizedException(FILE_ERROR.VIDEO_PLAY_TOKEN_MISSING);
    }

    await this.fileVideoPlayTokenService.verifyPlayToken(query);

    const file = await this.productFilesService.getVideoFileMetaById(
      file_id,
      product_id,
    );

    const fileSize = file.file_size;

    reply.header('Content-Type', file.mime_type);
    reply.header('Accept-Ranges', 'bytes');
    reply.header('Cache-Control', 'private, no-store');
    reply.header(
      'Content-Disposition',
      `inline; filename="${encodeURIComponent(file.filename)}"`,
    );

    if (!range) {
      const stream = await this.productFilesService.createVideoFileStream(
        file.storage_key,
      );

      reply.code(HttpStatus.OK);
      reply.header('Content-Length', fileSize.toString());
      return reply.send(stream);
    }

    const match = range.match(/^bytes=(\d*)-(\d*)$/);

    if (!match) {
      reply.code(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
      reply.header('Content-Range', `bytes */${fileSize}`);
      return reply.send();
    }

    let start: number;
    let end: number;

    if (!match[1] && match[2]) {
      const suffixLength = Number(match[2]);
      start = Math.max(fileSize - suffixLength, 0);
      end = fileSize - 1;
    } else {
      start = match[1] ? Number(match[1]) : 0;
      end = match[2] ? Number(match[2]) : fileSize - 1;
    }

    if (
      Number.isNaN(start) ||
      Number.isNaN(end) ||
      start > end ||
      start >= fileSize
    ) {
      reply.code(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
      reply.header('Content-Range', `bytes */${fileSize}`);
      return reply.send();
    }

    const safeEnd = Math.min(end, fileSize - 1);

    const stream = await this.productFilesService.createVideoFileStream(
      file.storage_key,
      start,
      safeEnd,
    );

    const chunkSize = safeEnd - start + 1;

    reply.code(HttpStatus.PARTIAL_CONTENT);
    reply.header('Content-Length', chunkSize.toString());
    reply.header('Content-Range', `bytes ${start}-${safeEnd}/${fileSize}`);

    return reply.send(stream);
  }
}
