// src/app/features/home/home.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { KeycloakService } from '../../core/services/keycloak/keycloak.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="home">
      <h1>Benvenuto in VeraPelle!</h1>
      <p>Questa è la pagina principale dell’app.</p>
    </section>
  `,
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  constructor(
    private kc: KeycloakService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Se autenticato e con ruolo ADMIN, redirect automatico alla console admin
    if (this.kc.profile?.token && this.kc.hasRole('ADMIN')) {
      this.router.navigate(['/admin/users']);
    }
  }
}
