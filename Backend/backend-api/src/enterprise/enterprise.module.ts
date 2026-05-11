import { Module } from '@nestjs/common';
import { CreateEmployeeService } from './services/create-employee.service.js';
import { CreateEmployeeController } from './controllers/create-employee.controller.js';
import { RouterModule } from '@nestjs/core';
import { WholesalerProfileController } from '#/enterprise/controllers/wholesaler-profile.controller.js';
import { WholesalerProfileService } from '#/enterprise/services/wholesaler-profile.service.js';

@Module({
  imports: [
    RouterModule.register([{ path: 'enterprise', module: EnterpriseModule }]),
  ],
  controllers: [CreateEmployeeController, WholesalerProfileController],
  providers: [CreateEmployeeService, WholesalerProfileService],
})
export class EnterpriseModule {}
