import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {PublisherManagement} from './publisher-management';
import {AdminPublisherService} from '../../../../core/services/admin-publisher.service';
import {ToastService} from '../../../../core/services/toast.service';
import {PagedResponsePublisherDto, PublisherDto} from '../../../../api';

/**
 * Test-only view of PublisherManagement's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation without widening production visibility.
 */
interface PublisherManagementInternals {
  form: {
    setValue(value: { name: string }): void;
    getRawValue(): { name: string };
    invalid: boolean;
  };

  editingId(): string | null;

  formOpen(): boolean;

  confirmingDeleteId(): string | null;

  startCreate(): void;

  startEdit(publisher: PublisherDto): void;

  cancelForm(): void;

  submit(): void;

  requestDelete(id: string): void;

  cancelDelete(): void;

  confirmDelete(id: string): void;
}

describe('PublisherManagement Component Tests', () => {
  let component: PublisherManagement;
  let fixture: ComponentFixture<PublisherManagement>;
  let mockPublisherService: {
    getAllPublishers: ReturnType<typeof vi.fn>;
    createPublisher: ReturnType<typeof vi.fn>;
    updatePublisher: ReturnType<typeof vi.fn>;
    deletePublisher: ReturnType<typeof vi.fn>;
  };
  let mockToast: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  const page: PagedResponsePublisherDto = {
    content: [{id: 'p1', name: 'Penguin Books'}],
    totalElements: 1,
    totalPages: 1,
    pageNumber: 0,
    pageSize: 100,
    isLast: true,
  };

  /** Typed access to the component's protected members. */
  const internals = (): PublisherManagementInternals => component as unknown as PublisherManagementInternals;

  beforeEach(async () => {
    mockPublisherService = {
      getAllPublishers: vi.fn().mockReturnValue(of(page)),
      createPublisher: vi.fn().mockReturnValue(of({id: 'p2', name: 'Doubleday'} as PublisherDto)),
      updatePublisher: vi.fn().mockReturnValue(of({id: 'p1', name: 'Random House'} as PublisherDto)),
      deletePublisher: vi.fn().mockReturnValue(of(undefined)),
    };
    mockToast = {showSuccess: vi.fn(), showError: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [PublisherManagement],
      providers: [
        {provide: AdminPublisherService, useValue: mockPublisherService},
        {provide: ToastService, useValue: mockToast},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PublisherManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load the publisher list', () => {
    // Assert
    expect(component).toBeTruthy();
    expect(mockPublisherService.getAllPublishers).toHaveBeenCalled();
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
    internals().startEdit({id: 'p1', name: 'Penguin Books'});

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBe('p1');
    expect(internals().form.getRawValue()).toEqual({name: 'Penguin Books'});
  });

  it('cancelForm closes and clears the form', () => {
    // Arrange
    internals().startEdit({id: 'p1', name: 'Penguin Books'});

    // Act
    internals().cancelForm();

    // Assert
    expect(internals().formOpen()).toBe(false);
    expect(internals().editingId()).toBeNull();
  });

  it('submit creates a publisher when in create mode and refreshes the list', () => {
    // Arrange
    internals().startCreate();
    internals().form.setValue({name: 'Doubleday'});

    // Act
    internals().submit();

    // Assert
    expect(mockPublisherService.createPublisher).toHaveBeenCalledWith({name: 'Doubleday'});
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Publisher created.');
    expect(mockPublisherService.getAllPublishers).toHaveBeenCalledTimes(2);
    expect(internals().formOpen()).toBe(false);
  });

  it('submit updates a publisher when in edit mode', () => {
    // Arrange
    internals().startEdit({id: 'p1', name: 'Penguin Books'});
    internals().form.setValue({name: 'Random House'});

    // Act
    internals().submit();

    // Assert
    expect(mockPublisherService.updatePublisher).toHaveBeenCalledWith('p1', {name: 'Random House'});
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Publisher updated.');
    expect(mockPublisherService.getAllPublishers).toHaveBeenCalledTimes(2);
  });

  it('submit does nothing when the form is invalid', () => {
    // Arrange
    internals().startCreate();
    internals().form.setValue({name: ''});

    // Act
    internals().submit();

    // Assert
    expect(mockPublisherService.createPublisher).not.toHaveBeenCalled();
    expect(mockPublisherService.updatePublisher).not.toHaveBeenCalled();
  });

  it('submit surfaces an error toast on failure', () => {
    // Arrange
    mockPublisherService.createPublisher.mockReturnValue(throwError(() => ({error: {detail: 'Duplicate'}})));
    internals().startCreate();
    internals().form.setValue({name: 'Penguin Books'});

    // Act
    internals().submit();

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('Duplicate');
  });

  it('requestDelete then confirmDelete deletes and refreshes', () => {
    // Act
    internals().requestDelete('p1');

    // Assert intermediate state
    expect(internals().confirmingDeleteId()).toBe('p1');

    // Act
    internals().confirmDelete('p1');

    // Assert
    expect(mockPublisherService.deletePublisher).toHaveBeenCalledWith('p1');
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Publisher deleted.');
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockPublisherService.getAllPublishers).toHaveBeenCalledTimes(2);
  });

  it('confirmDelete surfaces an error toast on failure', () => {
    // Arrange
    mockPublisherService.deletePublisher.mockReturnValue(throwError(() => ({error: {detail: 'In use'}})));

    // Act
    internals().requestDelete('p1');
    internals().confirmDelete('p1');

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('In use');
  });

  it('cancelDelete clears the pending confirmation', () => {
    // Act
    internals().requestDelete('p1');
    internals().cancelDelete();

    // Assert
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockPublisherService.deletePublisher).not.toHaveBeenCalled();
  });
});
