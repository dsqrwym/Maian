import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service.js';
import { CheckUserController } from './controllers/check-user.controller.js';
import { FindUserController } from './controllers/find-user.controller.js';
import { FindUserService } from './services/find-user.service.js';
import { ReadWholesalerService } from './services/read-wholesaler.service.js';
import { ReadWholesalerController } from '#/user/controllers/read-wholesaler.controller.js';

@Module({
  imports: [],
  controllers: [
    CheckUserController,
    FindUserController,
    ReadWholesalerController,
  ],
  providers: [CheckUserService, FindUserService, ReadWholesalerService],
})
export class UserModule {}
