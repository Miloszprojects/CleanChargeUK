import {
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { GridRegion } from '../../../../core/models/region.model';

@Component({
  selector: 'app-uk-region-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './uk-region-map.html',
  styleUrl: './uk-region-map.css',
})
export class UkRegionMap {
  @Input({ required: true })
  regions: GridRegion[] = [];

  @Input()
  selectedRegionId: number | null = null;

  @Output()
  regionSelected = new EventEmitter<number>();

  // NOWE – żeby wyżej można było reagować na hover
  @Output()
  regionHovered = new EventEmitter<number | null>();

  hoveredRegionId: number | null = null;

  isSelected(id: number): boolean {
    return this.selectedRegionId === id;
  }

  isHovered(id: number): boolean {
    return this.hoveredRegionId === id;
  }

  onRegionClick(id: number): void {
    this.regionSelected.emit(id);
  }

  onRegionEnter(id: number): void {
    this.hoveredRegionId = id;
    this.regionHovered.emit(id);
  }

  onRegionLeave(): void {
    this.hoveredRegionId = null;
    this.regionHovered.emit(null);
  }

  getRegionName(id: number): string {
    const match = this.regions.find((r) => r.id === id);
    return match?.shortName ?? `Region ${id}`;
  }
}
