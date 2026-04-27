import { Module } from '@nestjs/common';
import { CategoryService } from './services/category.service.js';
import { CategoryController } from './controllers/category.controller.js';
import { RouterModule } from '@nestjs/core';
import { CheckCategoryController } from './controllers/check-category.controller.js';
import { CheckCategoryService } from './services/check-category.service.js';

@Module({
  imports: [
    RouterModule.register([
      {
        path: 'category',
        module: CategoryModule,
      },
    ]),
  ],
  controllers: [CategoryController, CheckCategoryController],
  providers: [CategoryService, CheckCategoryService],
})
export class CategoryModule {}
