// src/app/features/cart/cart-item/cart-item.component.ts

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule }                         from '@angular/common';
import { CartItemDTO }                          from '../../../core/models/cart-item-dto';

@Component({
  selector: 'app-cart-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart-item.component.html',
  styleUrls: ['./cart-item.component.scss']
})
export class CartItemComponent {
  @Input()  item!: CartItemDTO;
  @Output() quantityChange = new EventEmitter<number>();
  @Output() remove         = new EventEmitter<void>();

  /**
   * Ritorna l’URL completo per la miniatura,
   * includendo host e porta del back-end.
   */
  get thumbUrl(): string {
    const thumb = this.item.thumbnailUrl || '';
    const normalized = thumb.startsWith('/') ? thumb : `/${thumb}`;
    return `http://localhost:8080${normalized}`;
  }

  onIncrement(): void {
    this.quantityChange.emit(1);
  }

  onDecrement(): void {
    this.quantityChange.emit(-1);
  }

  onRemove(): void {
    this.remove.emit();
  }
}
