import { Injectable } from '@nestjs/common';
import { ICreateProductDto } from './dto/create-product.dto.js';
import { IUpdateProductDto } from './dto/update-product.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { UserPayload } from '#/auth/auth.types.js';
import { PinoLogger } from 'nestjs-pino';
import { ProductsWriteService } from '#/products/services/products-write.service.js';
import { ProductsReadService } from '#/products/services/products-read.service.js';
import { IProductListQueryDto } from '#/products/dto/product-list-query.dto.js';

@Injectable()
export class ProductsService {
  constructor(
    private readonly logger: PinoLogger,
    private readonly productsWriteService: ProductsWriteService,
    private readonly productsReadService: ProductsReadService,
  ) {
    this.logger.setContext(ProductsService.name);
  }

  async create(
    createProductDto: ICreateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    return this.productsWriteService.create(createProductDto, ability, user);
  }

  async findAllUseSqlD(
    query: IProductListQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    return this.productsReadService.findAllUseSqlD(query, ability, user);
  }
  async update(
    id: string,
    updateProductDto: IUpdateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    return this.productsWriteService.update(
      id,
      updateProductDto,
      ability,
      user,
    );
  }

  async remove(id: string, ability: AppAbility) {
    return this.productsWriteService.remove(id, ability);
  }

  // 存在悲观锁所以 version 的放回不在必要
  async getForUpdate(id: string, ability: AppAbility) {
    return this.productsReadService.getForUpdate(id, ability);
  }

  async getProductDetail(id: string, langCode: string, ability: AppAbility) {
    return this.productsReadService.getProductDetail(id, langCode, ability);
  }
}
