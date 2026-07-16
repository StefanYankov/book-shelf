import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

import { AuthenticationAPIService } from '../../api';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../../api';

/**
 * Shape of the decoded JWT payload issued by the backend.
 * <p>
 * This is an implementation detail of {@link AuthService} — it is exported only so the
 * unit test can construct fake tokens. No component, guard, or other service should
 * consume it directly; all token-derived state is exposed via the service's signals.
 */
export interface DecodedToken {
  /** The subject claim — the authenticated user's username. */
  sub: string;
  /** Issued-at timestamp (seconds since epoch). */
  iat: number;
  /** Expiry timestamp (seconds since epoch). */
  exp: number;
  /** The user's granted authority, e.g. `ROLE_USER` or `ROLE_ADMIN`. */
  role: string;
  /** Whether a forced password rotation is pending for this user. */
  pwd_chg_req: boolean;
  /** The user's database UUID, used for client-side ownership checks (e.g. "is this my review?"). */
  userId: string;
}

/**
 * Central authentication service.
 * <p>
 * Owns the JWT lifecycle: persistence to `localStorage`, decoding, and exposure of
 * token-derived state as reactive signals. This is the single source of truth for
 * "who is the current user" — all identity/role/rotation questions are answered here
 * rather than by decoding the token elsewhere.
 * <p>
 * Runs in a fully zoneless, signal-driven manner: {@link isLoggedIn}, {@link userRole},
 * {@link userId}, and {@link isPasswordChangeRequired} are all `computed` off the raw
 * token signal, so consumers react automatically to login/logout.
 * <p>
 * Identity signals ({@link userRole}, {@link userId}, {@link isPasswordChangeRequired})
 * are gated on {@link isLoggedIn}: an expired-but-present token must not leak identity or
 * role, otherwise the UI could render actions (e.g. a delete button) for a logged-out user.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'bookshelf_jwt';
  private readonly authenticationAPIService = inject(AuthenticationAPIService);
  private readonly router = inject(Router);
  private readonly authToken = signal<string | null>(this.getStoredTokenSafe());

  /**
   * The decoded token payload, or `null` when there is no token, or it is malformed.
   * Recomputes automatically whenever {@link authToken} changes.
   */
  private readonly decodedToken = computed<DecodedToken | null>(() => {
    const token = this.authToken();
    if (!token) return null;
    return this.decodeToken(token);
  });

  /**
   * Whether the current user is authenticated with a non-expired token.
   * Compares the `exp` claim (seconds) against the current time (milliseconds).
   */
  public readonly isLoggedIn = computed(() => {
    const token = this.decodedToken();
    if (!token) return false;
    return token.exp * 1000 > Date.now();
  });

  /** The current user's authority (e.g. `ROLE_ADMIN`), or `null` when logged out or expired. */
  public readonly userRole = computed(() => {
    return this.isLoggedIn() ? (this.decodedToken()?.role ?? null) : null;
  });

  /**
   * The current user's database UUID, or `null` when logged out or expired.
   * Used for client-side ownership checks such as determining whether the current
   * user is the author of a given review.
   */
  public readonly userId = computed(() => {
    return this.isLoggedIn() ? (this.decodedToken()?.userId ?? null) : null;
  });

  /**
   * Whether the current user must change their password before proceeding.
   * Returns `false` when logged out/expired, or when the claim is absent (e.g. an older token),
   * mirroring the backend's null-safe handling of the same claim.
   */
  public readonly isPasswordChangeRequired = computed(() => {
    return this.isLoggedIn() ? (this.decodedToken()?.pwd_chg_req ?? false) : false;
  });

  /**
   * Authenticates a user and, on success, persists the returned JWT.
   * @param credentials The username and password.
   * @returns An observable emitting the authentication response (containing the JWT).
   */
  login(credentials: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.authenticationAPIService.authenticate(credentials).pipe(
      tap(response => this.handleAuthenticationResponse(response))
    );
  }

  /**
   * Registers a new user and, on success, persists the returned JWT.
   * @param request The registration payload.
   * @returns An observable emitting the authentication response (containing the JWT).
   */
  register(request: RegisterRequest): Observable<AuthenticationResponse> {
    return this.authenticationAPIService.register(request).pipe(
      tap(response => this.handleAuthenticationResponse(response))
    );
  }

  /**
   * Clears the persisted token, resets reactive state, and navigates to the login page.
   */
  logout(): void {
    this.removeStoredTokenSafe();
    this.authToken.set(null);
    void this.router.navigate(['/login']);
  }

  /**
   * @returns The current raw JWT string, or `null` when logged out.
   */
  getToken(): string | null {
    return this.authToken();
  }

  /**
   * Persists the JWT from an authentication response, if present.
   * Exposed publicly so it can be reused by flows that mint a fresh token
   * (e.g. a password change that returns a new JWT).
   * @param response The authentication response potentially carrying a token.
   */
  public handleAuthenticationResponse(response: AuthenticationResponse): void {
    if (response.token) {
      this.storeToken(response.token);
    }
  }

  /**
   * Persists a token to storage and updates the reactive token signal.
   * @param token The raw JWT to store.
   */
  private storeToken(token: string): void {
    this.setStoredTokenSafe(token);
    this.authToken.set(token);
  }

  // --- Safe Storage Wrappers ---
  // localStorage can throw (private browsing, disabled storage, quota). These wrappers
  // ensure the service degrades gracefully to a logged-out state rather than crashing.

  /**
   * Reads the token from storage, tolerating environments where access throws.
   * @returns The stored token, or `null` if absent or storage is unavailable.
   */
  private getStoredTokenSafe(): string | null {
    try {
      return localStorage.getItem(this.TOKEN_KEY);
    } catch {
      return null;
    }
  }

  /**
   * Writes the token to storage, silently ignoring failures.
   * @param token The raw JWT to persist.
   */
  private setStoredTokenSafe(token: string): void {
    try {
      localStorage.setItem(this.TOKEN_KEY, token);
    } catch {
      // Storage failure handled silently
    }
  }

  /**
   * Removes the token from storage, silently ignoring failures.
   */
  private removeStoredTokenSafe(): void {
    try {
      localStorage.removeItem(this.TOKEN_KEY);
    } catch {
      // Storage removal failure handled silently
    }
  }

  /**
   * Decodes the payload segment of a JWT into {@link DecodedToken}.
   * Performs base64url→base64 conversion and UTF-8 safe decoding. Returns `null`
   * on any malformed input rather than throwing, so downstream signals degrade safely.
   * @param token The raw JWT string.
   * @returns The decoded payload, or `null` if the token cannot be parsed.
   */
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
