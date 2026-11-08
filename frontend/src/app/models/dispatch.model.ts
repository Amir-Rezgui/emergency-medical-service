export interface DispatchRoute {
  vehicleId: string;
  vehicleName: string;
  incidentId: string;
  incidentTitle: string;
  origin: { lat: number; lng: number };
  destination: { lat: number; lng: number };
  path: Array<{ lat: number; lng: number }>;
  distanceKm: number;
  estimatedMinutes: number;
  trafficLevel: string;
  turnByTurn: string[];
}

export interface DispatchAssignment {
  incidentId: string;
  incidentTitle: string;
  vehicleId: string;
  vehicleName: string;
  dispatcher: string;
  notes?: string | null;
  vehicleStatus: string;
  incidentStatus: string;
  dispatchedAt: string;
  incidentTags: string[];
  route: DispatchRoute;
}

export interface DispatchRequest {
  incidentId: string;
  vehicleId: string;
  dispatcher?: string;
  notes?: string;
}
