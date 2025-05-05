// src/app/features/admin/admin.module.ts

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { AdminRoutingModule } from './admin-routing.module';
import { UserListComponent }  from './user-list/user-list.component';

@NgModule({
  // Rimuoviamo 'declarations' perché UserListComponent è standalone
  imports: [
    CommonModule,         // per tutte le direttive di base
    HttpClientModule,     // per HttpClient nel service
    AdminRoutingModule,   // monta le rotte /admin/*
    UserListComponent     // importa il componente standalone
  ]
})
export class AdminModule { }
