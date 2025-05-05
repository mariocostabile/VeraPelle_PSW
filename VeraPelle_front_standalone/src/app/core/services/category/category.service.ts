import { Injectable, inject } from '@angular/core';
import { HttpClient }         from '@angular/common/http';
import { Observable }         from 'rxjs';
import { CategoryDTO }        from '../../models/category-dto';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/customer/categories';

  /** GET /customer/categories */
  getCategories(): Observable<CategoryDTO[]> {
    return this.http.get<CategoryDTO[]>(this.baseUrl);
  }

  /** DELETE /customer/categories/{id} */
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
