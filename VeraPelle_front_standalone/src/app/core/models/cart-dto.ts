// src/app/core/models/cart-dto.ts

import { CartItemDTO } from './cart-item-dto';

export interface CartDTO {
  id: number;
  items: CartItemDTO[];
  total: number;
  version: number;
}
