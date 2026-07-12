import {ComponentFixture, TestBed} from '@angular/core/testing';
import {AdminHome} from './admin-home';
import {provideRouter} from '@angular/router';
import {beforeEach, describe, expect, it} from 'vitest';

describe('AdminHome Component Tests', () => {
  let component: AdminHome;
  let fixture: ComponentFixture<AdminHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHome],
      providers: [
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component cleanly', () => {
    expect(component).toBeTruthy();
  });
});
