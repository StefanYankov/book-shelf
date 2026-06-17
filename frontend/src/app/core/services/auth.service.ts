import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

// Correct: Import the generated service and models
import { AuthenticationAPIService } from '../../api';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../../api';

export interface DecodedToken {
  sub: string; // The username
  iat: number; // Issued at timestamp
  exp: number; // Expiration timestamp
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'bookshelf_jwt';

  private authenticationAPIService = inject(AuthenticationAPIService);
  private router = inject(Router);

  /**
   * Sends login credentials to the backend and stores the JWT upon success.
   * @param credentials The user's username and password.
   * @returns An Observable of the login response.
   */
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

  /**
   * Removes the JWT from storage and navigates the user to the login page.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  /**
   * Retrieves the raw JWT from local storage.
   * @returns The token string or null if not present.
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Decodes the payload section of the stored JWT.
   * A JWT has 3 parts separated by dots: Header.Payload.Signature.
   * This method extracts the Payload (index 1), decodes the Base64Url string, and parses the JSON.
   *
   * @returns The DecodedToken object containing claims (like userId and role), or null if invalid.
   */
  getDecodedToken(): DecodedToken | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const payloadBase64 = token.split('.')[1];
      const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));

      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Error decoding JWT token', e);
      return null;
    }
  }

  /**
   * Checks if a user is currently authenticated by verifying the presence of a token.
   * @returns True if a token exists, false otherwise.
   */
  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    const decoded = this.getDecodedToken();
    if (!decoded) {
      return false;
    }
    // Check if the token is expired
    return decoded.exp * 1000 > Date.now();
  }

  /**
   * Stores the JWT in local storage.
   * @param token The token string to store.
   */
  private storeToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }
}
