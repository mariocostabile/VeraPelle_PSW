import { bootstrapApplication } from '@angular/platform-browser';
import { importProvidersFrom, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideRouter, withHashLocation, Router } from '@angular/router';
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
  customer: CustomerService,
  router: Router
): () => Promise<void> {
  return () =>
    kc.init().then(async () => {
      if(kc.profile?.token){
        // ① Provo sempre a sincronizzare (upsert) l'utente sul DB interno
        try {
          await customer.registerCustomer().toPromise();
          console.log('✅ Utente creato/correttamente sincronizzato in Postgres');
        } catch (err: any) {
          if (err.status === 409) {
            // utente già presente: proseguo senza errori
            console.log('⚪️ Utente già esistente, skip register');
          } else {
            console.error('🔴 Errore in registerCustomer()', err);
          }
        }
      }else {
        console.log('⚪️ Nessun utente autenticato — skip register');
      }


      // Redirect automatico admin
      if (kc.hasRole('ADMIN') && window.location.pathname === '/') {
        await router.navigate(['/admin/products']);
      }
    });
}

bootstrapApplication(AppComponent, {
  providers: [
    // BrowserModule per direttive comuni
    importProvidersFrom(BrowserModule),

    // Routing con hash location strategy
    provideRouter(routes, withHashLocation()),

    // HttpClient + interceptor
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: HttpTokenInterceptor, multi: true },

    // Servizi Keycloak e Customer
    KeycloakService,
    CustomerService,

    // Inizializzazione Keycloak (sync registrazione + redirect admin)
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializerFactory,
      deps: [KeycloakService, CustomerService, Router],
      multi: true
    }
  ]
})
  .catch(err => console.error(err));
