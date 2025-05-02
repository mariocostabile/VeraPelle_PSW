import { KeycloakService } from './keycloak.service';

export function kcFactory(kc: KeycloakService) {
  return () => kc.init();
}
