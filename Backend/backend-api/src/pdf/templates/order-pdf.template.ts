import type { Content, TableCell } from 'pdfmake';
import type { IOrderPdfData, IOrderPdfLabels } from '#/pdf/pdf.type.js';
import type { TDocumentDefinitions } from 'pdfmake/interfaces.js';
import {
  formatDate,
  formatDecimal,
  formatMoney,
  getLocalizedAddressNames,
  getPartyName,
  safeText,
} from '#/utils/pdf/order.pdf.utils.js';

const LOGO_BOX_WIDTH = 110;
const LOGO_BOX_HEIGHT = 62;
/**
 * pdfmake 使用 pt
 * 1 pt = 1/72 inch
 * A4 = 210mm × 297mm
 * ≈ 595.28pt × 841.89pt
 */
const A4_WIDTH = 595.28;
const PAGE_MARGIN_LEFT = 28; // 9.88mm
const PAGE_MARGIN_TOP = 32; // 11.29mm
const PAGE_MARGIN_RIGHT = 28; // 9.88mm
const PAGE_MARGIN_BOTTOM = 40; // 14.11mm
const PAGE_MARGINS: [number, number, number, number] = [
  PAGE_MARGIN_LEFT,
  PAGE_MARGIN_TOP,
  PAGE_MARGIN_RIGHT,
  PAGE_MARGIN_BOTTOM,
];
const A4_CONTENT_WIDTH = A4_WIDTH - PAGE_MARGIN_LEFT - PAGE_MARGIN_RIGHT;

const buildLogoBox = (logoData?: string | null): Content => {
  return {
    table: {
      widths: [LOGO_BOX_WIDTH],
      heights: [LOGO_BOX_HEIGHT],
      body: [
        [
          logoData
            ? {
                image: logoData,
                fit: [LOGO_BOX_WIDTH - 12, LOGO_BOX_HEIGHT - 12],
                alignment: 'center',
                margin: [0, 6, 0, 0],
              }
            : {
                text: '',
              },
        ],
      ],
    },
    layout: {
      hLineWidth: () => 0.5,
      vLineWidth: () => 0.5,
      hLineColor: () => '#dddddd',
      vLineColor: () => '#dddddd',
      paddingLeft: () => 0,
      paddingRight: () => 0,
      paddingTop: () => 0,
      paddingBottom: () => 0,
    },
  };
};

const buildPartyBlock = (
  title: string,
  labels: IOrderPdfLabels,
  party: {
    company_name?: string | null;
    display_name?: string | null;
    company_type?: string | null;
    contact_name?: string | null;
    tax_id?: string | null;
    email?: string | null;
    telephone?: string | null;
  },
): Content => {
  return {
    stack: [
      { text: title, style: 'sectionTitle' },
      {
        text: getPartyName(party),
        bold: true,
        margin: [0, 0, 0, 3],
      },
      { text: `${labels.companyType}: ${safeText(party.company_type)}` },
      { text: `${labels.taxId}: ${safeText(party.tax_id)}` },
      party.contact_name
        ? { text: `${labels.contact}: ${safeText(party.contact_name)}` }
        : { text: '' },
      { text: `${labels.email}: ${safeText(party.email)}` },
      { text: `${labels.phone}: ${safeText(party.telephone)}` },
    ],
  };
};

const buildShippingAddressBlock = (
  data: IOrderPdfData['shipping_address_snapshot'],
  labels: IOrderPdfLabels,
  language: string,
): Content => {
  const addressNames = getLocalizedAddressNames(data, language);

  return {
    stack: [
      { text: labels.shippingAddress, style: 'sectionTitle' },
      { text: safeText(data.street), bold: true },
      { text: `${safeText(data.zip_code)} ${safeText(addressNames.cityName)}` },
      {
        text: `${safeText(addressNames.provinceName)}, ${safeText(
          addressNames.countryName,
        )}`,
      },
      {
        text: `${labels.country}: ${safeText(data.country_alpha2)} / ${safeText(
          data.country_alpha3,
        )}`,
      },
    ],
  };
};

