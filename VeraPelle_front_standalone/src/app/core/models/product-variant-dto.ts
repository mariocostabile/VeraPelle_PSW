// src/app/core/models/product-variant-dto.ts
export interface ProductVariantDTO {
  id?: number;
  sku: string;
  price: number;
  stockQuantity: number;
  colorId: number;
  size: string;
  hexCode: string;
}
