import { Prisma } from 'src/generated/prisma/client';
const Decimal = Prisma.Decimal;
// 使用Prisma的Decimal类型，其底层由decimal.js实现。 https://prisma.org.cn/docs/orm/prisma-client/special-fields-and-types
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
 */
export function computePrice(
  price?: string,
  priceIva?: string,
  iva: string = '0',
): { price: string; price_iva: string } {
  const ivaFactor = new Decimal(iva).dividedBy(100).plus(1);

  if (price !== undefined) {
    const priceDecimal = new Decimal(price);
    const priceIvaDecimal = priceDecimal.times(ivaFactor);
    return {
      price: priceDecimal.toFixed(2),
      price_iva: priceIvaDecimal.toFixed(2),
    };
  }

  if (priceIva !== undefined) {
    const priceIvaDecimal = new Decimal(priceIva);
    const priceDecimal = priceIvaDecimal.dividedBy(ivaFactor);
    return {
      price: priceDecimal.toFixed(2),
      price_iva: priceIvaDecimal.toFixed(2),
    };
  }

  throw new Error('Either price or price iva must be provided');
}