const buildItemsTable = (
  data: IOrderPdfData,
  labels: IOrderPdfLabels,
): Content => {
  const body: TableCell[][] = [
    [
      { text: labels.product, style: 'tableHeader' },
      { text: labels.code, style: 'tableHeader' },
      { text: labels.saleUnit, style: 'tableHeader', alignment: 'right' },
      { text: labels.quantity, style: 'tableHeader', alignment: 'right' },
      { text: labels.price, style: 'tableHeader', alignment: 'right' },
      { text: labels.ivaPercent, style: 'tableHeader', alignment: 'right' },
      { text: labels.subtotal, style: 'tableHeader', alignment: 'right' },
      { text: labels.iva, style: 'tableHeader', alignment: 'right' },
      { text: labels.total, style: 'tableHeader', alignment: 'right' },
    ],
    ...data.details.map((item): TableCell[] => [
      {
        stack: [
          { text: item.product_name, bold: true },
          item.product_title
            ? { text: item.product_title, fontSize: 7, color: '#555555' }
            : { text: '', fontSize: 7 },
          {
            text: `${labels.variant}: ${safeText(item.variant_product_code)}`,
            fontSize: 7,
            color: '#555555',
          },
        ],
      },
      {
        text: item.product_code,
        fontSize: 8,
      },
      {
        text: String(item.sale_unit_qty),
        alignment: 'right',
      },
      {
        text: String(item.quantity),
        alignment: 'right',
      },
      {
        text: formatMoney(item.unit_price, data.currency, data.language),
        alignment: 'right',
      },
      {
        text: `${formatDecimal(item.iva, data.language)} %`,
        alignment: 'right',
      },
      {
        text: formatMoney(item.subtotal, data.currency, data.language),
        alignment: 'right',
      },
      {
        text: formatMoney(item.iva_total, data.currency, data.language),
        alignment: 'right',
      },
      {
        text: formatMoney(item.total, data.currency, data.language),
        alignment: 'right',
        bold: true,
      },
    ]),
  ];

  return {
    stack: [
      { text: labels.itemsTitle, style: 'sectionTitle' },
      {
        table: {
          headerRows: 1,
          widths: ['*', 48, 28, 32, 50, 36, 50, 50, 55],
          body,
        },
        layout: {
          fillColor: (rowIndex: number) => {
            if (rowIndex === 0) return '#eeeeee';
            return rowIndex % 2 === 0 ? '#fafafa' : null;
          },
          hLineColor: () => '#dddddd',
          vLineColor: () => '#eeeeee',
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          paddingLeft: () => 4,
          paddingRight: () => 4,
          paddingTop: () => 4,
          paddingBottom: () => 4,
        },
      },
    ],
  };
};

const buildTotalsTable = (
  data: IOrderPdfData,
  labels: IOrderPdfLabels,
): Content => {
  return {
    margin: [0, 16, 0, 0],
    columns: [
      {
        width: '*',
        stack: [
          {
            text: `${labels.lineCount}: ${data.item_count}`,
            fontSize: 8,
            color: '#555555',
          },
        ],
      },
      {
        width: 210,
        table: {
          widths: ['*', 90],
          body: [
            [
              { text: labels.taxableBase, style: 'totalLabel' },
              {
                text: formatMoney(data.subtotal, data.currency, data.language),
                style: 'totalValue',
              },
            ],
            /* 还没有做折扣
            [
              { text: labels.discount, style: 'totalLabel' },
              {
                text: formatMoney(
                  data.discount_total,
                  data.currency,
                  data.language,
                ),
                style: 'totalValue',
              },
            ],
             */
            [
              { text: labels.iva, style: 'totalLabel' },
              {
                text: formatMoney(data.iva_total, data.currency, data.language),
                style: 'totalValue',
              },
            ],
            [
              { text: labels.grandTotal, style: 'grandTotalLabel' },
              {
                text: formatMoney(data.total, data.currency, data.language),
                style: 'grandTotalValue',
              },
            ],
          ],
        },
        layout: {
          hLineColor: () => '#dddddd',
          vLineColor: () => '#dddddd',
          hLineWidth: () => 0.5,
          vLineWidth: () => 0.5,
          paddingLeft: () => 6,
          paddingRight: () => 6,
          paddingTop: () => 5,
          paddingBottom: () => 5,
        },
      },
    ],
    columnGap: 20,
  };
};

