import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenrePreview } from './genre-preview';

describe('GenrePreview', () => {
  let component: GenrePreview;
  let fixture: ComponentFixture<GenrePreview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GenrePreview],
    }).compileComponents();

    fixture = TestBed.createComponent(GenrePreview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
