import { Module } from '@nestjs/common';
import { LocationsController } from './locations.controller.js';
import { LocationsService } from './locations.service.js';

@Module({
  providers: [LocationsService],
  controllers: [LocationsController],
})
export class LocationsModule {}
