import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

import { AuthenticationAPIService } from '../../api';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../../api';

export interface DecodedToken {
  sub: string;
  iat: number;
  exp: number;
  role: string;
  pwd_chg_req: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'bookshelf_jwt';
  private readonly authenticationAPIService = inject(AuthenticationAPIService);
  private readonly router = inject(Router);

  // Reactive authentication state
  private readonly authToken = signal<string | null>(this.getStoredTokenSafe());

  private readonly decodedToken = computed<DecodedToken | null>(() => {
    const token = this.authToken();
    if (!token) return null;
    return this.decodeToken(token);
  });

  public readonly isLoggedIn = computed(() => {
    const token = this.decodedToken();
    if (!token) return false;
    return token.exp * 1000 > Date.now();
  });

  public readonly userRole = computed(() => {
    return this.decodedToken()?.role || null;
  });

  public readonly isPasswordChangeRequired = computed(() => {
    return this.decodedToken()?.pwd_chg_req || false;
  });

  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.authenticationAPIService.authenticate(credentials).pipe(
      tap(response => {
        if (response.token) {
          this.storeToken(response.token);
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthenticationResponse> {
    return this.authenticationAPIService.register(request).pipe(
      tap(response => {
        if (response.token) {
          this.storeToken(response.token);
        }
      })
    );
  }

  logout(): void {
    this.removeStoredTokenSafe();
    this.authToken.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.authToken();
  }

  private storeToken(token: string): void {
    this.setStoredTokenSafe(token);
    this.authToken.set(token);
  }

  // --- Safe Storage Wrappers ---

  private getStoredTokenSafe(): string | null {
    try {
      return localStorage.getItem(this.TOKEN_KEY);
    } catch {
      return null;
    }
  }

  private setStoredTokenSafe(token: string): void {
    try {
      localStorage.setItem(this.TOKEN_KEY, token);
    } catch {
      // Storage failure handled silently
    }
  }

  private removeStoredTokenSafe(): void {
    try {
      localStorage.removeItem(this.TOKEN_KEY);
    } catch {
      // Storage removal failure handled silently
    }
  }

  private decodeToken(token: string): DecodedToken | null {
    try {
      const payloadBase64 = token.split('.')[1];
      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(jsonPayload);
    } catch {
      return null;
    }
  }
}
