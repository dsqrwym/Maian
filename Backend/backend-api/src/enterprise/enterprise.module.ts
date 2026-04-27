import { Module } from '@nestjs/common';
import { CreateEmployeeService } from './services/create-employee.service.js';
import { CreateEmployeeController } from './controllers/create-employee.controller.js';
import { RouterModule } from '@nestjs/core';

@Module({
  imports: [
    RouterModule.register([{ path: 'enterprise', module: EnterpriseModule }]),
  ],
  controllers: [CreateEmployeeController],
  providers: [CreateEmployeeService],
})
export class EnterpriseModule {}
