import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service.js';
import { CheckUserController } from './controllers/check-user.controller.js';
import { FindUserController } from './controllers/find-user.controller.js';
import { FindUserService } from './services/find-user.service.js';
import { ReadWholesalerService } from './services/read-wholesaler.service.js';
import { ReadWholesalerController } from '#/user/controllers/read-wholesaler.controller.js';
import { RetailerProfileController } from '#/user/controllers/retailer-profile.controller.js';
import { RetailerProfileService } from '#/user/services/retailer-profile.service.js';

@Module({
  imports: [],
  controllers: [
    CheckUserController,
    FindUserController,
    ReadWholesalerController,
    RetailerProfileController,
  ],
  providers: [
    CheckUserService,
    FindUserService,
    ReadWholesalerService,
    RetailerProfileService,
  ],
})
export class UserModule {}
