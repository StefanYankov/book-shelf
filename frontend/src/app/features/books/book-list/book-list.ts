import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, startWith, switchMap } from 'rxjs/operators';
import { BookService } from '../../../core/services/book.service';
import { PageBookSummaryDto } from '../../../api';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css'
})
export class BookList {
  private bookService = inject(BookService);

  searchControl = new FormControl('');
  private books$ = this.searchControl.valueChanges.pipe(
    startWith(''),
    debounceTime(300),
    distinctUntilChanged(),
    switchMap(query => this.bookService.searchBooks(query || '', 0, 20))
  );

  books = toSignal(this.books$, {
    initialValue: { content: [], totalPages: 0, number: 0 } as PageBookSummaryDto
  });
}
