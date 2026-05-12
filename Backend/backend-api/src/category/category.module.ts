import { Module } from '@nestjs/common';
import { CategoryService } from './category.service.js';
import { CategoryController } from './category.controller.js';
import { RouterModule } from '@nestjs/core';
import { CheckCategoryController } from './controllers/check-category.controller.js';
import { CheckCategoryService } from './services/check-category.service.js';
import { CategoryReadService } from '#/category/services/category-read.service.js';
import { CategoryWriteService } from '#/category/services/category-write.service.js';
import { ProductsModule } from '#/products/products.module.js';

@Module({
  imports: [
    RouterModule.register([
      {
        path: 'category',
        module: CategoryModule,
      },
    ]),
    ProductsModule,
  ],
  controllers: [CategoryController, CheckCategoryController],
  providers: [
    CategoryService,
    CheckCategoryService,
    CategoryWriteService,
    CategoryReadService,
  ],
})
export class CategoryModule {}
