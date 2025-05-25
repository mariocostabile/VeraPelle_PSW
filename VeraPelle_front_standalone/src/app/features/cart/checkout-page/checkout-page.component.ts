import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../../core/services/cart/cart.service';
import { OrderService } from '../../../core/services/order/order.service';
import { Observable } from 'rxjs';
import { CartDTO } from '../../../core/models/cart-dto';
import { CreateOrderRequest, OrderItemDTO, PaymentInfoDTO } from '../../../core/models/create-order-request';
import {CartItemDTO} from '@app/core/models/cart-item-dto';
import { take } from 'rxjs';


@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.scss']
})
export class CheckoutPageComponent implements OnInit {
  checkoutForm!: FormGroup;
  cart$!: Observable<CartDTO>;
  loading = false;
  errorMessage: string | null = null;

  private fb = inject(FormBuilder);
  private cartService = inject(CartService);
  private orderService = inject(OrderService);
  private router = inject(Router);

  ngOnInit() {
    // Init form
    this.checkoutForm = this.fb.group({
      shippingAddress: ['', Validators.required],
      cardNumber: ['', [Validators.required, Validators.minLength(12)]],
      expiry: ['', Validators.required],
      cvv: ['', Validators.required]
    });

    // Load and merge cart if needed
    this.cart$ = this.cartService.getCart();
  }

  onSubmit() {
    if (this.checkoutForm.invalid) return;
    this.errorMessage = null;
    this.loading = true;

    this.cart$
      .pipe(take(1))       // prendi solo la prima emissione e poi completa
      .subscribe({
        next: cart => {
          // 1) Costruisci gli orderItems
          const orderItems: OrderItemDTO[] = cart.items.map(i => ({
            productId: i.product.id!,
            quantity: i.quantity
          }));

          // 2) Prendi i valori del form
          const { shippingAddress, cardNumber, expiry, cvv } =
            this.checkoutForm.value;
          const paymentInfo: PaymentInfoDTO = { cardNumber, expiry, cvv };

          // 3) Prepara la request
          const req: CreateOrderRequest = {
            shippingAddress,
            items: orderItems,
            paymentInfo
          };

          // 4) Chiama il service con il payload corretto
          this.orderService.createOrder(req).subscribe({
            next: order => {
              // Se ha successo, svuota il carrello e naviga
              this.cartService.clearCart().subscribe(() => {
                this.router.navigate(['/order-confirmation', order.id]);
                this.loading = false;
              });
            },
            error: err => {
              this.errorMessage = err?.message || 'Errore durante il checkout';
              this.loading = false;
            }
          });
        },
        error: () => {
          // Errore a leggere il carrello
          this.errorMessage = 'Impossibile caricare il carrello';
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

}
