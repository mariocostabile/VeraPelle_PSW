// src/app/core/services/product/product.service.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable }         from 'rxjs';
import { ProductDTO }         from '@app/core/models/product-dto';
import { ProductPublicDTO }   from '@app/core/models/product-public-dto';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);

  // endpoint admin
  private adminBase = 'http://localhost:8080/api/admin/products';

  // endpoint pubblico per dettagli e suggestion
  private publicBase = 'http://localhost:8080/api/products';

  // — Admin CRUD —
  getProducts(): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(this.adminBase);
  }

  getProductById(id: number): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.adminBase}/${id}`);
  }

  createProduct(dto: ProductDTO): Observable<ProductDTO> {
    return this.http.post<ProductDTO>(this.adminBase, dto);
  }

  updateProduct(id: number, dto: ProductDTO): Observable<ProductDTO> {
    return this.http.put<ProductDTO>(`${this.adminBase}/${id}`, dto);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminBase}/${id}`);
  }

  // — Public suggestions —
  suggestProducts(q: string): Observable<ProductPublicDTO[]> {
    return this.http.get<ProductPublicDTO[]>(`${this.publicBase}/suggestions`, {
      params: { q }
    });
  }
}
