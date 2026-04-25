import Decimal from 'decimal.js';
import { BadRequestException } from '@nestjs/common';

// 联动验证：price 与 price_iva 最大值
const MAX_PRICE = new Decimal(10000000);
const MAX_PRICE_IVA = new Decimal(20000000);

/**
 * 计算价格（不含 IVA ↔ 含 IVA）。
 *
 * 使用规则：
 * - 传入 price（不含 IVA）→ 自动计算 priceIva
 * - 传入 priceIva（含 IVA）→ 自动计算 price
 * - 两者必须至少传一个，不能同时为 undefined
 *
 * @param price 不含 IVA 的价格（可为 0）
 * @param priceIva 含 IVA 的价格（可为 0）
 * @param iva IVA 税率，例如 21 表示 21%（默认 0）
 * @returns { price: string, price_iva: string } —— 两者都返回，保留 2 位小数字符串
 *
 * @throws 当 price 与 priceIva 同时为 undefined 时抛出错误
 * @throws 当 priceIva 超出最大允许值时抛出错误
 * @throws 当 price 超出最大允许值时抛出错误
 */
export function computePrice(
  price?: string | null,
  priceIva?: string | null,
  iva: string = '0',
): { price: string; price_iva: string } {
  const ivaFactor = new Decimal(iva).dividedBy(100).plus(1);

  if (price) {
    const priceDecimal = new Decimal(price);
    // 检查传入的 price 是否超过上限
    if (priceDecimal.gt(MAX_PRICE)) {
      throw new BadRequestException(
        `Price exceeds maximum allowed price ${MAX_PRICE.toFixed(2)}`,
      );
    }
    const priceIvaDecimal = priceDecimal.times(ivaFactor);
    if (priceIvaDecimal.gt(MAX_PRICE_IVA)) {
      throw new BadRequestException(
        `Price + IVA exceeds maximum allowed price_iva ${MAX_PRICE_IVA.toFixed(2)}`,
      );
    }
    return {
      price: priceDecimal.toFixed(2),
      price_iva: priceIvaDecimal.toFixed(2),
    };
  }

  if (priceIva) {
    const priceIvaDecimal = new Decimal(priceIva);
    // 检查传入的 priceIva 是否超过上限
    if (priceIvaDecimal.gt(MAX_PRICE_IVA)) {
      throw new BadRequestException(
        `Price with IVA exceeds maximum allowed price_iva ${MAX_PRICE_IVA.toFixed(2)}`,
      );
    }
    const priceDecimal = priceIvaDecimal.dividedBy(ivaFactor);
    if (priceDecimal.gt(MAX_PRICE)) {
      throw new BadRequestException(
        `Price derived from price_iva exceeds maximum allowed price ${MAX_PRICE.toFixed(2)}`,
      );
    }
    return {
      price: priceDecimal.toFixed(2),
      price_iva: priceIvaDecimal.toFixed(2),
    };
  }

  throw new BadRequestException(
    'At least one of price and price_iva is required',
  );
}
