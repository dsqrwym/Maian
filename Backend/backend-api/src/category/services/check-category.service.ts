import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import {
  ICheckCategoryNameCreateQueryDto,
  ICheckCategoryNameUpdateQueryDto,
} from '../dto/check-category-query.dto';
@Injectable()
export class CheckCategoryService {
  constructor(private readonly prisma: PrismaService) {}

  async checkNameUsedForCreate(query: ICheckCategoryNameCreateQueryDto) {
    const { name, userId } = query;
    const category = await this.prisma.categories.findFirst({
      where: {
        name,
        ...(userId ? { user_id: userId } : { user_id: null }),
      },
      select: { id: true },
    });
    return !!category;
  }

  async checkNameUsedForUpdate(query: ICheckCategoryNameUpdateQueryDto) {
    const { id, name, userId } = query;
    const excludeId = BigInt(id);
    const category = await this.prisma.categories.findFirst({
      where: {
        name,
        ...(userId ? { user_id: userId } : { user_id: null }),
        NOT: { id: excludeId },
      },
      select: { id: true },
    });
    return !!category;
  }
}
