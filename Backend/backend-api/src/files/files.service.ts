import { Inject, Injectable } from '@nestjs/common';
import { STORAGE_DRIVER } from './storage/storage-key';
import { StorageDriver } from './storage/storage.driver';
import { Readable } from 'stream';
import { PrismaService } from '../prisma/prisma.service';
import { UserPayload } from '../auth/auth.types';
import { UploadFileForWholesalerDto } from './upload-file-for-wholesaler.dto';
import { UserRole } from '../generated/prisma/enums';

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
    query: UploadFileForWholesalerDto,
  ) {
    const { pathKey, file_hash, file_name, mime_type, file_size } =
      await this.storage.upload(buffer, filename);

    const file = await this.prisma.$transaction(async (tx) => {
      const fileId = await tx.files.upsert({
        where: { file_hash },
        update: {
          to_delete: false,
        },
        create: {
          file_hash,
          file_name,
          mime_type,
          file_size,
          storage_key: pathKey,
        },
        select: { id: true },
      });

      const attachUser = (user_id: string, file_id: bigint) =>
        tx.user_uploads.upsert({
          where: { user_id_file_id: { user_id, file_id } },
          update: { created_at: new Date() }, // 更新时间以示"活跃"
          create: { user_id, file_id },
        });

      await attachUser(user.userId, fileId.id);

      if (user.wholesalerId) {
        await attachUser(user.wholesalerId, fileId.id);
      }

      if (
        query.wholesalerId &&
        (user.userRole === UserRole.ADMIN ||
          user.userRole === UserRole.SUPERADMIN)
      ) {
        const wholesaler = query.wholesalerId;
        await attachUser(wholesaler, fileId.id);
      }
      return fileId;
    });

    return { id: file.id.toString() };
  }
}
