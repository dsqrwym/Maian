import { Module } from '@nestjs/common';
import { CategoryService } from './services/category.service';
import { CategoryController } from './controllers/category.controller';
import { RouterModule } from '@nestjs/core';
import { CheckCategoryController } from './controllers/check-category.controller';
import { CheckCategoryService } from './services/check-category.service';

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
