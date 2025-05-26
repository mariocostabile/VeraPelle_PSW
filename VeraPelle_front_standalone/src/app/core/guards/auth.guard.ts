import { inject }                 from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService }       from '../services/keycloak/keycloak.service';

export const authGuard: CanActivateFn = () => {
  const kc = inject(KeycloakService);
  const router = inject(Router);

  if (kc.profile?.token) {
    return true;
  } else {
    // Non autenticato → login e, al termine, ritorno esattamente qui (es. /#/checkout)
    kc.login({
      redirectUri: window.location.href
    });
    return false;
  }
};
