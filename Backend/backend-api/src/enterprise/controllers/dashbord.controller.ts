import { Controller, Req, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '#/auth/guard/auth.guard.js';
import { RolesGuard } from '#/common/guards/roles.guard.js';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { FastifyRequest } from 'fastify';
import { IDashboardResponse } from '#/enterprise/dto/dashbord-response.dto.js';
import {
  IDashboardQuery,
  validateDashboardQuery,
} from '#/enterprise/dto/dashbord-query.dto.js';
import { DashboardService } from '#/enterprise/services/dashbord.service.js';

@ApiTags('Enterprise Dashboard')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('dashboard')
/**
 * Controller for enterprise dashboard metrics.
 *
 * @class DashboardController
 */
export class DashboardController {
  constructor(private readonly dashboardService: DashboardService) {}

  /**
   * Get dashboard data for the authenticated enterprise user.
   *
   * @param {IDashboardQuery} query - Dashboard filter query
   * @param {FastifyRequest} req - Request object containing the authenticated enterprise user
   * @returns {Promise<IDashboardResponse>} Dashboard metrics and summaries
   */
  @TypedRoute.Get()
  async getDashboard(
    @TypedQuery(validateDashboardQuery) query: IDashboardQuery,
    @Req() req: FastifyRequest,
  ): Promise<IDashboardResponse> {
    return this.dashboardService.getDashboard(
      req.user.wholesalerId ?? req.user.userId,
      query,
    );
  }
}
