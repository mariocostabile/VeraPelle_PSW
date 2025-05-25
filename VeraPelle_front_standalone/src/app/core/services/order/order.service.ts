// src/app/core/services/order.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateOrderRequest } from '../../models/create-order-request';
import { OrderDTO } from '../../models/order-dto';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly baseUrl = 'http://localhost:8080/api/auth/orders';

  constructor(private http: HttpClient) {}

  /**
   * Crea un nuovo ordine.
   * POST /api/auth/orders
   */
  createOrder(req: CreateOrderRequest): Observable<OrderDTO> {
    return this.http.post<OrderDTO>(
      this.baseUrl,
      req,
      { withCredentials: true }
    );
  }

  /**
   * Recupera la lista di ordini per l'utente autenticato.
   * GET /api/auth/orders
   */
  getOrders(): Observable<OrderDTO[]> {
    return this.http.get<OrderDTO[]>(
      this.baseUrl,
      { withCredentials: true }
    );
  }

  /**
   * Recupera i dettagli di un singolo ordine.
   * GET /api/auth/orders/{id}
   */
  getOrder(id: number): Observable<OrderDTO> {
    return this.http.get<OrderDTO>(
      `${this.baseUrl}/${id}`,
      { withCredentials: true }
    );
  }
}
