import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RegionDashboardComponent } from './region-dashboard/region-dashboard.component';
import { GRID_REGIONS } from '../../core/models/region-config';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RegionDashboardComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent {
  readonly regions = GRID_REGIONS;
}
