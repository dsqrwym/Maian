import { Injectable } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { ICreateOrderDto } from '#/orders/dto/create-order.dto.js';
import { alias } from 'drizzle-orm/pg-core';
import { users } from '#/generated/drizzle/schema.js';

@Injectable()
export class WriteOrderService {
  constructor(private readonly drizzle: DrizzleService) {}



  async createFromCart(retailerId: string, dto: ICreateOrderDto) {
    const { wholesalerId } = dto;
  }
}
