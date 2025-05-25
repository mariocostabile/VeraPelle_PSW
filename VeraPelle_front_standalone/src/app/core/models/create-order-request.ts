// src/app/models/create-order-request.ts

export interface OrderItemDTO {
  productId: number;
  quantity: number;
}

export interface PaymentInfoDTO {
  cardNumber: string;
  expiry: string;    // es. "09/27"
  cvv: string;
}

export interface CreateOrderRequest {
  shippingAddress: string;
  items: OrderItemDTO[];
  paymentInfo: PaymentInfoDTO;
}
