import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service.js';
import { CheckUserController } from './controllers/check-user.controller.js';
import { FindUserController } from './controllers/find-user.controller.js';
import { FindUserService } from './services/find-user.service.js';
import { RouterModule } from '@nestjs/core';
import { WholesalerProfileService } from './services/wholesaler-profile.service.js';
import { WholesalerProfileController } from './controllers/wholesaler-profile.controller.js';

@Module({
  imports: [
    RouterModule.register([
      {
        path: 'user',
        module: UserModule,
      },
    ]),
  ],
  controllers: [
    CheckUserController,
    FindUserController,
    WholesalerProfileController,
  ],
  providers: [CheckUserService, FindUserService, WholesalerProfileService],
})
export class UserModule {}
