import type { SpanishCompanyType } from '#/auth/dto/register-wholesaler.dto.js';
import type {
  TagsCompanyName,
  TagsWholesalerDeliveryAreaDescription,
  TagsWholesalerDescription,
  TagsWholesalerDisplayName,
  TagsWholesalerMinimumOrderAmount,
} from '#/utils/typia/validators/user.validator.js';
import type { TagsIntegerString } from '#/utils/typia/tags/string.tag.js';

export interface IWholesalerProfile {
  /**
   * Company's legal name.
   * 公司法定名称。用于审核、发票、合同等正式场景。
   * 注册时必填。
   */
  company_name: TagsCompanyName;

  /**
   * Company legal type in Spain.
   * 西班牙公司类型，例如 Autónomo、S.L.、S.A.、Sociedad Civil 等。
   * 注册时必填
   */
  company_type: SpanishCompanyType;

  /**
   * Public display name.
   * 对外展示名称/商家名称。用于零售商看到的店铺名。
   * company_name = "Comercial Zhang Alimentación S.L."
   * display_name = "Zhang Alimentación"
   */
  display_name?: TagsWholesalerDisplayName | null;

  /**
   * Public short description.
   * 商家简介。用于告诉零售商这个批发商主要卖什么、有什么特点。
   *
   * 例如：
   * "Mayorista de bebidas, alimentación y productos para hostelería."
   */
  description?: TagsWholesalerDescription | null;

  /**
   * Delivery area description.
   * 配送范围说明。
   *
   * 用于描述配送城市、区域或服务范围。
   *
   * 示例：
   * "Reparto en Benidorm, Altea y La Nucía."
   */
  delivery_area_description?: TagsWholesalerDeliveryAreaDescription | null;

  /**
   * Company logo file id.
   * 商家 Logo 文件 ID。关联 files.id。
   *
   */
  logo_file_id?: TagsIntegerString | null;

  /**
   * Minimum order amount.
   * 最低起订金额。用于订单创建时校验，不只是前端展示。
   *
   */
  minimum_order_amount?: TagsWholesalerMinimumOrderAmount | null;

  /**
   * Whether delivery is available.
   * 是否支持配送。
   *
   * true = 支持配送
   * false = 不支持配送
   * null/undefined = 未设置
   */
  delivery_available?: boolean | null;

  /**
   * Whether pickup is available.
   * 是否支持自提。
   *
   * true = 支持自提
   * false = 不支持自提
   * null/undefined = 未设置
   */
  pickup_available?: boolean | null;
}
