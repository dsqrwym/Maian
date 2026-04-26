import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  private readonly characters =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+[]{}|;:,.<>?';

  generateRandomString(length: number): string {
    let result = '';
    for (let i = 0; i < length; i++) {
      const randomIndex = Math.floor(Math.random() * this.characters.length);
      result += this.characters.charAt(randomIndex);
    }
    return result;
  }

  getHello(): string {
    return 'Hello World!';
  }
}
