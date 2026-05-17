import { existsSync } from 'node:fs';
import path from 'node:path';
import type { TFontDictionary } from 'pdfmake/interfaces.js';

export const ORDER_PDF_FONT_FAMILIES = {
  LATIN: 'NotoSans',
  ZH_CN: 'NotoSansSC',
  ZH_TW: 'NotoSansTC',
} as const;

export const ORDER_PDF_FONT_DIR = path.resolve(process.cwd(), 'assert', 'font');

export const ORDER_PDF_DIST_FONT_DIR = path.resolve(
  process.cwd(),
  'dist',
  'font',
);

export const ORDER_PDF_FONT_FILES = {
  NOTO_SANS_REGULAR: 'NotoSans-Regular.ttf',
  NOTO_SANS_BOLD: 'NotoSans-Bold.ttf',
  NOTO_SANS_ITALIC: 'NotoSans-Italic.ttf',
  NOTO_SANS_BOLD_ITALIC: 'NotoSans-BoldItalic.ttf',
  NOTO_SANS_SC: 'NotoSansSC-VF.ttf',
  NOTO_SANS_TC: 'NotoSansTC-VF.ttf',
} as const;

const resolveOrderPdfFontPath = (filename: string) => {
  const sourcePath = path.join(ORDER_PDF_FONT_DIR, filename);
  const distPath = path.join(ORDER_PDF_DIST_FONT_DIR, filename);

  return existsSync(sourcePath) ? sourcePath : distPath;
};

export const ORDER_PDF_FONT_PATHS = {
  NOTO_SANS_REGULAR: resolveOrderPdfFontPath(
    ORDER_PDF_FONT_FILES.NOTO_SANS_REGULAR,
  ),
  NOTO_SANS_BOLD: resolveOrderPdfFontPath(ORDER_PDF_FONT_FILES.NOTO_SANS_BOLD),
  NOTO_SANS_ITALIC: resolveOrderPdfFontPath(
    ORDER_PDF_FONT_FILES.NOTO_SANS_ITALIC,
  ),
  NOTO_SANS_BOLD_ITALIC: resolveOrderPdfFontPath(
    ORDER_PDF_FONT_FILES.NOTO_SANS_BOLD_ITALIC,
  ),
  NOTO_SANS_SC: resolveOrderPdfFontPath(ORDER_PDF_FONT_FILES.NOTO_SANS_SC),
  NOTO_SANS_TC: resolveOrderPdfFontPath(ORDER_PDF_FONT_FILES.NOTO_SANS_TC),
} as const;

export const ORDER_PDF_FONTS: TFontDictionary = {
  [ORDER_PDF_FONT_FAMILIES.LATIN]: {
    normal: ORDER_PDF_FONT_PATHS.NOTO_SANS_REGULAR,
    bold: ORDER_PDF_FONT_PATHS.NOTO_SANS_BOLD,
    italics: ORDER_PDF_FONT_PATHS.NOTO_SANS_ITALIC,
    bolditalics: ORDER_PDF_FONT_PATHS.NOTO_SANS_BOLD_ITALIC,
  },
  [ORDER_PDF_FONT_FAMILIES.ZH_CN]: {
    normal: ORDER_PDF_FONT_PATHS.NOTO_SANS_SC,
    bold: ORDER_PDF_FONT_PATHS.NOTO_SANS_SC,
    italics: ORDER_PDF_FONT_PATHS.NOTO_SANS_SC,
    bolditalics: ORDER_PDF_FONT_PATHS.NOTO_SANS_SC,
  },
  [ORDER_PDF_FONT_FAMILIES.ZH_TW]: {
    normal: ORDER_PDF_FONT_PATHS.NOTO_SANS_TC,
    bold: ORDER_PDF_FONT_PATHS.NOTO_SANS_TC,
    italics: ORDER_PDF_FONT_PATHS.NOTO_SANS_TC,
    bolditalics: ORDER_PDF_FONT_PATHS.NOTO_SANS_TC,
  },
};

export const ORDER_PDF_ALLOWED_FONT_PATHS = new Set(
  Object.values(ORDER_PDF_FONTS).flatMap((fontFamily) =>
    Object.values(fontFamily).filter(
      (font): font is string => typeof font === 'string',
    ),
  ),
);

export const getOrderPdfFontFamily = (language: string) => {
  const normalized = language.toLowerCase();

  if (normalized === 'zh-tw' || normalized === 'zh-hk') {
    return ORDER_PDF_FONT_FAMILIES.ZH_TW;
  }

  if (normalized.startsWith('zh')) {
    return ORDER_PDF_FONT_FAMILIES.ZH_CN;
  }

  return ORDER_PDF_FONT_FAMILIES.LATIN;
};
