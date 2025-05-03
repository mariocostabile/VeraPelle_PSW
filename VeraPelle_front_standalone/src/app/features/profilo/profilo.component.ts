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
      firstName:   [{ value: '', disabled: true }, Validators.required],
      lastName:    [{ value: '', disabled: true }, Validators.required],
      email:       [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      phone:       [''],
      address:     [''],
      dateOfBirth: ['']
    });
  }

  ngOnInit(): void {
    this.customerService.getCustomerProfile()
      .pipe(
        catchError(() => {
          this.error = true;
          return of(null);
        })
      )
      .subscribe(profile => {
        if (profile) {
          // popola il form, inclusi i campi disabilitati
          this.form.patchValue(profile);
        }
        this.loading = false;
      });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    // getRawValue include anche i campi disabled: avrai un CustomerDTO completo
    const payload = this.form.getRawValue() as CustomerDTO;

    this.customerService.updateCustomer(payload)
      .subscribe({
        next: updated => {
          this.form.patchValue(updated);
          alert('Profilo aggiornato con successo!');
        },
        error: () => alert('Errore durante l\'aggiornamento.')
      });
  }

}
