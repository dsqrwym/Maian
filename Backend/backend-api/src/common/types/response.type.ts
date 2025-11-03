interface Response<T> {
  statusCode: number;
  message?: string;
  data: T;
}

interface PaginationMeta {
  total: number;
  page: number;
  limit: number;
}

interface ToPaginated {
  items: any;
  meta: PaginationMeta;
}

interface PaginatedData<T> {
  items: T[];
  pagination: PaginationMeta;
}

export { Response, PaginationMeta, PaginatedData, ToPaginated };
