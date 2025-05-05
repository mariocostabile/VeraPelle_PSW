// src/app/core/services/category/category.service.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable }         from 'rxjs';
import { CategoryDTO }        from '../../models/category-dto';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);

  /** Base URL: endpoint pubblico per le categorie */
  private readonly baseUrl = 'http://localhost:8080/customer/categories';

  /** GET /customer/categories → lista di categorie (pubblico, no auth) */
  getCategories(): Observable<CategoryDTO[]> {
    return this.http.get<CategoryDTO[]>(this.baseUrl);
  }
}
