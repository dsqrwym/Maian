export interface IDashboardResponse {
  summary: {
    totalOrders: number;
    pendingOrders: number;
    acceptedOrders: number;
    totalRevenue: string;
    averageOrderValue: string;
  };

  orderStatus: {
    pending: number;
    accepted: number;
    rejected: number;
    cancelled: number;
    total: number;
  };

  revenueTrend: {
    date: string;
    orderCount: number;
    acceptedCount: number;
    revenue: string;
  }[];

  topSellingProducts: {
    productId: bigint | null;
    productName: string;
    productTranslation: {
      lang_code: string;
      name: string;
    }[];
    soldQuantity: number;
    revenue: string;
    orderCount: number;
  }[];
}
