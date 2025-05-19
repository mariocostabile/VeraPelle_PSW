// src/app/core/services/customer/customer.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { KeycloakService } from '../keycloak/keycloak.service';
import { CustomerDTO } from '../../models/customer-dto';
import { CustomerUpdate } from '../../models/customer-update';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly baseUrl = 'http://localhost:8080/api/customer/auth';

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService
  ) {}

  /** Restituisce gli header con il Bearer token, o lancia errore se non autenticato */
  private getAuthHeaders(): HttpHeaders {
    const token = this.keycloakService.profile?.token;
    if (!token) {
      throw new Error('Utente non autenticato.');
    }
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  /** GET /customer/auth/me */
  getCustomerProfile(): Observable<CustomerDTO> {
    try {
      const headers = this.getAuthHeaders();
      return this.http.get<CustomerDTO>(`${this.baseUrl}/me`, { headers });
    } catch (err) {
      return throwError(() => err);
    }
  }

  /** PUT /customer/auth/me */
  updateCustomer(payload: CustomerUpdate): Observable<CustomerDTO> {
    try {
      const headers = this.getAuthHeaders();
      return this.http.put<CustomerDTO>(`${this.baseUrl}/me`, payload, { headers });
    } catch (err) {
      return throwError(() => err);
    }
  }

  /** POST /customer/auth/register */
  registerCustomer(): Observable<void> {
    try {
      const headers = this.getAuthHeaders();
      return this.http.post<void>(`${this.baseUrl}/register`, {}, { headers });
    } catch (err) {
      return throwError(() => err);
    }
  }
}
