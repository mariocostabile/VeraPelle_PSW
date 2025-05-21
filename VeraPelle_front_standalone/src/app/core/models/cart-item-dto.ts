// src/app/core/models/cart-item-dto.ts

import { ProductDTO } from './product-dto';
import { ColorDTO } from './color-dto';

export interface CartItemDTO {
  id: number;
  product: ProductDTO;
  quantity: number;
  subtotal: number;
  thumbnailUrl: string | null;
  selectedColor: ColorDTO;
}
