// src/app/core/models/product-dto.ts

import {ProductVariantDTO} from '@app/core/models/product-variant-dto';

export interface ProductDTO {
  id?: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryIds: number[];    // array di ID di categorie
  colorIds: number[];       // ← array di ID di colori
  variants?: { colorId: number; stockQuantity: number }[];
}
