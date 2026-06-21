import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { MyShelves } from './my-shelves';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('MyShelves', () => {
  let component: MyShelves;
  let fixture: ComponentFixture<MyShelves>;
  let mockBookshelfService: { getShelvesForUser: ReturnType<typeof vi.fn>; createShelf: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockBookshelfService = {
      getShelvesForUser: vi.fn().mockReturnValue(of({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 20 })),
      createShelf: vi.fn()
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [MyShelves],
      providers: [
        provideRouter([]),
        { provide: BookshelfService, useValue: mockBookshelfService },
        { provide: ToastService, useValue: mockToastService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyShelves);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('onSubmit', () => {
    it('should show success toast and refresh list on successful creation', () => {
      // Arrange
      mockBookshelfService.createShelf.mockReturnValue(of({ id: '1', name: 'New Shelf' }));
      component.createForm.setValue({ name: 'New Shelf', description: '' });

      // Act
      component.onSubmit();

      // Assert
      expect(mockBookshelfService.createShelf).toHaveBeenCalled();
      expect(mockToastService.showSuccess).toHaveBeenCalledWith('Shelf created successfully!');
      expect(mockBookshelfService.getShelvesForUser).toHaveBeenCalledTimes(2); // Initial + refresh
    });

    it('should show error toast on failed creation', () => {
      // Arrange
      const errorResponse = { error: { detail: 'Failed to create' } };
      mockBookshelfService.createShelf.mockReturnValue(throwError(() => errorResponse));
      component.createForm.setValue({ name: 'New Shelf', description: '' });

      // Act
      component.onSubmit();

      // Assert
      expect(mockBookshelfService.createShelf).toHaveBeenCalled();
      expect(mockToastService.showError).toHaveBeenCalledWith('Failed to create');
    });

    it('should not submit if form is invalid', () => {
      // Arrange
      component.createForm.setValue({ name: '', description: '' }); // Invalid name

      // Act
      component.onSubmit();

      // Assert
      expect(mockBookshelfService.createShelf).not.toHaveBeenCalled();
    });
  });
});
