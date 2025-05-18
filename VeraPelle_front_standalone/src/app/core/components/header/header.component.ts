// src/app/core/components/header/header.component.ts
import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, NgIf }        from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter }                    from 'rxjs/operators';
import { KeycloakService }           from '../../../core/services/keycloak/keycloak.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    NgIf
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  public kc = inject(KeycloakService);
  private router = inject(Router);

  isAdmin = false;
  currentUrl = '';

  get isAuthenticated(): boolean {
    return !!this.kc.profile?.token;
  }

  async ngOnInit(): Promise<void> {
    // Ruolo
    this.isAdmin = this.kc.hasRole('ADMIN');

    // Traccia la route corrente (utile se vuoi disabilitare search in /profilo)
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: NavigationEnd) => this.currentUrl = e.urlAfterRedirects);
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
