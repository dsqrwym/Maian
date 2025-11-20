import { ForbiddenException, Injectable } from '@nestjs/common';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { AppAbility } from '../casl/casl-types';
import { Action } from '../casl/actions';
import { subject } from '@casl/ability';
import { PrismaService } from '../prisma/prisma.service';
import { UserPayload } from '../auth/auth.types';
import { computePrice } from '../utils/calculate/computePrice';
import { ProductQueryDto } from './dto/product-query.dto';
import { accessibleBy } from '@casl/prisma';
import { Prisma } from '@prisma/client';
import { Logger } from 'nestjs-pino';
import { ProductListSelectField } from './product.enums';
import { ToPaginated } from '../common/types/response.type';

@Injectable()
export class ProductsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly logger: Logger,
  ) {}
  async create(
    createProductDto: CreateProductDto,
    ability: AppAbility,
    user: UserPayload,
  ) {
    const {
      user_id,
      primary_category_id,
      product_code,
      description,
      iva,
      name,
      title,
      variants,
      translations,
      files,
    } = createProductDto;

    if (!ability.can(Action.Create, subject('products', { user_id }))) {
      throw new ForbiddenException(
        'You are not allowed to create products that do not belong to you or your company',
      );
    }

    await this.prisma.products.create({
      data: {
        iva,
        name,
        title,
        user_id,
        description,
        product_code,
        created_by: user.userId,
        product_categories: {
          create: { category_id: primary_category_id, is_primary: true },
        },
        variant_products: {
          createMany: {
            data: variants.map((variant) => ({
              type_sale: variant.type_sale,
              sort: variant.sort,
              iva: variant.iva ?? iva,
              ...computePrice(
                variant.price,
                variant.price_iva,
                variant.iva ?? iva,
              ),
              product_code: variant.product_code,
              min_order_qty: variant.min_order_qty,
              sale_uni_qty: variant.sale_unit_qty,
              available_stock: variant.available_stock,
              low_stock_threshold: variant.low_stock_threshold,
              created_by: user.userId,
            })),
          },
        },
        ...(translations && {
          product_translations: {
            create: translations.map((translation) => ({
              lang_code: translation.lang_code,
              name: translation.name,
              description: translation.description,
            })),
          },
        }),
        ...(files && {
          product_files: {
            create: files.map((file) => ({
              file_id: file.file_id,
              sort: file.sort,
            })),
          },
        }),
      },
    });
  }

  async findAll(query: ProductQueryDto, ability: AppAbility) {
    if (!ability.can(Action.Read, 'products')) {
      throw new ForbiddenException(
        'You do not have permission to read products',
      );
    }

    const permissionCondition: Prisma.productsWhereInput = accessibleBy(
      ability,
      Action.Read,
    ).products;

    this.logger.log('permissionCondition', permissionCondition);

    const { search, langCode, category_id, wholesaler_id, status } = query;
    const fields = query.fields;
    const iva = fields?.includes(ProductListSelectField.IVA);
    const selectedStatus = fields?.includes(ProductListSelectField.STATUS);
    const user_id = fields?.includes(ProductListSelectField.USER_ID);
    const category = fields?.includes(ProductListSelectField.CATEGORY);

    const select: Prisma.productsSelect = {
      id: true,
      name: true,
      title: true,
      product_code: true,
      product_translations: {
        select: { lang_code: true, name: true },
        where: { ...(langCode && { lang_code: langCode }) },
      },
      ...(iva && { iva }),
      ...(selectedStatus && { status: true }),
      ...(user_id && { user_id }),
      ...(category && { category }),
      ...(category && {
        product_categories: {
          select: {
            categories: {
              select: {
                id: true,
                name: true,
              },
            },
          },
          where: { is_primary: true },
        },
      }),
    };

    const andClauses: Prisma.productsWhereInput[] = [permissionCondition];

    if (search) {
      andClauses.push({
        OR: [
          { name_unaccent: { contains: search, mode: 'insensitive' } },
          { title_unaccent: { contains: search, mode: 'insensitive' } },
          { product_code: { contains: search, mode: 'insensitive' } },
        ],
      });
    }

    if (category_id != undefined) {
      andClauses.push({
        product_categories: {
          some: {
            category_id,
          },
        },
      });
    }

    if (wholesaler_id != undefined) {
      andClauses.push({
        user_id: wholesaler_id,
      });
    }

    if (status != undefined) {
      andClauses.push({ status });
    }

    const { page, limit } = query;
    const { sort_by, sort_order } = query;

    const orderBy: Prisma.productsOrderByWithRelationInput = {
      [sort_by]: sort_order,
    };

    const [products, count] = await Promise.all([
      this.prisma.products.findMany({
        select,
        where: { AND: andClauses },
        orderBy,
        skip: (page - 1) * limit,
        take: limit,
      }),
      this.prisma.products.count({
        where: { AND: andClauses },
      }),
    ]);

    const result: ToPaginated = {
      items: products,
      meta: { total: count, page, limit },
    };

    return result;
  }

  findOne(id: number) {
    return `This action returns a #${id} product`;
  }

  update(id: number, updateProductDto: UpdateProductDto) {
    return `This action updates a #${id} product`;
  }

  remove(id: number) {
    return `This action removes a #${id} product`;
  }
}
