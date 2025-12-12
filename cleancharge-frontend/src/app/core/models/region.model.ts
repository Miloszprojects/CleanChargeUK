export interface GridRegion {
  id: number;
  shortName: string;
  ciRegionId: number;
}

export interface FuelShare {
  fuel: string;
  percentage: number;
}

export interface ChargingWindowOption {
  hours: number;
  start: string;
  end: string;
  averageCleanEnergyPercentage: number;
}

export interface DailyEnergyMix {
  date: string;
  cleanPercentage: number;
  mix: FuelShare[];
}

export interface RegionDashboardSummary {
  regionId: number;
  name: string;
  currentMix: FuelShare[];
  chargingWindows: ChargingWindowOption[];
  dailyMixes: DailyEnergyMix[];
}
