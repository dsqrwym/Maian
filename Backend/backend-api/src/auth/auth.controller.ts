import { Controller, Post, Get, Query, Body, Res, Req } from "@nestjs/common";
import { AuthService } from "./auth.service";
import { RegisterDto } from "./dto/register.dto";
import { ApiBearerAuth, ApiBody, ApiOperation, ApiQuery, ApiResponse, ApiTags } from "@nestjs/swagger";
import { FastifyReply, FastifyRequest } from 'fastify';

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
}