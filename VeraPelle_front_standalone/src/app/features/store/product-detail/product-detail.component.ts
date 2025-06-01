// src/app/features/store/product-detail/product-detail.component.ts

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }               from '@angular/common';
import { ActivatedRoute, ParamMap }   from '@angular/router';
import { Location }                   from '@angular/common';
import { switchMap }                  from 'rxjs/operators';
import { StoreService, CartItemRequest } from '@app/core/services/store/store.service';
import { CartService }                   from '@app/core/services/cart/cart.service';
import { ProductPublicDTO }              from '@app/core/models/product-public-dto';
import { Router }      from '@angular/router';
import {ProductVariantDTO} from '@app/core/models/product-variant-dto';
import {KeycloakService} from '@app/core/services/keycloak/keycloak.service';




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
  private router = inject(Router);
  private kc = inject(KeycloakService);


  product!: ProductPublicDTO;
  selectedImage: string | null = null;
  currentIndex = 0;
  loading = true;
  error: string | null = null;

// Variante selezionata e quantità
  selectedVariant?: ProductVariantDTO;
  quantity = 1;
  maxQuantity = 0;

// messaggi di validazione inline
  colorError: string | null = null;
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

          // reset variante + quantità
          this.selectedVariant = undefined;
          this.maxQuantity     = 0;
          this.quantity        = 1;

          this.loading = false;
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

  selectVariant(v: ProductVariantDTO): void {
    this.colorError = this.quantityError = this.successMessage = null;
    if (this.selectedVariant?.colorId === v.colorId) {
      // Deseleziona la variante
      this.selectedVariant = undefined;
      this.maxQuantity     = 0;
      this.quantity        = 1;
    } else {
      // Seleziona la nuova variante
      this.selectedVariant = v;
      this.maxQuantity     = v.stockQuantity;
      this.quantity        = v.stockQuantity > 0 ? 1 : 0;
    }
  }


  decrement(): void {
    if (!this.selectedVariant) return;
    this.quantity = Math.max(1, this.quantity - 1);
  }

  increment(): void {
    if (!this.selectedVariant) return;
    this.quantity = Math.min(this.maxQuantity, this.quantity + 1);
  }


  addToCart(): void {
    if (!this.product) return;

    // reset iniziale di tutti i messaggi
    this.colorError     = null;
    this.quantityError  = null;
    this.successMessage = null;

    // validazione colore
    if (!this.selectedVariant) {
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
      colorId:   this.selectedVariant.colorId,
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
