// customer.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from '../keycloak/keycloak.service'; // percorso corretto
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService
  ) { }

  registerCustomer(): Observable<any> {
    // Ottieni il token da KeycloakService
    const token = this.keycloakService.profile?.token;

    // Imposta l’header Authorization
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    // Chiamata POST a /customer/register
    return this.http.post('/customer/register', {}, { headers });
  }
}
