// src/app/features/home/home.component.ts
import { Component, OnInit }           from '@angular/core';
import { CommonModule }                from '@angular/common';
import { RouterModule, Router }        from '@angular/router';
import { KeycloakService }             from '../../core/services/keycloak/keycloak.service';
import { ProductListComponent }        from '../store/product-list/product-list.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ProductListComponent
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  constructor(
    private kc: KeycloakService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Se ADMIN, redirect automatico alla console
    if (this.kc.profile?.token && this.kc.hasRole('ADMIN')) {
      this.router.navigate(['/admin/users']);
    }
  }
}
