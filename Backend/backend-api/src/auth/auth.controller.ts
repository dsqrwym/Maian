import { Controller, Post, Get, Query, Body } from "@nestjs/common";
import { AuthService } from "./auth.service";
import { RegisterDto } from "./dto/register.dto";

@Controller('api/auth')
export class AuthController {
    constructor(private authService: AuthService) {}

    @Get('verify-email')
    async getVerifyEmail(@Query('token') token: string) {
        return this.authService.verifyEmail(token);
    }

    @Post('register')
    async register(@Body() body : RegisterDto) {
        return this.authService.register(body);
    }
}