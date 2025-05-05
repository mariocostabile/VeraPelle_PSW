// src/main.ts

import { bootstrapApplication } from '@angular/platform-browser';
import { importProvidersFrom, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppComponent }         from './app/app.component';
import { routes }               from './app/app.routes';
import { HttpTokenInterceptor } from './app/core/interceptor/http-token.interceptor';
import { KeycloakService }      from './app/core/services/keycloak/keycloak.service';
import { CustomerService }      from './app/core/services/customer/customer.service';

export function appInitializerFactory(
  kc: KeycloakService,
  customer: CustomerService
): () => Promise<void> {
  return () =>
    // 1) init Keycloak e check-sso
    kc.init().then(async () => {
      // 2) se vengo da "Registrati", faccio il POST di sync
      if (localStorage.getItem('isRegistering') === 'true') {
        localStorage.removeItem('isRegistering');
        // usa lastValueFrom se toPromise dà warning
        await customer.registerCustomer().toPromise();
        console.log('✅ Utente sincronizzato in Postgres');
      }
    });
}

bootstrapApplication(AppComponent, {
  providers: [
    // 0) BrowserModule per direttive comuni
    importProvidersFrom(BrowserModule),

    // 1) Routing
    provideRouter(routes),

    // 2) HttpClient + interceptor
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: HttpTokenInterceptor, multi: true },

    // 3) KeycloakService & CustomerService
    KeycloakService,
    CustomerService,

    // 4) Inizializzazione Keycloak (e sincronizzazione utente)
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializerFactory,
      deps: [KeycloakService, CustomerService],
      multi: true
    }
  ]
})
  .catch(err => console.error(err));
