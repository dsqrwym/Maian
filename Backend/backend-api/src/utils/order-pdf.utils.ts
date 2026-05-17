import type { Readable } from 'node:stream';
import type {
  IOrderDetailItem,
  IShippingAddressSnapshot,
} from '#/orders/order.types.js';

const FALLBACK_TEXT = '-';

export const safeText = (value?: string | number | null): string => {
  if (value === undefined || value === null || value === '')
    return FALLBACK_TEXT;
  return String(value);
};

export const formatMoney = (
  value: string | number,
  currency: string,
  locale: string,
): string => {
  const amount = Number(value);
  const currencyCode = currency.trim();

  if (Number.isNaN(amount)) return `${safeText(value)} ${currencyCode}`;

  try {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currencyCode,
      currencyDisplay: 'narrowSymbol',
    }).format(amount);
  } catch {
    return `${amount.toFixed(2)} ${currencyCode}`;
  }
};

export const formatDecimal = (
  value: string | number,
  locale: string,
): string => {
  const amount = Number(value);

  if (Number.isNaN(amount)) return safeText(value);

  try {
    return new Intl.NumberFormat(locale, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return amount.toFixed(2);
  }
};

export const formatDate = (
  value: string,
  locale: string,
  timeZone: string,
): string => {
  try {
    return new Intl.DateTimeFormat(locale, {
      dateStyle: 'medium',
      timeStyle: 'short',
      timeZone,
    }).format(new Date(value));
  } catch {
    return new Intl.DateTimeFormat('en', {
      dateStyle: 'medium',
      timeStyle: 'short',
      timeZone: 'UTC',
    }).format(new Date(value));
  }
};

export const getPartyName = (party: {
  company_name?: string | null;
  display_name?: string | null;
}): string => {
  return party.company_name ?? party.display_name ?? FALLBACK_TEXT;
};

const normalizeLanguage = (language: string): string => {
  return language.toLowerCase();
};

const findProductTranslation = (item: IOrderDetailItem, language: string) => {
  const translations = item.product_translations_snapshot ?? [];
  const normalized = normalizeLanguage(language);
  const base = normalized.split('-')[0];

  return (
    translations.find(
      (translation) => normalizeLanguage(translation.lang_code) === normalized,
    ) ??
    translations.find(
      (translation) =>
        normalizeLanguage(translation.lang_code).split('-')[0] === base,
    )
  );
};

export const localizeOrderDetailItems = (
  items: IOrderDetailItem[],
  language: string,
): IOrderDetailItem[] => {
  return items.map((item) => {
    const translation = findProductTranslation(item, language);

    return {
      ...item,
      product_name: translation?.name || item.product_name,
      product_title: translation?.title ?? item.product_title,
    };
  });
};

export const getLocalizedAddressNames = (
  address: IShippingAddressSnapshot,
  language: string,
) => {
  const prefersLocalName = !normalizeLanguage(language).startsWith('en');

  return {
    cityName:
      prefersLocalName && address.city_name_local
        ? address.city_name_local
        : address.city_name,
    provinceName:
      prefersLocalName && address.province_name_local
        ? address.province_name_local
        : address.province_name,
    countryName:
      prefersLocalName && address.country_name_local
        ? address.country_name_local
        : address.country_name,
  };
};

export const sanitizeOrderPdfFilename = (orderNumber: string): string => {
  const safeOrderNumber = orderNumber.replace(/[/\\:*?"<>|]/g, '_');
  return `ORDER-${safeOrderNumber}.pdf`.slice(0, 255);
};

export const streamToBuffer = async (
  input: Readable | Promise<Readable>,
): Promise<Buffer> => {
  const stream = await input;
  const chunks: Buffer[] = [];

  for await (const chunk of stream) {
    chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
  }

  return Buffer.concat(chunks);
};
