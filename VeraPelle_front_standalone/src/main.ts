// main.ts
import { bootstrapApplication, BrowserModule } from '@angular/platform-browser';
import { importProvidersFrom, APP_INITIALIZER } from '@angular/core';
import {
  provideRouter
} from '@angular/router';
import {
  provideHttpClient,
  withInterceptorsFromDi,
  HTTP_INTERCEPTORS
} from '@angular/common/http';

import { AppComponent }          from './app/app.component';
import { routes }                from './app/app.routes';
import { HttpTokenInterceptor }  from './app/core/interceptor/http-token.interceptor';
import { KeycloakService }       from './app/core/services/keycloak/keycloak.service';
import { CustomerService }       from './app/core/services/customer/customer.service';

export function appInitializerFactory(
  kc: KeycloakService,
  customer: CustomerService
): () => Promise<void> {
  return () =>
    // 1) init Keycloak
    kc.init().then(async () => {
      // 2) se vengo da "Registrati", faccio il POST di sync
      if (localStorage.getItem('isRegistering') === 'true') {
        localStorage.removeItem('isRegistering');
        await customer.registerCustomer().toPromise();
        console.log('✅ Utente sincronizzato in Postgres');
      }
    });
}

bootstrapApplication(AppComponent, {
  providers: [
    // 2) Routing
    provideRouter(routes),

    // 3) HttpClient + interceptor
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: HttpTokenInterceptor, multi: true },

    // 4) Keycloak & CustomerService + initializer
    KeycloakService,
    CustomerService,
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializerFactory,
      deps: [KeycloakService, CustomerService],
      multi: true
    }
  ]
})
  .catch(err => console.error(err));
