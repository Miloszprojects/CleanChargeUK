import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';

import { RegionApiService } from './region-api.service';
import { GRID_REGIONS } from '../models/region-config';
import type { RegionDashboardSummary } from '../models/region.model';

describe('RegionApiService', () => {
  let service: RegionApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RegionApiService],
    });

    service = TestBed.inject(RegionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getRegions(): should return GRID_REGIONS', async () => {
    const regions = await firstValueFrom(service.getRegions());
    expect(regions).toEqual(GRID_REGIONS);
    httpMock.expectNone(() => true);
  });

  it('getRegionSummary(): should GET correct URL', () => {
    const ciRegionId = 13;

    const mockSummary: RegionDashboardSummary = {
      regionId: 7,
      name: 'London',
      currentMix: [{ fuel: 'wind', percentage: 70 }],
      chargingWindows: [],
      dailyMixes: [],
    };

    service.getRegionSummary(ciRegionId).subscribe((res) => {
      expect(res).toEqual(mockSummary);
    });

    const req = httpMock.expectOne(`/api/regions/${ciRegionId}/summary`);
    expect(req.request.method).toBe('GET');

    req.flush(mockSummary);
  });
});
