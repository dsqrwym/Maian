import {
  Controller,
  Post,
  Get,
  Query,
  Body,
  Res,
  UseGuards,
  Req,
  BadRequestException,
  UnauthorizedException,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/register.dto';
import {
  ApiBearerAuth,
  ApiBody,
  ApiExtraModels,
  ApiOperation,
  ApiQuery,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { FastifyReply, FastifyRequest } from 'fastify';
import { LoginDto } from './dto/login.dto';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { JwtAuthGuard, LocalAuthGuard } from './guard/auth.guard';

@ApiTags('Authentication')
@Controller('auth')
@ApiExtraModels(RegisterDto, LoginDto, DeleteSessionDto)
export class AuthController {
  constructor(private authService: AuthService) {}

  @Get('verify-email')
  @ApiOperation({ summary: 'Verify email address' })
  @ApiQuery({
    name: 'token',
    required: true,
    description: 'JWT token from email',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @ApiQuery({
    name: 'lang',
    required: false,
    description: 'Language code (default: en)',
    example: 'en',
  })
  @ApiResponse({
    status: 200,
    description: 'Returns HTML verification result page',
    content: {
      'text/html': {
        schema: {
          type: 'string',
          example:
            '<html lang="en"><body>Email verified successfully.</body></html>',
        },
      },
    },
  })
  @ApiResponse({ status: 400, description: 'Invalid or expired token' })
  async getVerifyEmail(
    @Query('lang') lang: string,
    @Query('token') token: string,
    @Res() res: FastifyReply,
  ) {
    return this.authService.verifyEmail(token, lang, res);
  }

  @Post('register')
  @ApiOperation({ summary: 'Register new user' })
  @ApiBody({ type: RegisterDto })
  @ApiResponse({
    status: 201,
    description: 'User successfully registered',
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid input data',
  })
  @ApiResponse({
    status: 409,
    description: 'Email already exists',
  })
  async register(@Body() body: RegisterDto) {
    return this.authService.register(body);
  }

  @Post('login')
  @ApiOperation({ summary: 'Log in a user' })
  @ApiBody({
    description: 'User login credentials',
    type: LoginDto,
    examples: {
      example1: {
        summary: 'Valid login data',
        value: {
          email: 'user@example.com',
          password: 'StrongPassword123!',
          deviceName: 'Chrome Browser',
          ipAddress: '192.168.1.1',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
        },
      },
    },
  })
  @ApiResponse({
    status: 200,
    description: 'User successfully logged in',
    schema: {
      example: {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid login credentials',
  })
  @UseGuards(LocalAuthGuard)
  async login(@Req() req: FastifyRequest, @Body() body: LoginDto) {
    const user = req.user.authenticatedUser;
    if (!user) {
      throw new BadRequestException('User authentication failed');
    }

    return this.authService.login(user, body);
  }

  @Post('refresh-token')
  @ApiOperation({ summary: 'Get new access token using refresh token' })
  @ApiBody({
    schema: {
      type: 'object',
      properties: {
        refreshToken: {
          type: 'string',
          example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        },
      },
    },
  })
  @ApiResponse({
    status: 200,
    description: 'Returns new access token',
    schema: {
      example: {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Invalid or expired refresh token' })
  async getAccessToken(@Body('refreshToken') refreshToken: string) {
    return this.authService.getAccessToken(refreshToken);
  }

  @Post('logout')
  @ApiOperation({ summary: 'Logout current session' })
  @ApiResponse({ status: 200, description: 'Successfully logged out' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  async logout(@Req() req: FastifyRequest) {
    const payload = req.user.authTokenPayload;

    if (!payload) {
      throw new UnauthorizedException('No valid token payload found');
    }

    return this.authService.logoutSession(payload);
  }

  @Post('delete-session')
  @ApiOperation({ summary: 'Delete a specific session' })
  @ApiBody({ type: DeleteSessionDto })
  @ApiResponse({ status: 200, description: 'Session deleted successfully' })
  @ApiResponse({ status: 400, description: 'Invalid session ID or input' })
  async deleteSession(@Body() deleteSessionDto: DeleteSessionDto) {
    return this.authService.deleteSession(deleteSessionDto);
  }
}
