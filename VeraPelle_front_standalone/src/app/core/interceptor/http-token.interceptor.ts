// src/app/core/interceptor/http-token.interceptor.ts

import { Injectable, inject } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { KeycloakService } from '../services/keycloak/keycloak.service';

@Injectable({ providedIn: 'root' })
export class HttpTokenInterceptor implements HttpInterceptor {
  private kc = inject(KeycloakService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.kc.profile?.token;
    // intercetta sia /auth/ sia /admin/ (e in generale tutte le chiamate al tuo backend)
    if (token && (req.url.includes('/auth/') || req.url.includes('/admin/') || req.url.includes('/api/cart'))) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
