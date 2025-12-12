import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { RegionDashboardComponent } from './region-dashboard.component';
import { RegionApiService } from '../../../core/services/region-api.service';

import {
  ChargingWindowOption,
  DailyEnergyMix,
  FuelShare,
  RegionDashboardSummary,
} from '../../../core/models/region.model';

describe('RegionDashboardComponent', () => {
  const fallbackMix: FuelShare[] = [
    { fuel: 'wind', percentage: 70 },
    { fuel: 'gas', percentage: 30 },
  ];

  const windows: ChargingWindowOption[] = [
    { hours: 1, start: 'a', end: 'b', averageCleanEnergyPercentage: 10 },
    { hours: 3, start: 'a', end: 'b', averageCleanEnergyPercentage: 55 },
    { hours: 6, start: 'a', end: 'b', averageCleanEnergyPercentage: 80 },
  ];

  const dailyMixes: DailyEnergyMix[] = [
    { date: '2025-01-01', cleanPercentage: 70, mix: fallbackMix },
    { date: '2025-01-02', cleanPercentage: 20, mix: fallbackMix },
    { date: '2025-01-03', cleanPercentage: 50, mix: fallbackMix },
  ];

  const apiMock = {
    getRegions: vi.fn(),
    getRegionSummary: vi.fn(),
  };

  beforeEach(async () => {
    vi.resetAllMocks();

    await TestBed.configureTestingModule({
      imports: [RegionDashboardComponent],
      providers: [{ provide: RegionApiService, useValue: apiMock }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  function create() {
    const fixture = TestBed.createComponent(RegionDashboardComponent);
    return {
      component: fixture.componentInstance,
      api: TestBed.inject(RegionApiService) as any,
    };
  }

  it('onRegionSelected: loads data for REAL svgId', () => {
    const { component, api } = create();

    api.getRegionSummary.mockReturnValue(
      of({
        regionId: 7,
        name: 'London',
        currentMix: fallbackMix,
        chargingWindows: windows,
        dailyMixes,
      } satisfies RegionDashboardSummary),
    );

    component.onRegionSelected(7);

    expect(api.getRegionSummary).toHaveBeenCalledWith(13);
    expect(component.selectedRegionName).toBe('London');
    expect(component.pieCharts.length).toBe(3);
    expect(component.barChartData.datasets[0].data).toEqual([10, 55, 80]);
  });

  it('does nothing when svgId not found', () => {
    const { component, api } = create();

    component.onRegionSelected(999);

    expect(api.getRegionSummary).not.toHaveBeenCalled();
  });

  it('fetch error sets error flag', () => {
    const { component, api } = create();

    api.getRegionSummary.mockReturnValue(
      throwError(() => new Error('boom')),
    );

    component.onRegionSelected(7);

    expect(component.error).toBe('Nie udało się pobrać danych dla regionu');
    expect(component.isLoading).toBe(false);
  });

  it('onHoursChange selects nearest window', () => {
    const { component } = create();

    component.chargingWindows = windows;
    component.selectedHours = 4;

    component.onHoursChange({ target: { value: '4' } } as any);

    expect(component.selectedWindow?.hours).toBe(3);
  });
});
