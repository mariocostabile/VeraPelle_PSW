// src/app/core/models/product-public-dto.ts

import { ColorDTO } from './color-dto';
import {ProductVariantDTO} from '@app/core/models/product-variant-dto';

export interface ProductPublicDTO {
  id: number;
  name: string;
  price: number;
  categoryNames: string[];
  description: string;
  imageUrls: string[];
  colors: ColorDTO[];             // puoi mantenerlo se serve altrove
  stockQuantity: number;         // totale (opzionale)
  variants: ProductVariantDTO[]; // ← qui prendi gli id/colori/hex/stock per il detail
}
