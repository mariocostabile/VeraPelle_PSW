// src/app/core/services/cart/cart.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { CartDTO } from '../../models/cart-dto';
import { CartItemDTO } from '../../models/cart-item-dto';
import { AddCartItemRequest } from '../../models/add-cart-item-request';
import { UpdateCartItemQuantityRequest } from '../../models/update-cart-item-quantity-request';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly baseUrl = 'http://localhost:8080/api/cart';

  /** Subject che tiene traccia del numero di item nel carrello */
  private itemCountSubject = new BehaviorSubject<number>(0);
  /** Observable pubblico per il count */
  public itemCount$ = this.itemCountSubject.asObservable();

  constructor(
    private http: HttpClient,
    private cookieService: CookieService
  ) {}

  /** Recupera il carrello (guest via cookie) */
  getCart(): Observable<CartDTO> {
    return this.http.get<CartDTO>(this.baseUrl, { withCredentials: true }).pipe(
      tap(cart => this.emitCount(cart))
    );
  }

  /** Aggiunge un item al carrello */
  addItem(req: AddCartItemRequest): Observable<CartItemDTO> {
    return this.http.post<CartItemDTO>(
      `${this.baseUrl}/items`,
      req,
      { withCredentials: true }
    ).pipe(
      tap(() => this.getCart().subscribe())
    );
  }

  /** Aggiorna la quantità di un item */
  updateItem(itemId: number, quantity: number): Observable<CartItemDTO> {
    const body: UpdateCartItemQuantityRequest = { quantity };
    return this.http.put<CartItemDTO>(
      `${this.baseUrl}/items/${itemId}`,
      body,
      { withCredentials: true }
    ).pipe(
      tap(() => this.getCart().subscribe())
    );
  }

  /** Rimuove un singolo item */
  removeItem(itemId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/items/${itemId}`,
      { withCredentials: true }
    ).pipe(
      tap(() => this.getCart().subscribe())
    );
  }

  /** Svuota tutto il carrello */
  clearCart(): Observable<void> {
    return this.http.delete<void>(
      this.baseUrl,
      { withCredentials: true }
    ).pipe(
      tap(() => this.itemCountSubject.next(0))
    );
  }

  /** Invia il count corrente */
  private emitCount(cart: CartDTO) {
    this.itemCountSubject.next(cart.items.length);
  }

  /** Fondi il carrello guest (dal cookie) in quello persistente del customer */
  mergeCart(): Observable<CartDTO> {
    return this.http
      .post<CartDTO>(
        `${this.baseUrl}/auth/merge`,
        {},                      // nessun body necessario
        { withCredentials: true }
      )
      .pipe(
        tap(cart => this.emitCount(cart))
      );
  }
}
