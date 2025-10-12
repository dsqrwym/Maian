import { Module, OnModuleInit } from '@nestjs/common';
import { CreateAdminService } from './services/create-admin.service';
import { CreateAdminController } from './controllers/create-admin.controller';
import { EnsureSuperAdminService } from './services/ensure-superadmin.service';

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
