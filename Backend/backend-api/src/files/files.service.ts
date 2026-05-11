import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { STORAGE_DRIVER } from './storage/storage-key.js';
import { StorageDriver } from './storage/storage.driver.js';
import { Readable } from 'stream';
import { UserPayload } from '#/auth/auth.types.js';
import type { IUploadFileForWholesalerDto } from './dto/upload-file-for-wholesaler.dto.js';
import { UserRole } from '#/generated/drizzle/enums.js';
import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import { files, user_uploads, users } from '#/generated/drizzle/schema.js';
import { and, eq, isNotNull, sql } from 'drizzle-orm';
import { FILE_ERROR } from './constants/files.constants.js';

@Injectable()
export class FilesService {
  constructor(
    @Inject(STORAGE_DRIVER) private readonly storage: StorageDriver,
    private readonly drizzle: DrizzleService,
  ) {}
  attachUser = (tx: DrizzleDb, user_id: string, file_id: bigint) =>
    tx
      .insert(user_uploads)
      .values({ user_id, file_id })
      .onConflictDoUpdate({
        target: [user_uploads.user_id, user_uploads.file_id],
        set: { created_at: sql`(NOW() AT TIME ZONE 'UTC')` },
      });

  async uploadFile(
    buffer: Buffer | Readable,
    filename: string,
    user: UserPayload,
    query: IUploadFileForWholesalerDto,
  ) {
    const uploadResult = await this.storage.upload(buffer, filename);
    const { pathKey, file_hash, file_name, mime_type, file_size, cloudSynced } =
      uploadResult;

    const file = await this.drizzle.db.transaction(async (tx) => {
      const [createdOrUpdatedFile] = await tx
        .insert(files)
        .values({
          file_hash,
          file_name,
          mime_type,
          file_size: BigInt(file_size),
          storage_key: pathKey,
          cloud_synced: cloudSynced ?? true, // 如果没有 cloudSynced 字段，默认为 true
        })
        .onConflictDoUpdate({
          target: files.file_hash,
          set: {
            to_delete: false,
            ...(cloudSynced === true ? { cloud_synced: true } : {}), // 更新时明确同步成功，才把它改成 true
          },
        })
        .returning({ id: files.id });

      await this.attachUser(tx, user.userId, createdOrUpdatedFile.id);

      if (user.wholesalerId) {
        await this.attachUser(tx, user.wholesalerId, createdOrUpdatedFile.id);
      }

      if (
        query.wholesalerId &&
        (user.userRole === UserRole.ADMIN ||
          user.userRole === UserRole.SUPERADMIN)
      ) {
        const wholesaler = query.wholesalerId;
        await this.attachUser(tx, wholesaler, createdOrUpdatedFile.id);
      }
      return createdOrUpdatedFile;
    });

    return { id: file.id.toString() };
  }

  async getImageByUserId(userId: string) {
    const [file] = await this.drizzle.db
      .select({
        filename: files.file_name,
        mime_type: files.mime_type,
        storage_key: files.storage_key,
      })
      .from(users)
      .innerJoin(files, eq(files.id, users.profile_image_file_id))
      .where(and(eq(users.id, userId), isNotNull(users.profile_image_file_id)));

    if (!file) {
      throw new NotFoundException(FILE_ERROR.FILE_NOT_FOUND);
    }

    return {
      stream: this.storage.createReadStream(file.storage_key),
      mime_type: file.mime_type,
      filename: file.filename,
    };
  }
}
