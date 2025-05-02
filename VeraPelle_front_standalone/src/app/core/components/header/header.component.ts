import { Component, inject } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { RouterLink }         from '@angular/router';
import { KeycloakService }    from '../../services/keycloak/keycloak.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  private kc = inject(KeycloakService);

  /** Autenticazione basata solo sul token Keycloak */
  get isAuthenticated(): boolean {
    return !!this.kc.profile?.token;
  }

  login(): Promise<void> | undefined {
    return this.kc.login({ redirectUri: window.location.origin + '/' });
  }

  register(): Promise<void> | undefined {
    return this.kc.register();
  }

  logout(): Promise<void> | undefined {
    return this.kc.logout();
  }
}
