import {
  BadRequestException,
  ForbiddenException,
  Inject,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import type { AppAbility } from '#/casl/casl-types.js';
import { Action } from '#/casl/actions.js';
import { subject } from '@casl/ability';
import { DrizzleDb, DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  files,
  products,
  products_files,
  user_uploads,
} from '#/generated/drizzle/schema.js';
import { and, eq, sql } from 'drizzle-orm';
import { STORAGE_DRIVER } from '#/files/storage/storage-key.js';
import { StorageDriver } from '#/files/storage/storage.driver.js';
import { IProductFilesQueryDto } from '#/files/dto/product-files-query.dto.js';
import { FILE_ERROR } from '#/files/constants/files.constants.js';

@Injectable()
export class ProductFilesService {
  constructor(
    @Inject(STORAGE_DRIVER) private readonly storage: StorageDriver,
    private readonly drizzle: DrizzleService,
  ) {}
  async verifyProductFile(query: IProductFilesQueryDto, ability: AppAbility) {
    const { product_id, file_id } = query;

    const [file] = await this.drizzle.db
      .select({
        storage_key: files.storage_key,
        mime_type: files.mime_type,
        file_name: files.file_name,
        product_user_id: products.user_id, // 拿 products 表的 user_id
      })
      .from(files)
      // innerJoin 就是 join 只返回两个表中匹配的行。如果某行在任一表中无匹配，则不返回。
      .innerJoin(products_files, eq(files.id, products_files.file_id))
      .innerJoin(products, eq(products_files.product_id, products.id))
      .where(
        and(
          eq(files.id, BigInt(file_id)), // 查找文件
          eq(products_files.product_id, BigInt(product_id)), // 文件属于产品
        ),
      )
      .limit(1);

    if (!file) {
      throw new NotFoundException(
        'File not found or not associated with this product.',
      );
    }

    if (
      !ability.can(
        Action.Read,
        subject('products_files', { user_id: file.product_user_id }),
      )
    ) {
      throw new ForbiddenException('You are not allowed to read this file');
    }

    return file;
  }

  async getProductFileById(query: IProductFilesQueryDto, ability: AppAbility) {
    const file = await this.verifyProductFile(query, ability);

    return {
      stream: this.storage.createReadStream(file.storage_key),
      mime_type: file.mime_type,
      filename: file.file_name,
    };
  }

  async getVideoFileMetaById(fileId: string, productId: string) {
    const [file] = await this.drizzle.db
      .select({
        storage_key: files.storage_key,
        mime_type: files.mime_type,
        file_name: files.file_name,
        file_size: files.file_size,
      })
      .from(files)
      .innerJoin(products_files, eq(files.id, products_files.file_id))
      .where(
        and(
          eq(files.id, BigInt(fileId)),
          eq(products_files.product_id, BigInt(productId)),
        ),
      )
      .limit(1);

    if (!file) {
      throw new NotFoundException(FILE_ERROR.FILE_NOT_FOUND);
    }

    if (!file.mime_type.startsWith('video/')) {
      throw new BadRequestException(FILE_ERROR.FILE_NOT_VIDEO);
    }

    return {
      storage_key: file.storage_key,
      mime_type: file.mime_type,
      filename: file.file_name,
      file_size: Number(file.file_size),
    };
  }

  async createVideoFileStream(
    storageKey: string,
    start?: number,
    end?: number,
  ) {
    if (start === undefined && end === undefined) {
      return this.storage.createReadStream(storageKey);
    }

    return this.storage.createReadStream(storageKey, {
      start,
      end,
    });
  }
}
