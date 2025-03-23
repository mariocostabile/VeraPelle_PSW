import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from '../keycloak/keycloak.service';
import { Observable, throwError } from 'rxjs';

export interface CustomerDTO {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  email: string;
  phone?: string;
  address?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  // Configura qui l'URL del tuo backend
  private backendUrl = 'http://localhost:8080';

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService
  ) {}

  registerCustomer(): Observable<any> {
    const token = this.keycloakService.profile?.token;
    if (!token) {
      // Se non c'è il token, interrompi la chiamata con un errore
      return throwError(() => new Error('Token non disponibile. L\'utente potrebbe non essere autenticato.'));
    }

    // Imposta l'header Authorization con il token
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    // Esegui la chiamata POST verso il backend per registrare l'utente
    return this.http.post(`${this.backendUrl}/customer/register`, {}, { headers });
  }


  getCustomerProfile(): Observable<CustomerDTO> {
    const token = this.keycloakService.profile?.token;
    if (!token) {
      return throwError(() => new Error('Token non disponibile. Utente non autenticato.'));
    }

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.get<CustomerDTO>(`${this.backendUrl}/customer/auth/me`, { headers });
  }
}
