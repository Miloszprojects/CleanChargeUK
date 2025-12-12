import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UkRegionMap } from './uk-region-map';

describe('UkRegionMap', () => {
  let component: UkRegionMap;
  let fixture: ComponentFixture<UkRegionMap>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UkRegionMap]
    })
      .compileComponents();

    fixture = TestBed.createComponent(UkRegionMap);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
