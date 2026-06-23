import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-authenticated-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './authenticated-header.html',
  styleUrl: './authenticated-header.css'
})
export class AuthenticatedHeader {
  public authService = inject(AuthService);
  private router = inject(Router);

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
