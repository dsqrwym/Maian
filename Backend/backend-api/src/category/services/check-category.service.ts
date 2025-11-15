import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import {
  CheckCategoryNameCreateQueryDto,
  CheckCategoryNameUpdateQueryDto,
} from '../dto/check-category-query.dto';

@Injectable()
export class CheckCategoryService {
  constructor(private readonly prisma: PrismaService) {}

  async checkNameUsedForCreate(query: CheckCategoryNameCreateQueryDto) {
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

  async checkNameUsedForUpdate(query: CheckCategoryNameUpdateQueryDto) {
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
