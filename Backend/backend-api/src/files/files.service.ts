import {
  ForbiddenException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { STORAGE_DRIVER } from './storage/storage-key';
import { StorageDriver } from './storage/storage.driver';
import { Readable } from 'stream';
import { PrismaService } from '../prisma/prisma.service';
import { UserPayload } from '../auth/auth.types';
import { IUploadFileForWholesalerDto } from './dto/upload-file-for-wholesaler.dto';
import { UserRole } from '../generated/prisma/enums';
import { IProductFilesQueryDto } from './dto/product-files-query.dto';
import { AppAbility } from '../casl/casl-types';
import { Action } from '../casl/actions';
import { subject } from '@casl/ability';

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
    query: IUploadFileForWholesalerDto,
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

  async getProductFileById(query: IProductFilesQueryDto, ability: AppAbility) {
    const { product_id, file_id } = query;
    const file = await this.prisma.files.findUnique({
      select: {
        storage_key: true,
        mime_type: true,
        file_name: true,
        products_files: {
          select: { products: { select: { user_id: true } } },
          where: { product_id: BigInt(product_id) },
        },
      },
      where: {
        id: BigInt(file_id),
        products_files: {
          some: {
            product_id: BigInt(product_id),
          },
        },
      },
    });

    if (!file) {
      throw new NotFoundException(
        'File not found or not associated with this product.',
      );
    }

    const productOwnerId = file.products_files[0].products.user_id;

    if (
      !ability.can(
        Action.Read,
        subject('products_files', { user_id: productOwnerId }),
      )
    ) {
      throw new ForbiddenException('You are not allowed to read this file');
    }
    return {
      stream: this.storage.createReadStream(file.storage_key),
      mime_type: file.mime_type,
      filename: file.file_name,
    };
  }
}
