import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthenticatedHeader } from './authenticated-header';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { signal, WritableSignal } from '@angular/core';
import { By } from '@angular/platform-browser';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { provideRouter } from '@angular/router';

describe('AuthenticatedHeader Component Tests', () => {
  let fixture: ComponentFixture<AuthenticatedHeader>;
  let router: Router;
  let mockAuthService: {
    userRole: WritableSignal<string | null>;
    logout: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockAuthService = {
      userRole: signal<string | null>(null),
      logout: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [AuthenticatedHeader],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    fixture = TestBed.createComponent(AuthenticatedHeader);
    router = TestBed.inject(Router);

    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
  });

  describe('Conditional Admin Route Authorization', () => {
    it('should hide the Admin link for users possessing standard ROLE_USER authority', () => {
      // Arrange
      mockAuthService.userRole.set('ROLE_USER');

      // Act
      fixture.detectChanges();

      // Assert
      const adminLink = fixture.debugElement.query(By.css('a[routerLink="/app/admin/users"]'));
      expect(adminLink).toBeNull();
    });

    it('should display the Admin link for users possessing administrative ROLE_ADMIN authority', () => {
      // Arrange
      mockAuthService.userRole.set('ROLE_ADMIN');

      // Act
      fixture.detectChanges();

      // Assert
      const adminLink = fixture.debugElement.query(By.css('a[routerLink="/app/admin/users"]'));
      expect(adminLink).not.toBeNull();
      expect(adminLink.nativeElement.textContent).toContain('Admin');
    });
  });

  describe('Logout Interactions', () => {
    it('should invoke AuthService logout logic and redirect to login state on click', () => {
      // Arrange
      fixture.detectChanges();
      const logoutButton = fixture.debugElement.query(By.css('button.nav-link'));

      // Act
      logoutButton.nativeElement.click();

      // Assert
      expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });
  });
});
