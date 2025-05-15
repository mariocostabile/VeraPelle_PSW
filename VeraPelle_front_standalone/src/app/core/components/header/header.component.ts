import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgForOf }   from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter }                        from 'rxjs/operators';
import { KeycloakService }               from '../../../core/services/keycloak/keycloak.service';
import { CategoryService }               from '../../../core/services/category/category.service';
import { CategoryDTO }                   from '../../../core/models/category-dto';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, NgIf, NgForOf],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  public kc = inject(KeycloakService);
  private categoryService = inject(CategoryService);
  private router = inject(Router);

  isAdmin = false;
  categories: CategoryDTO[] = [];
  currentUrl = '';

  get isAuthenticated(): boolean {
    return !!this.kc.profile?.token;
  }

  /** Mostra le categorie solo se NON admin e NON in /profilo */
  get showCategories(): boolean {
    return !this.isAdmin && this.currentUrl !== '/profilo';
  }

  async ngOnInit(): Promise<void> {
    // Ruolo
    this.isAdmin = this.kc.hasRole('ADMIN');

    // Traccia la route corrente
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: NavigationEnd) => this.currentUrl = e.urlAfterRedirects);

    // Carica le categorie solo per utenti normali
    if (!this.isAdmin) {
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
