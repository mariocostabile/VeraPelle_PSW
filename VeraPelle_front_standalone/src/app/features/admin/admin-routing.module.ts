import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserListComponent } from './user-list/user-list.component';
import { ProductListComponent } from './product-list/product-list.component';
import { CategoryListComponent } from './category-list/category-list.component';

const routes: Routes = [
  // /admin/products → ProductListComponent
  { path: 'products', component: ProductListComponent },
  // /admin/categories → CategoryListComponent
  { path: 'categories', component: CategoryListComponent },
  // /admin/users → UserListComponent
  { path: 'users', component: UserListComponent },

  // redirect automatico su /admin → /admin/products
  { path: '', redirectTo: 'products', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
