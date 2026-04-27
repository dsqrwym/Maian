import { Module, OnModuleInit } from '@nestjs/common';
import { CreateAdminService } from './services/create-admin.service.js';
import { CreateAdminController } from './controllers/create-admin.controller.js';
import { EnsureSuperAdminService } from './services/ensure-superadmin.service.js';

@Module({
  controllers: [CreateAdminController],
  providers: [CreateAdminService, EnsureSuperAdminService],
})
export class AdminModule implements OnModuleInit {
  constructor(private readonly ensureService: EnsureSuperAdminService) {}
  async onModuleInit() {
    await this.ensureService.ensureSuperAdmin();
  }
}
