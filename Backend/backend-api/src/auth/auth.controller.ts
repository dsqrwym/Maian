import { Controller, Post, Get, Query, Body, Res, Req, Param } from "@nestjs/common";
import { AuthService } from "./auth.service";
import { RegisterDto } from "./dto/register.dto";
import { ApiBearerAuth, ApiBody, ApiOperation, ApiQuery, ApiResponse, ApiTags } from "@nestjs/swagger";
import { FastifyReply, FastifyRequest } from 'fastify';
import { LoginDto } from "./dto/login.dto";

@ApiTags('Authentication')
@ApiBearerAuth()
@Controller('auth')
export class AuthController {
    constructor(private authService: AuthService) { }

    @Get('verify-email')
    @ApiOperation({ summary: 'Verify email address' })
    @ApiQuery({
        name: 'token',
        required: true,
        description: "JWT token from email",
        example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
    })
    @ApiQuery({
        name: 'lang',
        required: false,
        description: "Language code (default: en)",
        example: 'en',
    })
    @ApiResponse({
        status: 200,
        description: 'Returns HTML verification result page',
        content: {
            'text/html': {
                schema: {
                    type: 'string',
                    example: '<html><body>Email verified successfully.</body></html>',
                },
            },
        },
    })
    async getVerifyEmail(@Query('lang') lang: string, @Query('token') token: string, @Res() res: FastifyReply) {
        return this.authService.verifyEmail(token, lang, res);
    }

    @Post('register')
    @ApiOperation({ summary: 'Register new user' })
    @ApiBody({ type: RegisterDto })
    @ApiResponse({
        status: 201,
        description: 'User successfully registered'
    })
    @ApiResponse({
        status: 400,
        description: 'Invalid input data'
    })
    @ApiResponse({
        status: 409,
        description: 'Email already exists'
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
    async login(@Body() body: LoginDto) {
        return this.authService.login(body);
    }

    @Get('refresh-token')
    @ApiOperation({ summary: 'Get a new access token using a refresh token' })
    @ApiQuery({
        name: 'refreshToken',
        required: true,
        description: 'The refresh token issued during login',
        example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
    })
    @ApiResponse({
        status: 200,
        description: 'New access token generated successfully',
        schema: {
            example: {
                accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
            },
        },
    })
    @ApiResponse({
        status: 401,
        description: 'Invalid or expired refresh token',
    })
    async getAccessToken(@Query() refreshToken: string) {
        return this.authService.getAccessToken(refreshToken);
    }
}