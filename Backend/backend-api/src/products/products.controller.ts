import { Controller, UseGuards, Req, Get } from '@nestjs/common';
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
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { TypedParam, TypedQuery, TypedRoute } from '@nestia/core';
import { TypedBody } from '../utils/typia/typed-body.typia';
import { RolesAllowed } from '../common/guards/decorator/roles-allowed.decorator';
import { ADMIN_ROLES } from '../admin/admin.constants';
import { PaginatedDataWithT } from 'src/common/types-interfaces/response.interface';
import { IProductResponse } from './dto/product-response';
import {
  ProductStatus,
  SaleVariant,
  UserRole,
} from '../generated/drizzle/enums';
import { TagsIntegerString } from '../utils/typia/tags/string.tag';

@ApiTags('Product Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('product')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  /**
   * Create a new product.
   *
   * The request body must include at least one variant and can optionally
   * include translations and file references.
   *
   * @param {ICreateProductDto} createProductDto - Product creation payload
   * @param {FastifyRequest} req - Request object with user ability
   * @returns {Promise<void>}
   */
  @TypedRoute.Post()
  create(
    @TypedBody(validateICreateProduct) createProductDto: ICreateProductDto,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.productsService.create(createProductDto, req.ability, req.user);
  }

  /**
   * Search and paginate products with filters.
   *
   * Supports filtering by category, search terms, status, and selective field retrieval.
   *
   * @param {IProductListQueryDto} query - Search and pagination parameters
   * @param {FastifyRequest} req - Request object with user ability
   * @returns {Promise<PaginatedDataWithT<IProductResponse>>} Paginated product list
   */
  @TypedRoute.Get()
  async findAll(
    @TypedQuery(validateProductListQuery) query: IProductListQueryDto,
    @Req() req: FastifyRequest,
  ): Promise<PaginatedDataWithT<IProductResponse>> {
    return this.productsService.findAllUseSqlD(query, req.ability, req.user);
  }

  /**
   * Get product data required for the update form.
   *
   * Returns the current product state including variants, translations,
   * files, and categories. The response can be directly used to populate
   * an edit form.
   *
   * @param {string} id - Product ID
   * @param {FastifyRequest} req - Request object with user ability
   * @returns {Promise<{
   *   products_files: { sort: number; file_id: bigint }[];
   *   name: string;
   *   id: bigint;
   *   title: string | null;
   *   description: string | null;
   *   iva: string;
   *   product_code: string;
   *   status: ProductStatus;
   *   product_categories: {
   *     name: string;
   *     id: bigint;
   *     iva: string | null;
   *     category_translations: { name: string; lang_code: string }[];
   *     is_primary: boolean;
   *   }[];
   *   product_translations: {
   *     name: string;
   *     title: string | null;
   *     description: string | null;
   *     lang_code: string;
   *   }[];
   *   variant_products: {
   *     id: bigint;
   *     status: ProductStatus;
   *     product_code: string;
   *     type_sale: SaleVariant;
   *     price: string;
   *     price_iva: string;
   *     available_stock: number;
   *     sort: number;
   *     low_stock_threshold: number;
   *     min_order_qty: number;
   *   }[];
   * }>}
   */
  @Get(':id/update')
  @RolesAllowed(UserRole.WHOLESALER, UserRole.WAREHOUSE, ...ADMIN_ROLES)
  async getForUpdate(
    @TypedParam('id') id: TagsIntegerString,
    @Req() req: FastifyRequest,
  ): Promise<{
    products_files: {
      sort: number;
      file_id: bigint;
    }[];
    name: string;
    id: bigint;
    title: string | null;
    description: string | null;
    iva: string;
    product_code: string;
    status: ProductStatus;
    product_categories: {
      name: string;
      id: bigint;
      iva: string | null;
      category_translations: { name: string; lang_code: string }[];
      is_primary: boolean;
    }[];
    product_translations: {
      name: string;
      title: string | null;
      description: string | null;
      lang_code: string;
    }[];
    variant_products: {
      id: bigint;
      status: ProductStatus;
      product_code: string;
      type_sale: SaleVariant;
      price: string;
      price_iva: string;
      available_stock: number;
      sort: number;
      low_stock_threshold: number;
      min_order_qty: number;
    }[];
  }> {
    return this.productsService.getForUpdate(id, req.ability);
  }

  /**
   * Update a product (partial / PATCH).
   *
   * **Concurrency control:** This endpoint uses **pessimistic locking** (`SELECT ... FOR UPDATE`).
   * The request does not require a `version` field; the row‑level lock ensures
   * consistency of variant count limits.
   *
   * @param {string} id - Product ID to update
   * @param {IUpdateProductDto} updateProductDto - Fields to update (all optional)
   * @param {FastifyRequest} req - Request object with user ability
   * @returns {Promise<void>}
   */
  @TypedRoute.Patch(':id')
  update(
    @TypedParam('id') id: TagsIntegerString,
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

  /**
   * Delete a product by ID.
   *
   * @param {string} id - Product ID
   * @param {FastifyRequest} req - Request object with user ability
   * @returns {Promise<void>}
   */
  @TypedRoute.Delete(':id')
  remove(
    @TypedParam('id') id: TagsIntegerString,
    @Req() req: FastifyRequest,
  ): Promise<void> {
    return this.productsService.remove(id, req.ability);
  }
}
