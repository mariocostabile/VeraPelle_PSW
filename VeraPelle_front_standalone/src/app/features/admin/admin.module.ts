import { NgModule }         from '@angular/core';
import { CommonModule }     from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { AdminRoutingModule }    from './admin-routing.module';
import { ProductListComponent }  from './product-list/product-list.component';
import { ProductFormComponent }  from './product-form/product-form.component';
import { CategoryListComponent } from './category-list/category-list.component';
import { CategoryFormComponent } from './category-form/category-form.component';
import { UserListComponent }     from './user-list/user-list.component';

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    AdminRoutingModule,

    // Standalone components usati in questo modulo
    ProductListComponent,
    ProductFormComponent,
    CategoryListComponent,
    CategoryFormComponent,
    UserListComponent
  ]
})
export class AdminModule {}
