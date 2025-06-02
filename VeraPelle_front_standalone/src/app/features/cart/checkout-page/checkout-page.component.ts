import { Component, OnInit, inject } from '@angular/core';
import {CommonModule, Location} from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../../core/services/cart/cart.service';
import { OrderService } from '../../../core/services/order/order.service';
import { Observable } from 'rxjs';
import { CartDTO } from '../../../core/models/cart-dto';
import { CreateOrderRequest, PaymentInfoDTO } from '../../../core/models/create-order-request';
import {CartItemDTO} from '@app/core/models/cart-item-dto';
import { take } from 'rxjs';
import { CustomerService } from '../../../core/services/customer/customer.service';





@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.scss'],
})
export class CheckoutPageComponent implements OnInit {
  checkoutForm!: FormGroup;
  loading = false;
  errorMessage: string | null = null;
  cart?: CartDTO;
  cartVersion!: number;



  private fb = inject(FormBuilder);
  private cartService = inject(CartService);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private customerService = inject(CustomerService);


  ngOnInit() {
    // Init form
    this.checkoutForm = this.fb.group({
      shippingAddress: ['', Validators.required],
      cardNumber: ['', [Validators.required, Validators.minLength(12)]],
      expiry: ['', Validators.required],
      cvv: ['', Validators.required]
    });

    // 2) Pre-riempi shippingAddress dal profilo utente
    this.customerService.getCustomerProfile()
      .pipe(take(1))
      .subscribe(profile => {
        if (profile?.address) {
          this.checkoutForm.patchValue({
            shippingAddress: profile.address
          });
        }
      });

    // 3) Ora leggiamo il carrello UNA SOLA VOLTA e salviamo anche versione
    this.cartService.getCart().pipe(take(1)).subscribe(cartDto => {
      this.cart = cartDto;             // salviamo oggetti e totali per la view
      this.cartVersion = cartDto.version; // ← salviamo la “version” per l’optimistic lock
    });
  }

  onSubmit() {
    if (this.checkoutForm.invalid) return;
    this.errorMessage = null;
    this.loading = true;

    // 1) Estrai i valori dal form
    const { shippingAddress, cardNumber, expiry, cvv } = this.checkoutForm.value;
    const paymentInfo: PaymentInfoDTO = { cardNumber, expiry, cvv };

    // 2) Costruisci la request inviando solo la versione del carrello
    const req: CreateOrderRequest = {
      shippingAddress,
      paymentInfo,
      cartVersion: this.cartVersion
    };

    // 3) Chiama il service per creare l’ordine
    this.orderService.createOrder(req).subscribe({
      next: order => {
        // Se l’ordine viene creato con successo, svuota il carrello e naviga
        this.cartService.clearCart().subscribe(() => {
          this.router.navigate(['/order-confirmation', order.id]);
          this.loading = false;
        });
      },
      error: err => {
        // Se il server risponde 409, significa che la versione del carrello non combacia
        if (err.status === 409) {
          this.errorMessage = 'Il carrello è stato modificato da un’altra sessione. Ricarica e riprova.';
        } else {
          this.errorMessage = err?.message || 'Errore durante il checkout';
        }
        this.loading = false;
      }
    });
  }


  // sotto la tua classe CheckoutPageComponent
  /** Restituisce l’URL completo della miniatura */
  getThumbUrl(item: CartItemDTO): string {
    const t = item.thumbnailUrl || '';
    const normalized = t.startsWith('/') ? t : `/${t}`;
    return `http://localhost:8080${normalized}`;
  }

  /**
   * Aumenta o diminuisce la quantità di un item e aggiorna la vista
   */
  onQuantityChange(item: CartItemDTO, delta: number): void {
    const newQty = item.quantity + delta;
    if (newQty < 1) return;

    this.cartService.updateItem(item.id, newQty).subscribe({
      next: updatedDto => {
        // aggiorna solo l'item e il totale
        item.quantity = newQty;
        item.subtotal = updatedDto.subtotal;
        if (this.cart) {
          this.cart.total = this.cart.items
            .reduce((sum, it) => sum + it.subtotal, 0);
        }
      },
      error: () => {
        // opzionale: messaggio di errore
      }
    });
  }

  /**
   * Rimuove un item dal carrello e aggiorna la vista
   */
  onRemove(item: CartItemDTO): void {
    this.cartService.removeItem(item.id).subscribe({
      next: () => {
        if (!this.cart) return;
        this.cart.items = this.cart.items.filter(it => it.id !== item.id);
        this.cart.total = this.cart.items
          .reduce((sum, it) => sum + it.subtotal, 0);
      },
      error: () => {
        // opzionale: messaggio di errore
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/cart']);
  }

}
