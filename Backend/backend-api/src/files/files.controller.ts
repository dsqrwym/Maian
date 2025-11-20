import { Controller, Post, Req, UseGuards } from '@nestjs/common';
import { FilesService } from './files.service';
import { FastifyRequest } from 'fastify';
import { ApiBearerAuth, ApiBody, ApiConsumes, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/guard/auth.guard';

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
  async uploadRawFile(@Req() req: FastifyRequest) {
    const multipart = await req.file();
    if (!multipart) return;
    const { file, filename } = multipart;
    return await this.filesService.uploadFile(file, filename, req.user);
  }
}
