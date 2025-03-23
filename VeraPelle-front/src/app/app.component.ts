import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'app/core/services/keycloak/keycloak.service';
import { CustomerService } from 'app/core/services/customer/customer.service';

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
    // Se non c'è il token, l'utente è anonimo: non eseguo nessuna chiamata.
    if (!this.keycloakService.profile?.token) {
      return;
    }

    // L'utente è loggato: verifichiamo se esiste nel DB.
    this.customerService.getCustomerProfile().subscribe({
      next: (res) => {
        console.log('Utente già presente nel DB:', res);
      },
      error: (err) => {
        // Se l'errore è 404, l'utente non è presente nel DB e lo registriamo.
        if (err.status === 404) {
          this.customerService.registerCustomer().subscribe({
            next: () => console.log('Utente registrato con successo nel DB.'),
            error: (regErr) => console.error('Errore in fase di registrazione:', regErr)
          });
        } else {
          console.error('Errore durante la verifica del profilo:', err);
        }
      }
    });
  }
}
