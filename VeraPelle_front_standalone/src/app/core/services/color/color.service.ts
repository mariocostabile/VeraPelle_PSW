import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ColorDTO } from '@app/core/models/color-dto';

@Injectable({ providedIn: 'root' })
export class ColorService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/admin/colors';

  getColors(): Observable<ColorDTO[]> {
    return this.http.get<ColorDTO[]>(this.baseUrl);
  }

  getColor(id: number): Observable<ColorDTO> {
    return this.http.get<ColorDTO>(`${this.baseUrl}/${id}`);
  }

  createColor(dto: Partial<ColorDTO>): Observable<ColorDTO> {
    return this.http.post<ColorDTO>(this.baseUrl, dto);
  }

  updateColor(id: number, dto: Partial<ColorDTO>): Observable<ColorDTO> {
    return this.http.put<ColorDTO>(`${this.baseUrl}/${id}`, dto);
  }

  deleteColor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
