import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {AuthorManagement} from './author-management';
import {AdminAuthorService} from '../../../../core/services/admin-author.service';
import {ToastService} from '../../../../core/services/toast.service';
import {AuthorDetailsDto, AuthorSummaryDto, PagedResponseAuthorSummaryDto} from '../../../../api';

/**
 * Test-only view of AuthorManagement's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation without widening production visibility.
 */
interface AuthorManagementInternals {
  form: {
    setValue(value: { name: string; summary: string }): void;
    getRawValue(): { name: string; summary: string };
    invalid: boolean;
  };

  editingId(): string | null;

  formOpen(): boolean;

  confirmingDeleteId(): string | null;

  imageInputVisible(): boolean;

  selectedImage(): File | null;

  startCreate(): void;

  startEdit(author: AuthorSummaryDto): void;

  onImageSelected(event: Event): void;

  cancelForm(): void;

  submit(): void;

  requestDelete(id: string): void;

  cancelDelete(): void;

  confirmDelete(id: string): void;
}

describe('AuthorManagement Component Tests', () => {
  let component: AuthorManagement;
  let fixture: ComponentFixture<AuthorManagement>;
  let mockAuthorService: {
    getAllAuthors: ReturnType<typeof vi.fn>;
    createAuthor: ReturnType<typeof vi.fn>;
    updateAuthor: ReturnType<typeof vi.fn>;
    deleteAuthor: ReturnType<typeof vi.fn>;
  };
  let mockToast: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  const page: PagedResponseAuthorSummaryDto = {
    content: [{id: 'a1', name: 'Tolkien', imageUrl: 'https://cdn/a.jpg'}],
    totalElements: 1,
    totalPages: 1,
    pageNumber: 0,
    pageSize: 100,
    isLast: true,
  };

  /** Typed access to the component's protected members. */
  const internals = (): AuthorManagementInternals => component as unknown as AuthorManagementInternals;

  beforeEach(async () => {
    mockAuthorService = {
      getAllAuthors: vi.fn().mockReturnValue(of(page)),
      createAuthor: vi.fn().mockReturnValue(of({id: 'a2', name: 'Asimov'} as AuthorDetailsDto)),
      updateAuthor: vi.fn().mockReturnValue(of({id: 'a1', name: 'Tolkien'} as AuthorDetailsDto)),
      deleteAuthor: vi.fn().mockReturnValue(of(undefined)),
    };
    mockToast = {showSuccess: vi.fn(), showError: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [AuthorManagement],
      providers: [
        {provide: AdminAuthorService, useValue: mockAuthorService},
        {provide: ToastService, useValue: mockToast},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AuthorManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load the author list', () => {
    // Assert
    expect(component).toBeTruthy();
    expect(mockAuthorService.getAllAuthors).toHaveBeenCalled();
  });

  it('startCreate opens the form in create mode with the image input visible', () => {
    // Act
    internals().startCreate();

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBeNull();
    expect(internals().imageInputVisible()).toBe(true);
  });

  it('startEdit opens the form in edit mode, pre-fills it, and hides the image input', () => {
    // Act
    internals().startEdit({id: 'a1', name: 'Tolkien', imageUrl: 'https://cdn/a.jpg'});

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBe('a1');
    expect(internals().imageInputVisible()).toBe(false);
    expect(internals().form.getRawValue()).toEqual({name: 'Tolkien', summary: ''});
  });

  it('onImageSelected records the chosen file', () => {
    // Arrange
    const file = new File(['x'], 'a.jpg', {type: 'image/jpeg'});
    const event = {target: {files: [file]}} as unknown as Event;

    // Act
    internals().onImageSelected(event);

    // Assert
    expect(internals().selectedImage()).toBe(file);
  });

  it('submit creates an author with the selected image and refreshes', () => {
    // Arrange
    const file = new File(['x'], 'a.jpg', {type: 'image/jpeg'});
    internals().startCreate();
    internals().form.setValue({name: 'Asimov', summary: 'Foundation.'});
    internals().onImageSelected({target: {files: [file]}} as unknown as Event);

    // Act
    internals().submit();

    // Assert
    expect(mockAuthorService.createAuthor).toHaveBeenCalledWith({name: 'Asimov', summary: 'Foundation.'}, file);
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Author created.');
    expect(mockAuthorService.getAllAuthors).toHaveBeenCalledTimes(2);
    expect(internals().formOpen()).toBe(false);
  });

  it('submit creates an author without an image when none is chosen', () => {
    // Arrange
    internals().startCreate();
    internals().form.setValue({name: 'Asimov', summary: ''});

    // Act
    internals().submit();

    // Assert
    expect(mockAuthorService.createAuthor).toHaveBeenCalledWith({name: 'Asimov', summary: ''}, undefined);
  });

  it('submit updates an author when in edit mode', () => {
    // Arrange
    internals().startEdit({id: 'a1', name: 'Tolkien', imageUrl: 'https://cdn/a.jpg'});
    internals().form.setValue({name: 'Tolkien', summary: 'New bio.'});

    // Act
    internals().submit();

    // Assert
    expect(mockAuthorService.updateAuthor).toHaveBeenCalledWith('a1', {name: 'Tolkien', summary: 'New bio.'});
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Author updated.');
  });

  it('submit does nothing when the form is invalid', () => {
    // Arrange
    internals().startCreate();
    internals().form.setValue({name: '', summary: ''});

    // Act
    internals().submit();

    // Assert
    expect(mockAuthorService.createAuthor).not.toHaveBeenCalled();
    expect(mockAuthorService.updateAuthor).not.toHaveBeenCalled();
  });

  it('submit surfaces an error toast on failure', () => {
    // Arrange
    mockAuthorService.createAuthor.mockReturnValue(throwError(() => ({error: {detail: 'Duplicate'}})));
    internals().startCreate();
    internals().form.setValue({name: 'Tolkien', summary: ''});

    // Act
    internals().submit();

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('Duplicate');
  });

  it('requestDelete then confirmDelete deletes and refreshes', () => {
    // Act
    internals().requestDelete('a1');

    // Assert intermediate state
    expect(internals().confirmingDeleteId()).toBe('a1');

    // Act
    internals().confirmDelete('a1');

    // Assert
    expect(mockAuthorService.deleteAuthor).toHaveBeenCalledWith('a1');
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Author deleted.');
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockAuthorService.getAllAuthors).toHaveBeenCalledTimes(2);
  });

  it('confirmDelete surfaces an error toast on failure', () => {
    // Arrange
    mockAuthorService.deleteAuthor.mockReturnValue(throwError(() => ({error: {detail: 'In use'}})));

    // Act
    internals().requestDelete('a1');
    internals().confirmDelete('a1');

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('In use');
  });

  it('cancelDelete clears the pending confirmation', () => {
    // Act
    internals().requestDelete('a1');
    internals().cancelDelete();

    // Assert
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockAuthorService.deleteAuthor).not.toHaveBeenCalled();
  });
});
