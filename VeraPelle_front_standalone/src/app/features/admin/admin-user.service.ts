// src/app/features/admin/admin-user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CustomerDTO } from '../../core/models/customer-dto';

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {
  private readonly baseUrl = 'http://localhost:8080/api/admin/auth/users';

  constructor(private http: HttpClient) {}

  /** Recupera tutti gli utenti */
  getAll(): Observable<CustomerDTO[]> {
    return this.http.get<CustomerDTO[]>(this.baseUrl);
  }

  /** Recupera un singolo utente per ID */
  getById(id: string): Observable<CustomerDTO> {
    return this.http.get<CustomerDTO>(`${this.baseUrl}/${id}`);
  }

  /** Aggiorna un utente */
  update(id: string, dto: CustomerDTO): Observable<CustomerDTO> {
    return this.http.put<CustomerDTO>(`${this.baseUrl}/${id}`, dto);
  }

  /** Elimina un utente */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
