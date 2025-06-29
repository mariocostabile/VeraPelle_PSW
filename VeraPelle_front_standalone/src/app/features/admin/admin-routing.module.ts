// src/app/features/admin/admin-routing.module.ts

import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ProductListComponent }   from './product-list/product-list.component';
import { ProductFormComponent }   from './product-form/product-form.component';
import { CategoryListComponent }  from './category-list/category-list.component';
import { CategoryFormComponent }  from './category-form/category-form.component';
import { UserListComponent }      from './user-list/user-list.component';
import { UserFormComponent }      from './user-form/user-form.component';
import { ColorListComponent }     from './colors/color-list/color-list.component';    // ← nuovo
import { ColorFormComponent }     from './colors/color-form/color-form.component';    // ← nuovo

const routes: Routes = [
  // Prodotti
  { path: 'products',          component: ProductListComponent },
  { path: 'products/new',      component: ProductFormComponent },
  { path: 'products/:id/edit', component: ProductFormComponent },

  // Categorie
  { path: 'categories',          component: CategoryListComponent },
  { path: 'categories/new',      component: CategoryFormComponent },
  { path: 'categories/:id/edit', component: CategoryFormComponent },

  // Colori
  { path: 'colors',          component: ColorListComponent },
  { path: 'colors/new',      component: ColorFormComponent },
  { path: 'colors/:id/edit', component: ColorFormComponent },

  // Utenti
  { path: 'users',            component: UserListComponent },
  { path: 'users/:id/edit',   component: UserFormComponent },

  // Default
  { path: '', redirectTo: 'products', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {}
