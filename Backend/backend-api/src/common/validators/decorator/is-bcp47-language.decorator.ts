import { registerDecorator, ValidationOptions } from 'class-validator';
import { Bcp47LanguageValidator } from '../../validators/is-bcp47-language.validator';

export function IsBCP47Language(validationOptions?: ValidationOptions) {
    return function (object: Object, propertyName: string) {
        registerDecorator({
            target: object.constructor,
            propertyName: propertyName,
            options: validationOptions,
            constraints: [],
            validator: Bcp47LanguageValidator,
        });
    };
}
