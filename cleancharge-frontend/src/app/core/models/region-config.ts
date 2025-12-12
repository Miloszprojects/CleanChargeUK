import { GridRegion } from './region.model';

export const GRID_REGIONS: GridRegion[] = [
  { id: 1, shortName: 'North East',            ciRegionId: 4  },
  { id: 2, shortName: 'North West',            ciRegionId: 3  },
  { id: 3, shortName: 'Yorkshire and Humber',  ciRegionId: 5  },
  { id: 4, shortName: 'East Midlands',         ciRegionId: 9  },
  { id: 5, shortName: 'West Midlands',         ciRegionId: 8  },
  { id: 6, shortName: 'East',                  ciRegionId: 10 },
  { id: 7, shortName: 'London',                ciRegionId: 13 },
  { id: 8, shortName: 'South East',            ciRegionId: 14 },
  { id: 9, shortName: 'South West',            ciRegionId: 11 },
  { id: 10, shortName: 'Wales',             ciRegionId: 17 },
  { id: 11, shortName: 'Scotland',                ciRegionId: 16 },
  { id: 12, shortName: 'Northern Ireland*',    ciRegionId: 3  },
];

export function getRegionBySvgId(svgId: number): GridRegion | undefined {
  return GRID_REGIONS.find(r => r.id === svgId);
}
