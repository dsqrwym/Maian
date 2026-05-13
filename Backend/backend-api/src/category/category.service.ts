import { Injectable } from '@nestjs/common';
import { PaginatedDataWithT } from '#/common/types-interfaces/response.interface.js';
import { UserPayload } from '#/auth/auth.types.js';
import { ReadCategoryService } from './services/read-category.service.js';
import { WriteCategoryService } from './services/write-category.service.js';
import { PinoLogger } from 'nestjs-pino';
import { ICreateCategoryDto } from '#/category/dto/create-category.dto.js';
import { AppAbility } from '#/casl/casl-types.js';
import { ICategoryQueryDto } from '#/category/dto/category-query.dto.js';
import { ICategoryResponse } from '#/category/dto/category-response.dto.js';
import { IUpdateCategoryDto } from '#/category/dto/update-category.dto.js';

@Injectable()
export class CategoryService {
  constructor(
    private readonly logger: PinoLogger,
    private readonly categoryWriteService: WriteCategoryService,
    private readonly categoryReadService: ReadCategoryService,
  ) {
    this.logger.setContext(CategoryService.name);
  }

  async create(
    createCategoryDto: ICreateCategoryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    return this.categoryWriteService.create(createCategoryDto, ability, user);
  }

  async findAllUseDrizzle(
    query: ICategoryQueryDto,
    ability: AppAbility,
    user: UserPayload,
  ): Promise<PaginatedDataWithT<ICategoryResponse>> {
    return this.categoryReadService.findAllUseDrizzle(query, ability, user);
  }

  async getCategoryForUpdate(id: string, ability: AppAbility) {
    return this.categoryReadService.getCategoryForUpdate(id, ability);
  }

  async update(
    categoryId: string,
    updateCategoryDto: IUpdateCategoryDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    return this.categoryWriteService.update(
      categoryId,
      updateCategoryDto,
      ability,
      user,
    );
  }

  async remove(categoryId: string, ability: AppAbility) {
    return this.categoryWriteService.remove(categoryId, ability);
  }
}