export function buildOrderPdfTemplate(
  data: IOrderPdfData,
  labels: IOrderPdfLabels,
): TDocumentDefinitions {
  const formattedDate = formatDate(
    data.created_at,
    data.language,
    data.timezone,
  );

  return {
    pageSize: 'A4',
    pageMargins: PAGE_MARGINS,

    defaultStyle: {
      font: data.pdf_font ?? 'NotoSans',
      fontSize: 8,
      lineHeight: 1.15,
    },

    styles: {
      documentTitle: {
        fontSize: 20,
        bold: true,
        color: '#222222',
      },
      documentSubtitle: {
        fontSize: 9,
        color: '#555555',
      },
      sectionTitle: {
        fontSize: 10,
        bold: true,
        color: '#222222',
        margin: [0, 10, 0, 5],
      },
      tableHeader: {
        bold: true,
        fontSize: 7.5,
        color: '#222222',
      },
      totalLabel: {
        bold: true,
        alignment: 'right',
      },
      totalValue: {
        alignment: 'right',
      },
      grandTotalLabel: {
        bold: true,
        alignment: 'right',
        fontSize: 11,
      },
      grandTotalValue: {
        bold: true,
        alignment: 'right',
        fontSize: 11,
      },
    },

    footer: (currentPage, pageCount) => ({
      columns: [
        {
          text: `${labels.order} ${data.order_number}`,
          alignment: 'left',
          margin: [PAGE_MARGIN_LEFT, 18, 0, 0],
          fontSize: 7,
          color: '#777777',
        },
        {
          text: `${labels.page} ${currentPage} / ${pageCount}`,
          alignment: 'right',
          margin: [0, 18, PAGE_MARGIN_RIGHT, 0],
          fontSize: 7,
          color: '#777777',
        },
      ],
    }),

    content: [
      {
        columns: [
          // 左 LOGO
          {
            width: LOGO_BOX_WIDTH,
            stack: [buildLogoBox(data.wholesaler_logo_data_url)],
          },
          {
            // 右 文本
            width: '*',
            stack: [
              {
                text: labels.documentTitle,
                style: 'documentTitle',
                alignment: 'right',
              },
              {
                text: data.order_number,
                style: 'documentSubtitle',
                alignment: 'right',
                margin: [0, 3, 0, 0],
              },
              {
                text: `${labels.series}: ${data.order_series} | ${labels.year}: ${data.order_year} | ${labels.sequence}: ${data.order_sequence}`,
                style: 'documentSubtitle',
                alignment: 'right',
                margin: [0, 2, 0, 0],
              },
              {
                text: `${labels.date}: ${formattedDate}`,
                style: 'documentSubtitle',
                alignment: 'right',
                margin: [0, 2, 0, 0],
              },
            ],
          },
        ],
        columnGap: 16,
      },

      // 分割线
      {
        canvas: [
          {
            type: 'line',
            x1: 0,
            y1: 10,
            x2: A4_CONTENT_WIDTH,
            y2: 10,
            lineWidth: 1,
            lineColor: '#dddddd',
          },
        ],
        margin: [0, 4, 0, 10],
      },

      {
        columns: [
          {
            width: '*',
            stack: [
              // 零售商
              buildPartyBlock(labels.seller, labels, data.wholesaler_snapshot),
            ],
          },
          {
            // 批发商
            width: '*',
            stack: [
              buildPartyBlock(labels.buyer, labels, data.retailer_snapshot),
            ],
          },
        ],
        columnGap: 28,
      },

      {
        margin: [0, 8, 0, 0],
        columns: [
          {
            width: '*',
            stack: [
              // 收货地址
              buildShippingAddressBlock(
                data.shipping_address_snapshot,
                labels,
                data.language,
              ),
            ],
          },
          {
            width: '*',
            stack: [
              // 摘要
              { text: labels.documentSummary, style: 'sectionTitle' },
              {
                table: {
                  widths: ['*', '*'],
                  body: [
                    [labels.number, data.order_number],
                    [labels.date, formattedDate],
                    [labels.currency, data.currency.trim()],
                    [labels.lineCount, String(data.item_count)],
                  ],
                },
                layout: 'lightHorizontalLines',
              },
            ],
          },
        ],
        columnGap: 28,
      },

      // 商品明细
      buildItemsTable(data, labels),
      // 商品总计
      buildTotalsTable(data, labels),
    ],
  };
}
