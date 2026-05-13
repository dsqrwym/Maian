import { Module } from '@nestjs/common';
import { CategoryService } from './category.service.js';
import { CategoryController } from './category.controller.js';
import { CheckCategoryController } from './controllers/check-category.controller.js';
import { CheckCategoryService } from './services/check-category.service.js';
import { ReadCategoryService } from './services/read-category.service.js';
import { WriteCategoryService } from './services/write-category.service.js';
import { ProductsModule } from '#/products/products.module.js';

@Module({
  imports: [ProductsModule],
  controllers: [CategoryController, CheckCategoryController],
  providers: [
    CategoryService,
    CheckCategoryService,
    WriteCategoryService,
    ReadCategoryService,
  ],
})
export class CategoryModule {}
