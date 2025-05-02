// src/app/core/services/keycloak/keycloak.service.ts

import { Injectable } from '@angular/core';
import Keycloak, { KeycloakConfig, KeycloakTokenParsed } from 'keycloak-js';
import { UserProfile } from './user-profile';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private kc?: Keycloak;
  private _profile?: UserProfile;

  get profile(): UserProfile | undefined {
    return this._profile;
  }

  /** Il token decodificato (claims) */
  get tokenParsed(): KeycloakTokenParsed | undefined {
    return this.kc?.tokenParsed;
  }

  /** L’ID utente (sub) dal JWT */
  getUserId(): string | undefined {
    return this.kc?.tokenParsed?.sub;
  }

  async init(): Promise<void> {
    const config: KeycloakConfig = {
      url: 'http://localhost:8081',
      realm: 'ProgettoPSW_PelleVera',
      clientId: 'pelle-vera-api'
    };
    this.kc = new Keycloak(config);

    const authenticated = await this.kc.init({ onLoad: 'check-sso' });
    if (!authenticated) return;

    const p = await this.kc.loadUserProfile();
    this._profile = p as UserProfile;
    this._profile.token = this.kc.token!;
  }

  login(options?: Keycloak.KeycloakLoginOptions): Promise<void> | undefined {
    return this.kc?.login({
      redirectUri: window.location.origin + '/',
      ...options
    });
  }

  logout(): Promise<void> | undefined {
    this._profile = undefined;
    return this.kc?.logout({
      redirectUri: window.location.origin + '/'
    });
  }

  register(): Promise<void> | undefined {
    // ① Imposto il flag per distinguere il flow di registrazione
    localStorage.setItem('isRegistering', 'true');
    // ② Avvio il flow Keycloak in modalità "register"
    return this.kc?.login({
      action: 'register',
      redirectUri: window.location.origin + '/'
    });
  }
}
