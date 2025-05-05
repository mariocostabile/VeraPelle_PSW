// src/app/core/models/product-dto.ts

export interface ProductDTO {
  id?: number;          // opzionale perché generato automaticamente dal DB
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: number;
}
