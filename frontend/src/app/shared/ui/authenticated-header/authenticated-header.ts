import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-authenticated-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './authenticated-header.html',
  styleUrl: './authenticated-header.css'
})
export class AuthenticatedHeader {
  private authService = inject(AuthService);
  private router = inject(Router);

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
