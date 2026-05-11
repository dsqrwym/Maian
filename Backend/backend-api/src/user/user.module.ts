import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service.js';
import { CheckUserController } from './controllers/check-user.controller.js';
import { FindUserController } from './controllers/find-user.controller.js';
import { FindUserService } from './services/find-user.service.js';
import { RouterModule } from '@nestjs/core';

@Module({
  imports: [
    RouterModule.register([
      {
        path: 'user',
        module: UserModule,
      },
    ]),
  ],
  controllers: [CheckUserController, FindUserController],
  providers: [CheckUserService, FindUserService],
})
export class UserModule {}
