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

/**
 * Controller for user authentication via login
 * @class LoginController
 */
@ApiTags('Authentication')
@ApiExtraModels(TokenResponseDto)
@Controller('login')
export class LoginController {
  constructor(private readonly authService: AuthService) {}

  /**
   * Login for standard (retailer) users via native app.
   *
   * Validates credentials and returns access/refresh tokens in the response body.
   *
   * @param {FastifyRequest} req - Request object
   * @param {ILoginDto} body - Login credentials (email, password, deviceName)
   * @returns {Promise<LoginResponseDto>} Access token, refresh token, and user payload
   */
  @TypedRoute.Post('standard')
  @HttpCode(200)
  async loginStandard(
    @Req() req: FastifyRequest,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.login(req, body, [UserRole.RETAILER]);
  }

  /**
   * Login for standard (retailer) users via web browser.
   *
   * Sets the refresh token as an httpOnly cookie and returns a CSRF token
   * in the response body instead of the raw refresh token.
   *
   * @param {FastifyRequest} req - Request object
   * @param {FastifyReply} res - Response object for setting cookie
   * @param {ILoginDto} body - Login credentials (email, password, deviceName)
   * @returns {Promise<LoginResponseDto>} Access token, CSRF token, and user payload
   */
  @TypedRoute.Post('standard/web')
  @HttpCode(200)
  async loginStandardWeb(
    @Req() req: FastifyRequest,
    @Res({ passthrough: true }) res: FastifyReply,
    @TypedBody(validateLogin) body: ILoginDto,
  ): Promise<LoginResponseDto> {
    return await this.authService.loginWeb(req, res, body, [UserRole.RETAILER]);
  }

  /**
   * Login for enterprise users via native app.
   *
   * Allows WHOLESALER, DELIVERY, SUPPORT, and WAREHOUSE roles.
   * Returns access/refresh tokens in the response body.
   *
   * @param {FastifyRequest} req - Request object
   * @param {ILoginDto} body - Login credentials (email, password, deviceName)
   * @returns {Promise<LoginResponseDto>} Access token, refresh token, and user payload
   */
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

  /**
   * Login for enterprise users via web browser.
   *
   * Allows WHOLESALER, DELIVERY, SUPPORT, and WAREHOUSE roles.
   * Sets the refresh token as an httpOnly cookie and returns a CSRF token.
   *
   * @param {FastifyRequest} req - Request object
   * @param {FastifyReply} res - Response object for setting cookie
   * @param {ILoginDto} body - Login credentials (email, password, deviceName)
   * @returns {Promise<LoginResponseDto>} Access token, CSRF token, and user payload
   */
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

  /**
   * Login for admin users via native app.
   *
   * Allows ADMIN and SUPERADMIN roles.
   * Returns access/refresh tokens in the response body.
   *
   * @param {FastifyRequest} req - Request object
   * @param {ILoginDto} body - Login credentials (email, password, deviceName)
   * @returns {Promise<LoginResponseDto>} Access token, refresh token, and user payload
   */
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

  /**
   * Login for admin users via web browser.
   *
   * Allows ADMIN and SUPERADMIN roles.
   * Sets the refresh token as an httpOnly cookie and returns a CSRF token.
   *
   * @param {FastifyRequest} req - Request object
   * @param {FastifyReply} res - Response object for setting cookie
   * @param {ILoginDto} body - Login credentials (email, password, deviceName)
   * @returns {Promise<LoginResponseDto>} Access token, CSRF token, and user payload
   */
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
