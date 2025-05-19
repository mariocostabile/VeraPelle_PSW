// src/app/core/models/product-dto.ts

export interface ProductDTO {
  id?: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryIds: number[];    // array di ID di categorie
  colorIds: number[];       // ← array di ID di colori
}
