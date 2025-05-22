// src/app/features/store/product-detail/product-detail.component.ts

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }               from '@angular/common';
import { ActivatedRoute, ParamMap }   from '@angular/router';
import { Location }                   from '@angular/common';
import { switchMap }                  from 'rxjs/operators';

import { StoreService, CartItemRequest } from '@app/core/services/store/store.service';
import { CartService }                   from '@app/core/services/cart/cart.service';
import { ProductPublicDTO }              from '@app/core/models/product-public-dto';
import { ColorDTO }                      from '@app/core/models/color-dto';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {

  private route       = inject(ActivatedRoute);
  private store       = inject(StoreService);
  private cartService = inject(CartService);
  private location    = inject(Location);

  product: ProductPublicDTO | null = null;
  selectedImage: string | null      = null;
  currentIndex = 0;
  loading      = true;
  error: string | null = null;

  selectedColorId: number | null = null;
  quantity       = 1;
  maxQuantity    = 0;

  // messaggi di validazione inline
  colorError:    string | null = null;
  quantityError: string | null = null;

  // messaggio di successo
  successMessage: string | null = null;

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap((params: ParamMap) => {
          this.loading = true;
          this.error   = null;
          const id = Number(params.get('id'));
          return this.store.getProductById(id);
        })
      )
      .subscribe({
        next: p => {
          this.product       = p;
          this.currentIndex  = 0;
          this.selectedImage = p.imageUrls[0] || null;
          this.maxQuantity   = p.stockQuantity;
          this.quantity      = 1;
          this.loading       = false;
        },
        error: () => {
          this.error   = 'Prodotto non trovato';
          this.loading = false;
        }
      });
  }

  goBack(): void {
    this.location.back();
  }

  onSelectImage(url: string, idx: number): void {
    this.selectedImage = url;
    this.currentIndex  = idx;
  }

  onPrev(): void {
    if (!this.product) return;
    const len = this.product.imageUrls.length;
    this.currentIndex  = (this.currentIndex - 1 + len) % len;
    this.selectedImage = this.product.imageUrls[this.currentIndex];
  }

  onNext(): void {
    if (!this.product) return;
    const len = this.product.imageUrls.length;
    this.currentIndex  = (this.currentIndex + 1) % len;
    this.selectedImage = this.product.imageUrls[this.currentIndex];
  }

  selectColor(id: number): void {
    // se clicco sullo stesso colore, lo "deseleziono"
    if (this.selectedColorId === id) {
      this.selectedColorId = null;
    } else {
      this.selectedColorId = id;
    }
    // reset degli errori e del messaggio di successo
    this.colorError    = null;
    this.successMessage = null;
  }

  changeQuantity(delta: number): void {
    if (!this.product) return;
    this.quantity        = Math.min(this.maxQuantity, Math.max(1, this.quantity + delta));
    this.quantityError   = null;  // reset mirato
    this.successMessage  = null;  // rimuove eventuale messaggio di successo precedente
  }

  addToCart(): void {
    if (!this.product) return;

    // reset iniziale di tutti i messaggi
    this.colorError     = null;
    this.quantityError  = null;
    this.successMessage = null;

    // validazione colore
    if (!this.selectedColorId) {
      this.colorError = 'Per favore seleziona un colore.';
      return;
    }
    // validazione quantità
    if (this.quantity < 1 || this.quantity > this.maxQuantity) {
      this.quantityError = `La quantità deve essere tra 1 e ${this.maxQuantity}.`;
      return;
    }

    const req: CartItemRequest = {
      productId: this.product.id,
      colorId:   this.selectedColorId,
      quantity:  this.quantity
    };

    this.cartService.addItem(req).subscribe({
      next: () => {
        // mostra messaggio di conferma inline
        this.successMessage = 'Prodotto aggiunto correttamente al carrello!';
        // sparisce dopo 3 secondi
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: () => {
        this.quantityError = 'Impossibile aggiungere al carrello. Riprova.';
      }
    });
  }
}
