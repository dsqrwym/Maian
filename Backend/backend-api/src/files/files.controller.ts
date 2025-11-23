import {
  BadRequestException,
  Controller,
  Post,
  Query,
  Req,
  UseGuards,
} from '@nestjs/common';
import { FilesService } from './files.service';
import { FastifyRequest } from 'fastify';
import { ApiBearerAuth, ApiBody, ApiConsumes, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guard/auth.guard';
import { fileTypeFromBuffer } from 'file-type';
import { ALLOWED_MIMES } from '../config/fastify-multipart.config';
import { UploadFileForWholesalerDto } from './upload-file-for-wholesaler.dto';

@ApiTags('File Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('files')
export class FilesController {
  constructor(private readonly filesService: FilesService) {}

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
  @Post('upload-raw')
  async uploadRawFile(
    @Req() req: FastifyRequest,
    @Query() query: UploadFileForWholesalerDto,
  ) {
    const multipart = await req.file();
    if (!multipart) throw new BadRequestException('File is required.');
    const { file, filename } = multipart;
    const CHUNK_SIZE = 4100; // 足够检测大部分文件类型
    const chunk = (await file.read(CHUNK_SIZE)) as Buffer | null;

    if (!chunk || chunk.length === 0) {
      throw new BadRequestException('Empty file');
    }

    // 使用 file-type 检测真实 mime
    const type = await fileTypeFromBuffer(chunk);
    const detectedMime = type?.mime ?? multipart.mimetype;

    if (!ALLOWED_MIMES.has(detectedMime)) {
      throw new BadRequestException(`File type not allowed: ${detectedMime}`);
    }

    // 合法文件，把 chunk 放回流中
    if (file.unshift) {
      file.unshift(chunk);
      const user = req.user;
      return await this.filesService.uploadFile(file, filename, user, query);
    }
  }
}
