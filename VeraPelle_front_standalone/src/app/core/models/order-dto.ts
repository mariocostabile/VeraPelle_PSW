// src/app/models/order-dto.ts

export enum OrderStatus {
  CREATED = 'CREATED',
  PAID = 'PAID',
  DECLINED = 'DECLINED',
  CANCELLED = 'CANCELLED'
}

export interface OrderItemDetailDTO {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface OrderDTO {
  id: number;
  date: string;              // ISO string dal backend
  status: OrderStatus;
  totalAmount: number;
  shippingAddress: string;
  items: OrderItemDetailDTO[];
  paymentStatus: 'APPROVED' | 'DECLINED';
}
