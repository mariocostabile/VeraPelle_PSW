// src/app/features/admin/user-list/user-list.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule }      from '@angular/common';       // ← serve per NgIf, NgFor
import { Router }            from '@angular/router';       // ← servono router.navigate
import { AdminUserService }  from '../admin-user.service';
import { CustomerDTO }       from '../../../core/models/customer-dto';

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {
  users: CustomerDTO[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private adminUserService: AdminUserService,
    private router: Router           // ← iniettiamo il Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;

    this.adminUserService.getAll().subscribe({
      next: data => {
        this.users = data;
        this.loading = false;
      },
      error: err => {
        this.error = err.message || 'Errore nel caricamento';
        this.loading = false;
      }
    });
  }

  deleteUser(id: string): void {
    if (!confirm('Sei sicuro di voler eliminare questo utente?')) {
      return;
    }
    this.adminUserService.delete(id).subscribe({
      next: () => this.loadUsers(),
      error: err => alert('Impossibile eliminare: ' + err.message)
    });
  }

  /**
   * Naviga al form di modifica dell'utente selezionato.
   */
  editUser(id: string): void {
    this.router.navigate(['/admin', 'users', id, 'edit']);
  }
}
