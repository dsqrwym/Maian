import { registerDecorator, ValidationOptions } from 'class-validator';
import { Bcp47LanguageValidator } from '../is-bcp47-language.validator';

export function IsBCP47Language(validationOptions?: ValidationOptions) {
  return function (object: object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName: propertyName,
      options: validationOptions,
      constraints: [],
      validator: Bcp47LanguageValidator,
    });
  };
}
