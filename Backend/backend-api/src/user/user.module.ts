import { Module } from '@nestjs/common';
import { CheckUserService } from './services/check-user.service';
import { CheckUserController } from './controllers/check-user.controller';
import { FindUserController } from './controllers/find-user.controller';
import { FindUserService } from './services/find-user.service';
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
