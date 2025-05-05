// src/app/core/services/keycloak/keycloak.service.ts

import { Injectable } from '@angular/core';
import Keycloak, { KeycloakConfig, KeycloakTokenParsed, KeycloakRoles } from 'keycloak-js';
import { UserProfile } from './user-profile';

// Estendiamo KeycloakTokenParsed usando esattamente KeycloakRoles
interface ParsedToken extends KeycloakTokenParsed {
  realm_access?: KeycloakRoles;
  resource_access?: {
    [client: string]: KeycloakRoles;
  };
}

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private kc?: Keycloak;
  private _profile?: UserProfile;
  private _parsedToken?: ParsedToken;

  /** Profilo utente arricchito con token */
  get profile(): UserProfile | undefined {
    return this._profile;
  }

  /** Il token decodificato (claims) */
  private get tokenParsed(): ParsedToken | undefined {
    return this._parsedToken;
  }

  /** Restituisce l’ID utente (sub) */
  getUserId(): string | undefined {
    return this.tokenParsed?.sub;
  }

  /** Tutti i ruoli disponibili (realm + client) */
  get roles(): string[] {
    const realmRoles = this.tokenParsed?.realm_access?.roles ?? [];
    const clientRoles = Object.values(this.tokenParsed?.resource_access ?? {})
      .flatMap(acc => acc.roles ?? []);
    return Array.from(new Set([ ...realmRoles, ...clientRoles ]));
  }

  /** Controlla se ho il ruolo specificato */
  hasRole(role: string): boolean {
    return this.roles.includes(role);
  }

  /** Inizializza Keycloak e carica profilo + tokenParsed */
  async init(): Promise<void> {
    const config: KeycloakConfig = {
      url: 'http://localhost:8081',
      realm: 'ProgettoPSW_PelleVera',
      clientId: 'pelle-vera-api'
    };
    this.kc = new Keycloak(config);

    const authenticated = await this.kc.init({ onLoad: 'check-sso' });
    if (!authenticated) return;

    // carica profilo utente
    const p = await this.kc.loadUserProfile();
    this._profile = p as UserProfile;
    this._profile.token = this.kc.token!;

    // conserva anche il token decodificato
    this._parsedToken = this.kc.tokenParsed as ParsedToken;
  }

  /** Login Keycloak (default redirect su "/") */
  login(options?: Keycloak.KeycloakLoginOptions): Promise<void> | undefined {
    return this.kc?.login({
      redirectUri: window.location.origin + '/',
      ...options
    });
  }

  /** Logout e clear del profilo */
  logout(): Promise<void> | undefined {
    this._profile = undefined;
    this._parsedToken = undefined;
    return this.kc?.logout({
      redirectUri: window.location.origin + '/'
    });
  }

  /** Registrazione via Keycloak */
  register(): Promise<void> | undefined {
    localStorage.setItem('isRegistering', 'true');
    return this.kc?.login({
      action: 'register',
      redirectUri: window.location.origin + '/'
    });
  }
}
