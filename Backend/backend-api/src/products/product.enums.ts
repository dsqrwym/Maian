export enum ProductListSelectField {
  IVA = 'iva',
  STATUS = 'status',
  USER_ID = 'user_id',
  CATEGORY = 'category',
}

export enum ProductSelectField {
  STATUS = 'status',
  USER_ID = 'user_id',
  CREATED_AT = 'created_at',
  UPDATED_AT = 'updated_at',
  CREATED_BY = 'created_by',
  UPDATED_BY = 'updated_by',
  RESERVED_STOCK = 'reserved_stock',
  LOW_STOCK_THRESHOLD = 'low_stock_threshold',
}

export enum ProductSortField {
  NAME = 'name',
  TITLE = 'title',
  CATEGORY = 'category',
  PRODUCT_CODE = 'product_code',
  MIN_ORDER_QTY = 'min_order_qty',
  AVAILABLE_STOCK = 'available_stock',
  PRICE_IVA = 'price_iva',
  PRICE = 'price',
}
