import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import { GRID_REGIONS } from '../../core/models/region-config';

@Component({
  selector: 'app-region-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `<div data-testid="region-dashboard-mock"></div>`,
})
class RegionDashboardComponentMock {}

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let component: DashboardComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
    })
      .overrideComponent(DashboardComponent, {
        set: {
          imports: [CommonModule, RegionDashboardComponentMock],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose regions from GRID_REGIONS', () => {
    expect(component.regions).toEqual(GRID_REGIONS);
  });
});
