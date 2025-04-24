import { Controller, Post, Get, Query } from "@nestjs/common";
import { AuthService } from "./auth.service";

@Controller('auth')
export class AuthController {
    constructor(private authService: AuthService) {}

    @Get('verify-email')
    async getVerifyEmail(@Query('token') token: string) {
        return this.authService.verifyEmail(token);
    }
}