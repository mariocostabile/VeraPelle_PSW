import { ProductDTO } from './product-dto';
import { ColorDTO } from './color-dto';

export interface CartItemDTO {
  id: number;
  product: ProductDTO;
  quantity: number;
  subtotal: number;
  thumbnailUrl: string | null;

  // colore selezionato
  selectedColor: ColorDTO;

  // stock residuo della variante selezionata
  variantStockQuantity: number;
}
