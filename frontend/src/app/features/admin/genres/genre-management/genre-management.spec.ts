import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {GenreManagement} from './genre-management';
import {GenreDto, PagedResponseGenreDto} from '../../../../api';
import {ToastService} from '../../../../core/services/toast.service';
import {AdminGenreService} from "../../../../core/services/admin-genre.service";


/**
 * Test-only view of GenreManagement's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation without widening production visibility.
 */
interface GenreManagementInternals {
  form: {
    setValue(value: { name: string; description: string }): void;
    getRawValue(): { name: string; description: string };
    invalid: boolean;
  };

  editingId(): string | null;

  formOpen(): boolean;

  confirmingDeleteId(): string | null;

  startCreate(): void;

  startEdit(genre: GenreDto): void;

  cancelForm(): void;

  submit(): void;

  requestDelete(id: string): void;

  cancelDelete(): void;

  confirmDelete(id: string): void;
}

describe('GenreManagement Component Tests', () => {
  let component: GenreManagement;
  let fixture: ComponentFixture<GenreManagement>;
  let mockGenreService: {
    getAllGenres: ReturnType<typeof vi.fn>;
    createGenre: ReturnType<typeof vi.fn>;
    updateGenre: ReturnType<typeof vi.fn>;
    deleteGenre: ReturnType<typeof vi.fn>;
  };
  let mockToast: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  const page: PagedResponseGenreDto = {
    content: [{id: 'g1', name: 'Fantasy', description: 'Magic.'}],
    totalElements: 1,
    totalPages: 1,
    pageNumber: 0,
    pageSize: 100,
    isLast: true,
  };

  /** Typed access to the component's protected members. */
  const internals = (): GenreManagementInternals => component as unknown as GenreManagementInternals;

  beforeEach(async () => {
    mockGenreService = {
      getAllGenres: vi.fn().mockReturnValue(of(page)),
      createGenre: vi.fn().mockReturnValue(of({id: 'g2', name: 'Sci-Fi'} as GenreDto)),
      updateGenre: vi.fn().mockReturnValue(of({id: 'g1', name: 'Renamed'} as GenreDto)),
      deleteGenre: vi.fn().mockReturnValue(of(undefined)),
    };
    mockToast = {showSuccess: vi.fn(), showError: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [GenreManagement],
      providers: [
        {provide: AdminGenreService, useValue: mockGenreService},
        {provide: ToastService, useValue: mockToast},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GenreManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load the genre list', () => {
    // Arrange & Act done in beforeEach

    // Assert
    expect(component).toBeTruthy();
    expect(mockGenreService.getAllGenres).toHaveBeenCalled();
  });

  it('startCreate opens the form in create mode', () => {
    // Act
    internals().startCreate();

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBeNull();
  });
});
