// src/app/core/models/product-public-dto.ts

import { ColorDTO } from './color-dto';

export interface ProductPublicDTO {
  id: number;
  name: string;
  price: number;
  categoryNames: string[];
  description: string;
  imageUrls: string[];
  colors: ColorDTO[];
  stockQuantity: number;
}
