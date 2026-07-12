import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-header',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './admin-header.html'
})
export class AdminHeader {
  public readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /**
   * Tracks if the administrator's system layout navigation features should be locked.
   */
  protected isNavigationRestricted = computed(() => this.authService.isPasswordChangeRequired());

  /**
   * Destroys the active administrative context session stream and drops back to the login terminal.
   */
  public logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
