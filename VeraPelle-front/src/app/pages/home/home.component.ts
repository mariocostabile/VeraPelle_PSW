import { Component } from '@angular/core';
import { KeycloakService } from '../../core/services/keycloak/keycloak.service';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  constructor(private keycloakService: KeycloakService) {}

  // Chiama il metodo di login di Keycloak
  onLoginClick(): void {
    this.keycloakService.login();
  }

  // Chiama il metodo di logout di Keycloak
  onLogoutClick(): void {
    this.keycloakService.logout();
  }

  onRegisterClick(): void {
    this.keycloakService.register();
  }
}
