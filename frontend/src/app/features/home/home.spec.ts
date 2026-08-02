import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {beforeEach, describe, expect, it} from 'vitest';
import {Home} from './home';

describe('Home Component Tests', () => {
  let fixture: ComponentFixture<Home>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render a reading challenge card linking to the challenge page', () => {
    // Assert
    const link = fixture.nativeElement.querySelector('a[href="/app/challenges"]');
    expect(link).not.toBeNull();
    expect(link.textContent).toContain('Reading Challenge');
  });

  it('should show the current year on the challenge prompt', () => {
    // Assert
    const year = new Date().getFullYear().toString();
    expect(fixture.nativeElement.textContent).toContain(year);
  });
});
