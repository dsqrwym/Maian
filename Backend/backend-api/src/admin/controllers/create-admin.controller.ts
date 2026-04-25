import { Controller, UseGuards } from '@nestjs/common';
import { CreateAdminService } from '../services/create-admin.service';
import { ICreateAdminDto, validateCreateAdmin } from '../dto/create-admin.dto';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '@/auth/guard/auth.guard';
import { RolesAllowed } from '@/common/guards/decorator/roles-allowed.decorator';
import { UserRole } from 'src/generated/drizzle/enums';
import { TypedBody } from '@/utils/typia/typed-body.typia';
import { TypedRoute } from '@nestia/core';

/**
 * Controller for creating new admin users
 * Requires ADMIN role and valid JWT token
 *
 * @endpoint POST /create-admin
 */
@ApiTags('Admin Management')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@RolesAllowed(UserRole.SUPERADMIN)
@Controller('create-admin')
export class CreateAdminController {
  constructor(private readonly createAdminService: CreateAdminService) {}

  /**
   * Create a new admin user
   *
   * This endpoint creates a new admin user with PENDING_VERIFICATION status.
   * An email with verification link will be sent to the provided email address.
   * The verification link is valid for 7 days.
   *
   * @param dto - Contains email and optional username for the new admin
   * @returns Empty response with 201 status code on success
   */
  @TypedRoute.Post()
  async createAdmin(
    @TypedBody(validateCreateAdmin) dto: ICreateAdminDto,
  ): Promise<void> {
    return this.createAdminService.createAdmin(dto);
  }
}
