export interface DashboardStats {
  activeAmbulances: number;
  totalAmbulances: number;
  avgResponseTime: string;
  hospitalOccupancy: number;
  criticalIncidents: number;
}

export interface DispatchVolume {
  time: string;
  volume: number;
}

export interface ResponseData {
  day: string;
  arrival: number;
  dispatch: number;
}
