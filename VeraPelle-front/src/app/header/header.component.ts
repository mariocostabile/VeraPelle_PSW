import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'app/services/keycloak/keycloak.service';
import { UserProfile } from 'app/services/keycloak/user-profile';

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
    // Recupera i dati dell'utente (se loggato)
    this.profile = this.keycloakService.profile;
  }

  isLoggedIn(): boolean {
    // L’utente è loggato se esiste un token
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
