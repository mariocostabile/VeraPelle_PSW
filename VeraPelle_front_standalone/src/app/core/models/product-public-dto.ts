// src/app/core/models/product-public-dto.ts

export interface ProductPublicDTO {
  id: number;
  name: string;
  price: number;
  categoryNames: string[];
  description: string;   // aggiunto
  imageUrls: string[];   // aggiunto
}
