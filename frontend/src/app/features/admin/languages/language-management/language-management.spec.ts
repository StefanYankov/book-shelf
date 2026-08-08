import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {LanguageManagement} from './language-management';
import {AdminLanguageService} from '../../../../core/services/admin-language.service';
import {ToastService} from '../../../../core/services/toast.service';
import {LanguageDto, PagedResponseLanguageDto} from '../../../../api';

/**
 * Test-only view of LanguageManagement's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation without widening production visibility.
 */
interface LanguageManagementInternals {
  form: {
    setValue(value: { name: string }): void;
    getRawValue(): { name: string };
    invalid: boolean;
  };

  editingId(): string | null;

  formOpen(): boolean;

  confirmingDeleteId(): string | null;

  startCreate(): void;

  startEdit(language: LanguageDto): void;

  cancelForm(): void;

  submit(): void;

  requestDelete(id: string): void;

  cancelDelete(): void;

  confirmDelete(id: string): void;
}

describe('LanguageManagement Component Tests', () => {
  let component: LanguageManagement;
  let fixture: ComponentFixture<LanguageManagement>;
  let mockLanguageService: {
    getAllLanguages: ReturnType<typeof vi.fn>;
    createLanguage: ReturnType<typeof vi.fn>;
    updateLanguage: ReturnType<typeof vi.fn>;
    deleteLanguage: ReturnType<typeof vi.fn>;
  };
  let mockToast: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  const page: PagedResponseLanguageDto = {
    content: [{id: 'l1', name: 'English'}],
    totalElements: 1,
    totalPages: 1,
    pageNumber: 0,
    pageSize: 100,
    isLast: true,
  };

  /** Typed access to the component's protected members. */
  const internals = (): LanguageManagementInternals => component as unknown as LanguageManagementInternals;

  beforeEach(async () => {
    mockLanguageService = {
      getAllLanguages: vi.fn().mockReturnValue(of(page)),
      createLanguage: vi.fn().mockReturnValue(of({id: 'l2', name: 'Bulgarian'} as LanguageDto)),
      updateLanguage: vi.fn().mockReturnValue(of({id: 'l1', name: 'German'} as LanguageDto)),
      deleteLanguage: vi.fn().mockReturnValue(of(undefined)),
    };
    mockToast = {showSuccess: vi.fn(), showError: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [LanguageManagement],
      providers: [
        {provide: AdminLanguageService, useValue: mockLanguageService},
        {provide: ToastService, useValue: mockToast},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LanguageManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load the language list', () => {
    // Assert
    expect(component).toBeTruthy();
    expect(mockLanguageService.getAllLanguages).toHaveBeenCalled();
  });

  it('startCreate opens the form in create mode', () => {
    // Act
    internals().startCreate();

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBeNull();
  });

  it('startEdit opens the form in edit mode and pre-fills it', () => {
    // Act
    internals().startEdit({id: 'l1', name: 'English'});

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBe('l1');
    expect(internals().form.getRawValue()).toEqual({name: 'English'});
  });

  it('cancelForm closes and clears the form', () => {
    // Arrange
    internals().startEdit({id: 'l1', name: 'English'});

    // Act
    internals().cancelForm();

    // Assert
    expect(internals().formOpen()).toBe(false);
    expect(internals().editingId()).toBeNull();
  });

  it('submit creates a language when in create mode and refreshes the list', () => {
    // Arrange
    internals().startCreate();
    internals().form.setValue({name: 'Bulgarian'});

    // Act
    internals().submit();

    // Assert
    expect(mockLanguageService.createLanguage).toHaveBeenCalledWith({name: 'Bulgarian'});
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Language created.');
    expect(mockLanguageService.getAllLanguages).toHaveBeenCalledTimes(2);
    expect(internals().formOpen()).toBe(false);
  });

  it('submit updates a language when in edit mode', () => {
    // Arrange
    internals().startEdit({id: 'l1', name: 'English'});
    internals().form.setValue({name: 'German'});

    // Act
    internals().submit();

    // Assert
    expect(mockLanguageService.updateLanguage).toHaveBeenCalledWith('l1', {name: 'German'});
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Language updated.');
    expect(mockLanguageService.getAllLanguages).toHaveBeenCalledTimes(2);
  });

  it('submit does nothing when the form is invalid', () => {
    // Arrange
    internals().startCreate();
    internals().form.setValue({name: ''});

    // Act
    internals().submit();

    // Assert
    expect(mockLanguageService.createLanguage).not.toHaveBeenCalled();
    expect(mockLanguageService.updateLanguage).not.toHaveBeenCalled();
  });

  it('submit surfaces an error toast on failure', () => {
    // Arrange
    mockLanguageService.createLanguage.mockReturnValue(throwError(() => ({error: {detail: 'Duplicate'}})));
    internals().startCreate();
    internals().form.setValue({name: 'English'});

    // Act
    internals().submit();

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('Duplicate');
  });

  it('requestDelete then confirmDelete deletes and refreshes', () => {
    // Act
    internals().requestDelete('l1');

    // Assert intermediate state
    expect(internals().confirmingDeleteId()).toBe('l1');

    // Act
    internals().confirmDelete('l1');

    // Assert
    expect(mockLanguageService.deleteLanguage).toHaveBeenCalledWith('l1');
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Language deleted.');
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockLanguageService.getAllLanguages).toHaveBeenCalledTimes(2);
  });

  it('confirmDelete surfaces an error toast on failure', () => {
    // Arrange
    mockLanguageService.deleteLanguage.mockReturnValue(throwError(() => ({error: {detail: 'In use'}})));

    // Act
    internals().requestDelete('l1');
    internals().confirmDelete('l1');

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('In use');
  });

  it('cancelDelete clears the pending confirmation', () => {
    // Act
    internals().requestDelete('l1');
    internals().cancelDelete();

    // Assert
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockLanguageService.deleteLanguage).not.toHaveBeenCalled();
  });
});
