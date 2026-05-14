import type { tags } from 'typia';
import type { TagsNotBlank } from '#/utils/typia/tags/string.tag.js';
import type {
  TagsMaxNumberString,
  TagsUNumeric10_2_String,
} from '#/utils/typia/tags/number.tags.js';

export type TagsFirstName = TagsNotBlank & tags.MaxLength<50>;
export type TagsLastName = TagsNotBlank & tags.MaxLength<60>;

/**
 * Spanish tax/person/business identifier basic format.
 * 西班牙税号/身份号基础格式。
 *
 * 支持：
 * - DNI/NIF: 12345678Z
 * - NIE: X1234567L / Y1234567X / Z1234567R
 * - CIF / business NIF: B12345678 / A12345678 / J1234567A / P1234567A
 *
 * 适合兼容：
 * - autónomo / 个体户：DNI 或 NIE
 * - empresa / 大公司、小公司、协会等：CIF / business NIF
 *
 * 仅做格式校验，不做控制码算法校验。
 */
export type TagsTaxId = string &
  tags.Pattern<'^(?:\\d{8}[A-Z]|[XYZ]\\d{7}[A-Z]|[ABCDEFGHJNPQRSUVW]\\d{7}[0-9A-J])$'> &
  tags.MinLength<9> &
  tags.MaxLength<9> &
  tags.Example<'B12345678'>;

/**
 * Company's legal name.
 * 公司法定名称。用于审核、发票、合同等正式场景。
 * 注册时必填且不可为空。
 */
export type TagsCompanyName = TagsNotBlank & tags.MaxLength<100>;

/**
 * Public display name.
 * 对外展示名称/商家名称。用于零售商看到的店铺名。
 * company_name = "Comercial Zhang Alimentación S.L."
 * display_name = "Zhang Alimentación"
 */
export type TagsWholesalerDisplayName = TagsNotBlank & tags.MaxLength<60>;

/**
 * Retailer public display name.
 * 零售商对外展示名称。
 */
export type TagsRetailerDisplayName = TagsWholesalerDisplayName;

/**
 * Retailer contact person name.
 * 零售商联系人姓名。
 */
export type TagsRetailerContactName = TagsNotBlank & tags.MaxLength<80>;

/**
 * Public short description.
 * 商家简介。用于告诉零售商这个批发商主要卖什么、有什么特点。
 *
 * 例如：
 * "Mayorista de bebidas, alimentación y productos para hostelería."
 */
export type TagsWholesalerDescription = string & tags.MaxLength<300>;

/**
 * Delivery area description.
 * 配送范围说明。
 *
 * 用于描述配送城市、区域或服务范围。
 *
 * 示例：
 * "Reparto en Benidorm, Altea y La Nucía."
 */
export type TagsWholesalerDeliveryAreaDescription = string &
  tags.MaxLength<200>;

/**
 * Minimum order amount.
 * 最低起订金额。
 *
 * - 字符串形式，避免 decimal 精度问题
 * - PostgreSQL numeric(10,2) 风格
 * - 范围：0 ~ 1,000,000.00
 * - 最多两位小数
 */
export type TagsWholesalerMinimumOrderAmount = TagsUNumeric10_2_String &
  TagsMaxNumberString<1000000.0> &
  tags.Example<'100.00'>;
