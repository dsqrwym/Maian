import { AddressType } from 'src/generated/drizzle/enums';
import typia, { tags } from 'typia';
import {
  TagsLatitude,
  TagsLongitude,
} from '../../utils/typia/validators/direction.validator';
import { isObject } from '../../utils/is.utils';
import { cleanString } from '../../utils/string.util';

export interface IDirectionDto {
  /**
   * 地址类型
   */
  type?: (AddressType & tags.Example<'STORE'>) | null; // 默认值需要在业务层处理

  /**
   * 街道地址
   */
  street: string & tags.MaxLength<200> & tags.Example<'123 Main St, Apt 4B'>;

  /**
   * 城市ID
   */
  city: number & tags.Minimum<1> & tags.Example<1>;

  /**
   * 省份ID
   */
  province: number & tags.Minimum<1> & tags.Example<2>;

  /**
   * 国家（ISO numeric）
   */
  country: number & tags.Minimum<1> & tags.Maximum<999> & tags.Example<724>;

  /**
   * 邮编
   */
  zipCode: string & tags.MaxLength<10>;

  /**
   * 纬度
   */
  latitude?: TagsLatitude | null;

  /**
   * 经度
   */
  longitude?: TagsLongitude | null;
}

export const validateDirection = (input: unknown) => {
  if (isObject(input)) {
    const obj = input;
    if (typeof obj.street === 'string') {
      obj.street = cleanString(obj.street);
    }
  }
  const typedInput = typia.assertEquals<IDirectionDto>(input);
  typedInput.type = typedInput.type ?? AddressType.STORE;
  return typedInput;
};
