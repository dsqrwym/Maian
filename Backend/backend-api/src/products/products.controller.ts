import { Controller, UseGuards, Req } from '@nestjs/common';
import { ProductsService } from './products.service';
import {
  ICreateProductDto,
  validateICreateProduct,
} from './dto/create-product.dto';
import {
  IUpdateProductDto,
  validateIUpdateProduct,
} from './dto/update-product.dto';
import { JwtAuthGuard } from '../auth/guard/auth.guard';
import { FastifyRequest } from 'fastify';
import {
  IProductListQueryDto,
  validateProductListQuery,
} from './dto/product-list-query.dto';
import { IProductQueryDto } from './dto/product-query.dto';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { TypedParam, TypedQuery, TypedRoute } from '@nestia/core';
import { TypedBody } from '../utils/typia/typed-body.typia';

@ApiTags('Product Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('product')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @TypedRoute.Post()
  create(
    @TypedBody(validateICreateProduct) createProductDto: ICreateProductDto,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.productsService.create(createProductDto, req.ability, req.user);
  }

  @TypedRoute.Get()
  async findAll(
    @TypedQuery(validateProductListQuery) query: IProductListQueryDto,
    @Req() req: FastifyRequest,
  ) {
    return this.productsService.findAllUseSql(query, req.ability, req.user);
    // return this.productsService.findAll(query, req.ability); prisma 无法实现 我对关系聚合和排序的要求
  }

  @TypedRoute.Get(':id')
  findOne(
    @TypedParam('id') id: string,
    @Req() req: FastifyRequest,
    @TypedQuery() query: IProductQueryDto,
  ) {
    return this.productsService.findOne(id, query, req.ability, req.user);
  }

  @TypedRoute.Patch(':id')
  update(
    @TypedParam('id') id: string,
    @TypedBody(validateIUpdateProduct) updateProductDto: IUpdateProductDto,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.productsService.update(
      id,
      updateProductDto,
      req.ability,
      req.user,
    );
  }

  @TypedRoute.Delete(':id')
  remove(
    @TypedParam('id') id: string,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.productsService.remove(id, req.ability);
  }
}
