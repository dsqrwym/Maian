import { Controller, HttpCode, Req, Res } from '@nestjs/common';
import { ApiExtraModels, ApiTags } from '@nestjs/swagger';
import { ILoginDto, validateLogin } from '../dto/login.dto';
import { TokenResponseDto } from '../dto/token-response.dto';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { AuthService } from '../auth.service';
import { UserRole } from 'src/generated/drizzle/enums';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from 'src/utils/typia/typed-body.typia';
import { LoginResponseDto } from '../dto/login-response.dto';

@ApiTags('Authentication')
@ApiExtraModels(TokenResponseDto)
@Controller('login')
export class LoginController {
  constructor(private readonly authService: AuthService) {}

  @TypedRoute.Post('standard')
  @HttpCode(200)
  async loginStandard(
    @Req() req: FastifyRequest,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.login(req, body, [UserRole.RETAILER]);
  }

  @TypedRoute.Post('standard/web')
  @HttpCode(200)
  async loginStandardWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.loginWeb(req, res, body, [UserRole.RETAILER]);
  }

  @TypedRoute.Post('enterprise')
  @HttpCode(200)
  async loginEnterprise(
    @Req() req: FastifyRequest,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.login(req, body, [
      UserRole.WHOLESALER,
      UserRole.DELIVERY,
      UserRole.SUPPORT,
      UserRole.WAREHOUSE,
    ]);
  }

  @TypedRoute.Post('enterprise/web')
  @HttpCode(200)
  async loginEnterpriseWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.loginWeb(req, res, body, [
      UserRole.WHOLESALER,
      UserRole.DELIVERY,
      UserRole.SUPPORT,
      UserRole.WAREHOUSE,
    ]);
  }

  @TypedRoute.Post('admin')
  @HttpCode(200)
  async loginAdmin(
    @Req() req: FastifyRequest,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.login(req, body, [
      UserRole.ADMIN,
      UserRole.SUPERADMIN,
    ]);
  }

  @TypedRoute.Post('admin/web')
  @HttpCode(200)
  async loginAdminWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.loginWeb(req, res, body, [
      UserRole.ADMIN,
      UserRole.SUPERADMIN,
    ]);
  }
}
