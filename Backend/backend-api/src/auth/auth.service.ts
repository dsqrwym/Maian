import { Injectable } from '@nestjs/common';
import { MailService } from 'src/mail/mail.service';




@Injectable()
export class AuthService{
    constructor(private mailService: MailService) {}

    async verifyEmail(token: string) {
        try{
            
        }catch(e){
            console.log(e);
        }
    }
}