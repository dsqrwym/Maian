import { Inject, Injectable } from '@nestjs/common';
import { STORAGE_DRIVER } from './storage/storage-key';
import { StorageDriver } from './storage/storage.driver';
import { Readable } from 'stream';
import { PrismaService } from '../prisma/prisma.service';
import { UserPayload } from '../auth/auth.types';

@Injectable()
export class FilesService {
  constructor(
    @Inject(STORAGE_DRIVER) private readonly storage: StorageDriver,
    private readonly prisma: PrismaService,
  ) {}

  async uploadFile(
    buffer: Buffer | Readable,
    filename: string,
    user: UserPayload,
  ) {
    const { pathKey, file_hash, file_name, mime_type, file_size } =
      await this.storage.upload(buffer, filename);

    const exist = await this.prisma.files.findUnique({
      where: {
        file_hash,
      },
    });
    if (exist) {
      return {
        id: exist.id.toString(),
      };
    }

    const file = await this.prisma.files.create({
      data: {
        file_hash,
        file_name,
        mime_type,
        file_size,
        storage_key: pathKey,
        uploaded_by: user.userId,
      },
      select: {
        id: true,
      },
    });

    return {
      id: file.id.toString(),
    };
  }
}
