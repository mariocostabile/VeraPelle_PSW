// src/app/features/header/header.component.ts

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NgIf, NgForOf } from '@angular/common';
import { KeycloakService } from '../../../core/services/keycloak/keycloak.service';
import { CategoryService } from '../../../core/services/category/category.service';
import { CategoryDTO } from '../../../core/models/category-dto';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf, NgForOf],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  public kc = inject(KeycloakService);
  private categoryService = inject(CategoryService);
  private router = inject(Router);

  isAdmin = false;
  categories: CategoryDTO[] = [];

  get isAuthenticated(): boolean {
    return !!this.kc.profile?.token;
  }

  async ngOnInit(): Promise<void> {
    // Determina se l'utente è admin
    this.isAdmin = this.kc.hasRole('ADMIN');

    if (!this.isAdmin) {
      // Carica le categorie solo per utenti non-admin
      this.categoryService.getCategories().subscribe({
        next: cats => this.categories = cats,
        error: err => console.error('Errore caricamento categorie', err)
      });
    }
  }

  login(): Promise<void> | undefined {
    return this.kc.login({ redirectUri: window.location.origin + '/' });
  }

  register(): Promise<void> | undefined {
    return this.kc.register();
  }

  logout(): void {
    this.kc.logout();
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}
