interface Response<T> {
  statusCode: number;
  message?: string;
  data: T;
}

interface ErrorResponse {
  statusCode: number;
  message?: string;
  error?: string | string[];
}

interface PaginationMeta {
  total: number;
  page: number;
  limit: number;
}

interface PaginatedData {
  items: any[];
  pagination: PaginationMeta;
}

interface PaginatedDataWithT<T> {
  items: T[];
  pagination: PaginationMeta;
}

export {
  Response,
  PaginationMeta,
  PaginatedData,
  PaginatedDataWithT,
  ErrorResponse,
};
