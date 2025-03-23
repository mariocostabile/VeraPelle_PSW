import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'app/services/keycloak/keycloak.service';
import { CustomerService } from 'app/services/customer/customer.service';

@Component({
  standalone: false,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'VeraPelle-front';

  constructor(
    private keycloakService: KeycloakService,
    private customerService: CustomerService
  ) {}

  ngOnInit(): void {
    // Se l’utente è loggato, tentiamo la registrazione sul nostro DB
    if (this.keycloakService.profile?.token) {
      this.customerService.registerCustomer().subscribe({
        next: () => console.log('Utente registrato o già presente nel DB.'),
        error: err => console.error('Errore registrazione DB:', err)
      });
    }
  }
}
