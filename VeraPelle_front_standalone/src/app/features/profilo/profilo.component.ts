import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { CustomerService } from '../../core/services/customer/customer.service';
import { CustomerDTO } from '../../core/models/customer-dto';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { OrderService } from '../../core/services/order/order.service';
import { OrderDTO }       from '../../core/models/order-dto';
import { Observable }     from 'rxjs';

@Component({
  selector: 'app-profilo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './profilo.component.html',
  styleUrls: ['./profilo.component.scss']
})
export class ProfiloComponent implements OnInit {
  private customerService = inject(CustomerService);
  private router = inject(Router);
  private orderService = inject(OrderService);


  form: FormGroup;
  loading = true;
  error = false;
  orders$!: Observable<OrderDTO[]>;


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
          this.form.patchValue(profile);
        }
        this.loading = false;
        // Dopo caricamento profilo, inizializza orders$
        this.orders$ = this.orderService.getOrders();
      });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

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

  /** Torna alla home senza salvare */
  cancel(): void {
    this.router.navigate(['/']);
  }
}
