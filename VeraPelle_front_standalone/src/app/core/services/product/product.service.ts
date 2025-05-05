// src/app/core/services/product/product.service.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable }         from 'rxjs';
import { ProductDTO }         from '@app/core/models/product-dto';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  // ← qui il percorso corretto:
  private readonly baseUrl = 'http://localhost:8080/api/admin/products';

  /** GET /api/admin/products */
  getProducts(): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(this.baseUrl);
  }

  /** DELETE /api/admin/products/{id} */
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
