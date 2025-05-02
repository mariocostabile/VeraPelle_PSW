import { Injectable }             from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService }         from '../keycloak/keycloak.service';
import { Observable, throwError }  from 'rxjs';

export interface CustomerDTO {
  firstName:   string;
  lastName:    string;
  dateOfBirth?: string;
  email:       string;
  phone?:      string;
  address?:    string;
}

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private backendUrl = 'http://localhost:8080';

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService
  ) {}

  /** Recupera il profilo dal backend */
  getCustomerProfile(): Observable<CustomerDTO> {
    const token = this.keycloakService.profile?.token;
    if (!token) {
      return throwError(() => new Error('Utente non autenticato.'));
    }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.get<CustomerDTO>(
      `${this.backendUrl}/customer/auth/me`,
      { headers }
    );
  }

  /** Aggiorna il profilo sul backend usando l’ID dal token */
  updateCustomer(profile: CustomerDTO): Observable<CustomerDTO> {
    const token  = this.keycloakService.profile?.token;
    const userId = this.keycloakService.getUserId();
    if (!token || !userId) {
      return throwError(() => new Error('Utente non autenticato o ID mancante.'));
    }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.put<CustomerDTO>(
      `${this.backendUrl}/customer/auth/update/${userId}`,
      profile,
      { headers }
    );
  }

  /** Registra l’utente sul backend (endpoint protetto) */
  registerCustomer(): Observable<any> {
    const token = this.keycloakService.profile?.token;
    if (!token) {
      return throwError(() => new Error('Utente non autenticato.'));
    }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post(
      `${this.backendUrl}/customer/auth/register`, // endpoint protetto
      {},
      { headers }
    );
  }
}
