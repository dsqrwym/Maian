import { Module } from '@nestjs/common';
import { WriteEmployeeService } from './services/write-employee.service.js';
import { WriteEmployeeController } from './controllers/write-employee.controller.js';
import { WholesalerProfileController } from '#/enterprise/controllers/wholesaler-profile.controller.js';
import { WholesalerProfileService } from '#/enterprise/services/wholesaler-profile.service.js';
import { DrizzleModule } from '#/drizzle/drizzle.module.js';
import { ReadEmployeeService } from '#/enterprise/services/read-employee.service.js';
import { ReadEmployeeController } from '#/enterprise/controllers/read-employee.controller.js';

@Module({
  imports: [DrizzleModule],
  controllers: [
    WriteEmployeeController,
    WholesalerProfileController,
    ReadEmployeeController,
  ],
  providers: [
    WriteEmployeeService,
    WholesalerProfileService,
    ReadEmployeeService,
  ],
})
export class EnterpriseModule {}
