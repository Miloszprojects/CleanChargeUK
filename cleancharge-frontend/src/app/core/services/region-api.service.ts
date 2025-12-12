import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { GridRegion, RegionDashboardSummary } from '../models/region.model';
import { GRID_REGIONS } from '../models/region-config';

@Injectable({ providedIn: 'root' })
export class RegionApiService {
  constructor(private http: HttpClient) {}

  getRegions(): Observable<GridRegion[]> {
    return of(GRID_REGIONS);
  }

  getRegionSummary(ciRegionId: number): Observable<RegionDashboardSummary> {
    return this.http.get<RegionDashboardSummary>(`/api/regions/${ciRegionId}/summary`);
  }
}
