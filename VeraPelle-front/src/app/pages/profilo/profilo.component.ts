import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'app/core/services/keycloak/keycloak.service';
import { UserProfile } from 'app/core/services/keycloak/user-profile';
import { CustomerService, CustomerDTO } from 'app/core/services/customer/customer.service';

@Component({
  standalone: false,
  selector: 'app-profilo',
  templateUrl: './profilo.component.html',
  styleUrls: ['./profilo.component.css']
})
export class ProfiloComponent implements OnInit {
  // Dati base da Keycloak
  profile?: UserProfile;
  // Dati estesi dal DB
  customerData?: CustomerDTO;

  constructor(
    private keycloakService: KeycloakService,
    private customerService: CustomerService
  ) { }

  ngOnInit(): void {
    // 1) Dati base Keycloak
    this.profile = this.keycloakService.profile;

    // 2) Dati aggiuntivi dal DB
    if (this.profile?.token) {
      this.customerService.getCustomerProfile().subscribe({
        next: (data) => {
          this.customerData = data;
          console.log('Dati utente dal DB:', data);
        },
        error: (err) => {
          console.error('Errore recupero dati utente dal DB:', err);
        }
      });
    }
  }
}
