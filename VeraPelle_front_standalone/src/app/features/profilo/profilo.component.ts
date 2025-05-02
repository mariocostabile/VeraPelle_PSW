import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }              from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { CustomerService, CustomerDTO } from '../../core/services/customer/customer.service';
import { catchError }                from 'rxjs/operators';
import { of }                        from 'rxjs';

@Component({
  selector: 'app-profilo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profilo.component.html',
  styleUrls: ['./profilo.component.scss']
})
export class ProfiloComponent implements OnInit {
  private customerService = inject(CustomerService);
  form: FormGroup;
  loading = true;
  error = false;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName:  ['', Validators.required],
      email:     ['', [Validators.required, Validators.email]],
      phone:     [''],
      address:   [''],
      dateOfBirth: ['']
    });
  }

  ngOnInit(): void {
    this.customerService.getCustomerProfile()
      .pipe(catchError(() => { this.error = true; return of(null); }))
      .subscribe(profile => {
        if (profile) {
          this.form.patchValue(profile);
        }
        this.loading = false;
      });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.customerService.updateCustomer(this.form.value as CustomerDTO)
      .subscribe({
        next: updated => {
          this.form.patchValue(updated);
          alert('Profilo aggiornato con successo!');
        },
        error: () => alert('Errore durante l\'aggiornamento.')
      });
  }
}
