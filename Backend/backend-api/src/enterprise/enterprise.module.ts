import { Module } from '@nestjs/common';
import { CreateEmployeeService } from './services/create-employee.service';
import { CreateEmployeeController } from './controllers/create-employee.controller';
import { RouterModule } from '@nestjs/core';

@Module({
  imports: [
    RouterModule.register([{ path: 'enterprise', module: EnterpriseModule }]),
  ],
  controllers: [CreateEmployeeController],
  providers: [CreateEmployeeService],
})
export class EnterpriseModule {}
