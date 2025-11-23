import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  UseGuards,
  Req,
  Query,
} from '@nestjs/common';
import { ProductsService } from './products.service';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { JwtAuthGuard } from '../auth/guard/auth.guard';
import { FastifyRequest } from 'fastify';
import { ProductListQueryDto } from './dto/product-list-query.dto';
import { ProductQueryDto } from './dto/product-query.dto';

@UseGuards(JwtAuthGuard)
@Controller('products')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Post()
  create(
    @Body() createProductDto: CreateProductDto,
    @Req() req: FastifyRequest,
  ) {
    return this.productsService.create(createProductDto, req.ability, req.user);
  }

  @Get()
  async findAll(
    @Query() query: ProductListQueryDto,
    @Req() req: FastifyRequest,
  ) {
    return this.productsService.findAll(query, req.ability);
  }

  @Get(':id')
  findOne(
    @Param('id') id: string,
    @Req() req: FastifyRequest,
    @Query() query: ProductQueryDto,
  ) {
    return this.productsService.findOne(id, query, req.ability, req.user);
  }

  @Patch(':id')
  update(
    @Param('id') id: string,
    @Body() updateProductDto: UpdateProductDto,
    @Req() req: FastifyRequest,
  ) {
    return this.productsService.update(
      id,
      updateProductDto,
      req.ability,
      req.user,
    );
  }

  @Delete(':id')
  remove(@Param('id') id: string, @Req() req: FastifyRequest) {
    return this.productsService.remove(id, req.ability);
  }
}
