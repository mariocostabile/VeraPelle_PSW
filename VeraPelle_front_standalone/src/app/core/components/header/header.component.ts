// src/app/core/components/header/header.component.ts
import { Component, inject, OnInit, HostListener, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule, NgIf, NgForOf } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { filter, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { KeycloakService } from '../../../core/services/keycloak/keycloak.service';
import { ProductService } from '@app/core/services/product/product.service';
import { ProductPublicDTO } from '@app/core/models/product-public-dto';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    NgIf,
    NgForOf,
    ReactiveFormsModule
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, AfterViewInit {
  public kc = inject(KeycloakService);
  private router = inject(Router);
  private productService = inject(ProductService);

  // Ora static: false per legarsi dopo il render del *ngIf
  @ViewChild('searchRef', { read: ElementRef, static: false })
  searchRef!: ElementRef<HTMLElement>;

  isAdmin = false;
  currentUrl = '';

  // Search: FormControl non-nullable
  searchControl = new FormControl<string>('', { nonNullable: true });
  suggestions: ProductPublicDTO[] = [];
  loadingSuggestions = false;

  get isAuthenticated(): boolean {
    return !!this.kc.profile?.token;
  }

  async ngOnInit(): Promise<void> {
    this.isAdmin = this.kc.hasRole('ADMIN');

    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: NavigationEnd) => this.currentUrl = e.urlAfterRedirects);

    this.searchControl.valueChanges
      .pipe(
        debounceTime(200),
        distinctUntilChanged(),
        filter(term => term.length >= 2),
        switchMap(term => {
          this.loadingSuggestions = true;
          return this.productService.suggestProducts(term);
        })
      )
      .subscribe(list => {
        this.suggestions = list;
        this.loadingSuggestions = false;
      });
  }

  ngAfterViewInit(): void {
    // ViewChild è ora disponibile
  }

  @HostListener('document:click', ['$event.target'])
  onClickOutside(target: HTMLElement): void {
    if (this.searchRef && !this.searchRef.nativeElement.contains(target)) {
      this.suggestions = [];
    }
  }

  onSelectProduct(prod: ProductPublicDTO): void {
    // Rotta aggiornata
    this.router.navigate(['/store/products', prod.id]);
    this.suggestions = [];
    this.searchControl.setValue('');
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
