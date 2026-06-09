/*
import {
  registerDecorator,
  ValidationArguments,
  ValidationOptions,
  ValidatorConstraint,
  ValidatorConstraintInterface,
} from 'class-validator';

@ValidatorConstraint({ name: 'AtLeastOneOf', async: false })
export class AtLeastOneOfConstraint implements ValidatorConstraintInterface {
  validate(value: any, args: ValidationArguments) {
    const propertyNames = args.constraints; // 获取传递给装饰器的字段名数组
    const object = args.object;

    // 检查属性列表中至少有一个字段不是 undefined 且不是 null
    return propertyNames.some((property: string) => {
      const fieldValue: unknown = object[property];
      // 示例：检查字段值既不是 undefined 也不是 null 且不为空字符串
      return (
        fieldValue !== undefined && fieldValue !== null && fieldValue !== ''
      );
    });
  }

  defaultMessage(args: ValidationArguments) {
    const propertyNames = args.constraints;
    return `At least one of the following fields must be provided and not empty: ${propertyNames.join(
      ', ',
    )}`;
  }
}
/!**
 * 验证目标类中指定的字段列表中至少有一个字段不为空 (undefined/null/'')
 *
 * @param properties 要检查的字段名数组
 * @param validationOptions 验证选项
 *!/
export function AtLeastOneOf(
  properties: string[],
  validationOptions?: ValidationOptions,
) {
  return function (object: Record<string, any>, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName: propertyName, // 不需要 propertyName，因为是类级别验证
      options: validationOptions,
      constraints: properties, // 传递字段名数组到约束中
      validator: AtLeastOneOfConstraint,
    });
  };
}
*/
