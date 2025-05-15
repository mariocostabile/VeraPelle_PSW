// src/app/features/admin/user-form/user-form.component.ts

import { Component, OnInit, inject }        from '@angular/core';
import { CommonModule }                     from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router }           from '@angular/router';

import { AdminUserService }                 from '../admin-user.service';
import { CustomerDTO }                      from '../../../core/models/customer-dto';

@Component({
  standalone: true,
  selector: 'app-user-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  private fb     = inject(FormBuilder);
  private route  = inject(ActivatedRoute);
  private router = inject(Router);
  private svc    = inject(AdminUserService);

  form = this.fb.group({
    id:           [''],

    // questi campi sono disabilitati → immodificabili
    firstName:    [{ value: '', disabled: true }, Validators.required],
    lastName:     [{ value: '', disabled: true }, Validators.required],
    email:        [{ value: '', disabled: true }, [Validators.required, Validators.email]],

    // questi restano editabili
    phone:        [''],
    address:      [''],
    dateOfBirth:  ['']
  });

  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'ID utente mancante';
      return;
    }
    this.loading = true;
    this.svc.getById(id).subscribe({
      next: user => {
        // patchValue riempie anche i campi disabilitati
        this.form.patchValue(user);
        this.loading = false;
      },
      error: () => {
        this.error = 'Utente non trovato';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    // getRawValue() include anche i campi disabilitati
    if (this.form.invalid) return;
    this.loading = true;
    const dto = this.form.getRawValue() as CustomerDTO;
    this.svc.update(dto.id!, dto).subscribe({
      next: () => this.router.navigate(['/admin/users']),
      error: () => {
        this.error = 'Errore durante l’aggiornamento';
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/users']);
  }
}
