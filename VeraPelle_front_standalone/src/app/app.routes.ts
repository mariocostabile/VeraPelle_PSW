// src/app/app.routes.ts

import { Routes }              from '@angular/router';
import { HomeComponent }       from './features/home/home.component';
import { ProfiloComponent }    from './features/profilo/profilo.component';
import { authGuard }           from './core/guards/auth.guard';
import { ProductListComponent }   from './features/store/product-list/product-list.component';
import { ProductDetailComponent } from './features/store/product-detail/product-detail.component';

export const routes: Routes = [
  { path: '',        component: HomeComponent },
  { path: 'store',   component: ProductListComponent },
  { path: 'store/products/:id', component: ProductDetailComponent },
  { path: 'profilo', component: ProfiloComponent, canActivate: [authGuard] },
  {
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/admin.module').then(m => m.AdminModule),
    canActivate: [authGuard]
  },
  { path: '**',      redirectTo: '' }
];
