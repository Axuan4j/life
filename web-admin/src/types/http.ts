export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  requestId: string;
}

export interface PageResponse<T> {
  items: T[];
  total: number;
  pageNo: number;
  pageSize: number;
}
