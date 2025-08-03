import { registerDecorator, ValidationOptions } from 'class-validator';
import { IanaTimezoneValidator } from '../../validators/is-iana.validator'; // 引入自定义验证器类

export function IsIANA(validationOptions?: ValidationOptions) {
  return function (object: object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName: propertyName,
      options: validationOptions,
      constraints: [],
      validator: IanaTimezoneValidator, // 这里指向你的验证器类
    });
  };
}
