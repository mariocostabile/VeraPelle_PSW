import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'app/services/keycloak/keycloak.service';
import { UserProfile } from 'app/services/keycloak/user-profile';

@Component({
  standalone: false,
  selector: 'app-profilo',
  templateUrl: './profilo.component.html',
  styleUrls: ['./profilo.component.css']
})
export class ProfiloComponent implements OnInit {
  profile?: UserProfile;

  constructor(private keycloakService: KeycloakService) { }

  ngOnInit(): void {
    this.profile = this.keycloakService.profile;
  }
}
