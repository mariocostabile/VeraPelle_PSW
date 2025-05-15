// src/app/core/services/category/category.service.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable }         from 'rxjs';
import { CategoryDTO }        from '@app/core/models/category-dto';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/categories';

  /** GET /api/admin/categories */
  getCategories(): Observable<CategoryDTO[]> {
    return this.http.get<CategoryDTO[]>(this.baseUrl);
  }

  /** GET /api/admin/categories/{id} */
  getCategoryById(id: number): Observable<CategoryDTO> {
    return this.http.get<CategoryDTO>(`${this.baseUrl}/${id}`);
  }

  /** POST /api/admin/categories */
  createCategory(dto: CategoryDTO): Observable<CategoryDTO> {
    return this.http.post<CategoryDTO>(this.baseUrl, dto);
  }

  /** PUT /api/admin/categories/{id} */
  updateCategory(id: number, dto: CategoryDTO): Observable<CategoryDTO> {
    return this.http.put<CategoryDTO>(`${this.baseUrl}/${id}`, dto);
  }

  /** DELETE /api/admin/categories/{id} */
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
