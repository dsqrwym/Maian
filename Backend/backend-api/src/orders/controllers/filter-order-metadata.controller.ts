import { Controller, Req, UseGuards } from '@nestjs/common';
import { FilterOrderMetadataService } from '#/orders/services/filter-order-metadata.service.js';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import { TypedRoute } from '@nestia/core';
import { UserRole } from '#/generated/drizzle/enums.js';
import { RolesAllowed } from '#/common/guards/decorator/roles-allowed.decorator.js';
import { FastifyRequest } from 'fastify';
import { WHOLESALER_ROLES } from '#/enterprise/enterprise.constants.js';

@ApiTags('Orders')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('filter-metadata')
/**
 * Controller for order filter metadata used by order search screens.
 *
 * @class FilterOrderMetadataController
 */
export class FilterOrderMetadataController {
  constructor(
    private readonly filterOrderMetadataService: FilterOrderMetadataService,
  ) {}

  /**
   * Get order filter metadata for the authenticated retailer.
   *
   * @param {FastifyRequest} req - Request object containing the authenticated retailer
   * @returns Order totals and item count boundaries for retailer filters
   */
  @TypedRoute.Get('standard')
  @RolesAllowed(UserRole.RETAILER)
  async getFilterOrderMetadataByRetailer(@Req() req: FastifyRequest): Promise<{
    max_total: string | null;
    min_total: string | null;
    max_subtotal: string | null;
    min_subtotal: string | null;
    max_iva_total: string | null;
    min_iva_total: string | null;
    max_item_count: number | null;
    min_item_count: number | null;
  }> {
    return this.filterOrderMetadataService.getFilterOrderMetadata(
      req.ability,
      req.user.userId,
    );
  }

  /**
   * Get order filter metadata for the authenticated wholesaler.
   *
   * @param {FastifyRequest} req - Request object containing the authenticated wholesaler
   * @returns Order totals and item count boundaries for wholesaler filters
   */
  @TypedRoute.Get('enterprise')
  @RolesAllowed(...WHOLESALER_ROLES)
  async getFilterOrderMetadataByWholesaler(
    @Req() req: FastifyRequest,
  ): Promise<{
    max_total: string | null;
    min_total: string | null;
    max_subtotal: string | null;
    min_subtotal: string | null;
    max_iva_total: string | null;
    min_iva_total: string | null;
    max_item_count: number | null;
    min_item_count: number | null;
  }> {
    return this.filterOrderMetadataService.getFilterOrderMetadata(
      req.ability,
      undefined,
      req.user.wholesalerId ?? req.user.userId,
    );
  }
}
