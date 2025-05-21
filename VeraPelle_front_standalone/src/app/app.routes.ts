// src/app/app.routes.ts

import { Routes }                   from '@angular/router';
import { HomeComponent }            from './features/home/home.component';
import { ProfiloComponent }         from './features/profilo/profilo.component';
import { authGuard }                from './core/guards/auth.guard';
import { ProductListComponent }     from './features/store/product-list/product-list.component';
import { ProductDetailComponent }   from './features/store/product-detail/product-detail.component';
import { CartPageComponent }        from './features/cart/cart-page/cart-page.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },

  // 1) Rotta specifica dettaglio prodotto
  { path: 'store/products/:id', component: ProductDetailComponent },

  // 2) Rotta generica lista prodotti
  { path: 'store', component: ProductListComponent },

  // 3) Pagina dedicata Carrello
  { path: 'cart', component: CartPageComponent },

  // 4) Profilo (protetto)
  { path: 'profilo', component: ProfiloComponent, canActivate: [authGuard] },

  // 5) Area admin (lazy-loaded, protetta)
  {
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/admin.module').then(m => m.AdminModule),
    canActivate: [authGuard]
  },

  // 6) Wildcard → home
  { path: '**', redirectTo: '' }
];
