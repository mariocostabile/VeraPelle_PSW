// src/app/features/cart/cart-page/cart-page.component.ts

import {Component, inject, OnInit} from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { CartService }       from '../../../core/services/cart/cart.service';
import { CartDTO }           from '../../../core/models/cart-dto';
import { CartItemComponent } from '../cart-item/cart-item.component';
import { RouterModule, Router } from '@angular/router';
import {CartItemDTO} from '@app/core/models/cart-item-dto';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [
    CommonModule,
    CartItemComponent,
    RouterModule
  ],
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss']
})
export class CartPageComponent implements OnInit {
  cart: CartDTO | null = null;
  loading = false;
  private location    = inject(Location);

  constructor(
    private cartService: CartService,
  ) {}

  private router = inject(Router);

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: dto => {
        this.cart = dto;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.location.back();
  }

  onQuantityChange(item: CartItemDTO, delta: number): void {
    const newQty = item.quantity + delta;
    if (newQty < 1) return;
    this.cartService.updateItem(item.id, newQty).subscribe({
      next: updatedDto => {
        // aggiorno solo l'item e il totale
        item.quantity = newQty;
        item.subtotal = updatedDto.subtotal;
        if (this.cart) {
          this.cart.total = this.cart.items
            .reduce((sum, it) => sum + it.subtotal, 0);
        }
      },
      error: () => {
        // opzionale: rollback o messaggio di errore
      }
    });
  }

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

  onClear(): void {
    this.cartService.clearCart().subscribe({
      next: () => {
        if (!this.cart) return;
        // svuoto solo items e aggiorno total
        this.cart.items = [];
        this.cart.total = 0;
      },
      error: () => {
        // opzionale: mostra un messaggio di errore
      }
    });
  }
}
