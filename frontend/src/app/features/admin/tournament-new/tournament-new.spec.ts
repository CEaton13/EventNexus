import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TournamentNew } from './tournament-new';

describe('TournamentNew', () => {
  let component: TournamentNew;
  let fixture: ComponentFixture<TournamentNew>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TournamentNew],
    }).compileComponents();

    fixture = TestBed.createComponent(TournamentNew);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
