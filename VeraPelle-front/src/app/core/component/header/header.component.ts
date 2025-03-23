import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'app/core/services/keycloak/keycloak.service';
import { UserProfile } from 'app/core/services/keycloak/user-profile';

@Component({
  standalone: false,
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  profile?: UserProfile;

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    // Carica il profilo dell'utente, se presente
    this.profile = this.keycloakService.profile;
  }

  isLoggedIn(): boolean {
    // L'utente è considerato loggato se esiste un token nel profilo
    return !!this.profile?.token;
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout();
  }

  register(): void {
    this.keycloakService.register();
  }
}
