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
  let component: AuthenticatedHeader;
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
    component = fixture.componentInstance;
    router = TestBed.inject(Router);

    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
  });

  describe('Conditional Admin Route Authorization', () => {
    it('should have isAdmin return false for standard ROLE_USER', () => {
      mockAuthService.userRole.set('ROLE_USER');
      fixture.detectChanges();

      expect(component['isAdmin']()).toBe(false);
    });

    it('should have isAdmin return true for ROLE_ADMIN', () => {
      mockAuthService.userRole.set('ROLE_ADMIN');
      fixture.detectChanges();

      expect(component['isAdmin']()).toBe(true);
    });
  });

  describe('Logout Interactions', () => {
    it('should invoke AuthService logout logic on click', () => {
      fixture.detectChanges();
      const logoutButton = fixture.debugElement.query(By.css('button.nav-link'));

      logoutButton.nativeElement.click();

      expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
    });
  });
});
