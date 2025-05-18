import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductPublicDTO } from '@app/core/models/product-public-dto';

interface Paginated<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // pagina corrente
}

@Injectable({ providedIn: 'root' })
export class StoreService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/products';

  /**
   * Restituisce una pagina di prodotti, filtrata per categoria opzionale.
   */
  getProducts(
    page: number,
    size: number,
    categoryId?: number
  ): Observable<Paginated<ProductPublicDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (categoryId != null) {
      params = params.set('categoryId', categoryId.toString());
    }

    return this.http.get<Paginated<ProductPublicDTO>>(this.baseUrl, { params });
  }

  /**
   * Restituisce il dettaglio di un singolo prodotto.
   */
  getProductById(id: number): Observable<ProductPublicDTO> {
    return this.http.get<ProductPublicDTO>(`${this.baseUrl}/${id}`);
  }
}
