import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

import { RegionApiService } from '../../../core/services/region-api.service';
import {
  GridRegion,
  RegionDashboardSummary,
  ChargingWindowOption,
  DailyEnergyMix,
  FuelShare,
} from '../../../core/models/region.model';

import { GRID_REGIONS, getRegionBySvgId } from '../../../core/models/region-config';
import { UkRegionMap } from '../components/uk-regions-map/uk-region-map';

type PieCardVm = {
  title: string;
  date?: string;
  cleanPercentage?: number;
  data: ChartData<'pie', number[], string | string[]>;
};

@Component({
  selector: 'app-region-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, UkRegionMap],
  templateUrl: './region-dashboard.component.html',
  styleUrls: ['./region-dashboard.component.css'],
})
export class RegionDashboardComponent implements OnInit {
  regions: GridRegion[] = [];
  selectedRegionId: number | null = null;
  selectedRegionName = '';
  isLoading = false;
  error: string | null = null;
  pieCharts: PieCardVm[] = [
    { title: 'Today', data: { labels: [], datasets: [] } },
    { title: 'Tommorow', data: { labels: [], datasets: [] } },
    { title: 'In 2 days', data: { labels: [], datasets: [] } },
  ];

  pieChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    plugins: { legend: { display: false } },
  };

  barChartData: ChartData<'bar'> = { labels: [], datasets: [] };

  barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    layout: {
      padding: { top: 6, right: 10, bottom: 0, left: 6 },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: {
          color: 'rgba(229, 249, 255, 0.85)',
          font: { size: 12, weight: 500 },
          padding: 6,
        },
      },
      y: {
        beginAtZero: true,
        max: 100,
        suggestedMax: 100,
        grid: { color: 'rgba(148, 163, 184, 0.18)' },
        ticks: {
          color: 'rgba(229, 249, 255, 0.65)',
          font: { size: 11 },
          padding: 6,
          stepSize: 20,
          callback: (value) => `${value}%`,
        },
      },
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const y = ctx.parsed?.y;
            if (y === null || y === undefined) return '';
            return `${Number(y).toFixed(1)}% clean`;
          },
        },
      },
    },
  };

  chargingWindows: ChargingWindowOption[] = [];
  selectedHours = 3;
  selectedWindow: ChargingWindowOption | null = null;

  private lastLoadedRegionCiId: number | null = null;

  constructor(private regionApi: RegionApiService) {}

  ngOnInit(): void {
    this.regionApi.getRegions().subscribe((regs) => {
      this.regions = regs;
    });
  }

  onRegionSelected(svgId: number): void {
    this.selectedRegionId = svgId;
    this.loadRegionBySvgId(svgId);
  }
  onRegionHovered(svgId: number | null): void {
    if (svgId == null) return;
  }

  private loadRegionBySvgId(svgId: number): void {
    const region = getRegionBySvgId(svgId);
    if (!region) return;

    if (region.ciRegionId === this.lastLoadedRegionCiId) return;

    this.fetchRegionSummary(region);
  }

  private fetchRegionSummary(region: GridRegion): void {
    const ciId = region.ciRegionId;

    this.isLoading = true;
    this.error = null;

    this.regionApi.getRegionSummary(ciId).subscribe({
      next: (summary: RegionDashboardSummary) => {
        this.lastLoadedRegionCiId = ciId;
        this.selectedRegionName = summary.name || region.shortName;
        this.bindDailyMixes(summary.dailyMixes ?? [], summary.currentMix ?? []);
        this.chargingWindows = summary.chargingWindows ?? [];
        this.updateBarChart();
        this.updateSelectedWindow();
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Nie udało się pobrać danych dla regionu';
        this.isLoading = false;
      },
    });
  }

  private bindDailyMixes(dailyMixes: DailyEnergyMix[], fallbackCurrentMix: FuelShare[]): void {
    const normalized = [...dailyMixes].slice(0, 3);

    while (normalized.length < 3) {
      normalized.push({ date: '', cleanPercentage: 0, mix: [] });
    }

    const toPieData = (mix: FuelShare[]): ChartData<'pie', number[], string | string[]> => ({
      labels: mix.map((m) => m.fuel),
      datasets: [{ data: mix.map((m) => m.percentage) }],
    });

    this.pieCharts = [
      {
        title: 'Today',
        date: normalized[0].date || undefined,
        cleanPercentage: normalized[0].cleanPercentage ?? undefined,
        data: normalized[0].mix?.length ? toPieData(normalized[0].mix) : toPieData(fallbackCurrentMix),
      },
      {
        title: 'Tommorow',
        date: normalized[1].date || undefined,
        cleanPercentage: normalized[1].cleanPercentage ?? undefined,
        data: normalized[1].mix?.length ? toPieData(normalized[1].mix) : toPieData(fallbackCurrentMix),
      },
      {
        title: 'In 2 days',
        date: normalized[2].date || undefined,
        cleanPercentage: normalized[2].cleanPercentage ?? undefined,
        data: normalized[2].mix?.length ? toPieData(normalized[2].mix) : toPieData(fallbackCurrentMix),
      },
    ];
  }

  get pieLegendLabels(): string[] {
    for (const p of this.pieCharts) {
      const labels = p.data.labels as string[] | undefined;
      if (labels?.length) return labels;
    }
    return [];
  }

  private updateBarChart(): void {
    if (!this.chargingWindows.length) {
      this.barChartData = { labels: [], datasets: [] };
      return;
    }

    this.barChartData = {
      labels: this.chargingWindows.map((w) => `${w.hours} h`),
      datasets: [
        {
          data: this.chargingWindows.map((w) => w.averageCleanEnergyPercentage),
          barThickness: 46,
          maxBarThickness: 52,
          borderRadius: 10,
        },
      ],
    };
  }

  private updateSelectedWindow(): void {
    if (!this.chargingWindows.length) {
      this.selectedWindow = null;
      return;
    }

    const exact = this.chargingWindows.find((w) => w.hours === this.selectedHours);
    if (exact) {
      this.selectedWindow = exact;
      return;
    }

    let best = this.chargingWindows[0];
    let bestDiff = Math.abs(best.hours - this.selectedHours);

    for (const w of this.chargingWindows) {
      const diff = Math.abs(w.hours - this.selectedHours);
      if (diff < bestDiff) {
        best = w;
        bestDiff = diff;
      }
    }

    this.selectedWindow = best;
  }

  onHoursChange(event: Event): void {
    this.selectedHours = Number((event.target as HTMLInputElement).value);
    this.updateSelectedWindow();
  }

  formatDateTime(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleString('en-GB', {
      timeZone: 'Europe/London',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  }

  formatShortDate(localDate: string): string {
    if (!localDate) return '';
    const [y, m, d] = localDate.split('-').map(Number);
    if (!y || !m || !d) return localDate;
    const dt = new Date(Date.UTC(y, m - 1, d));
    return dt.toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit' });
  }
}
