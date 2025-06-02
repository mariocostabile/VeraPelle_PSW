// src/app/core/models/create-order-request.ts

export interface OrderItemDTO {
  productId: number;
  quantity: number;
  colorId: number;       // ← nuovo campo per la variante selezionata
}

export interface PaymentInfoDTO {
  cardNumber: string;
  expiry: string;    // es. "09/27"
  cvv: string;
}

export interface CreateOrderRequest {
  shippingAddress: string;
  paymentInfo: PaymentInfoDTO;
  cartVersion: number;   // ← versione del carrello per l'ottimistic locking
}
