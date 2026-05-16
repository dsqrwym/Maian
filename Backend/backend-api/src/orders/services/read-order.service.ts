import { Injectable } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';

@Injectable()
export class ReadOrderService {
  constructor(private readonly drizzle: DrizzleService) {}
}
