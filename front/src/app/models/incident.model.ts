export interface Incident {
  id: string;
  title: string;
  location: string;
  coordinates: { lat: number; lng: number };
  time: string;
  type: 'urgent' | 'normal';
  tags: string[];
  status: string;
  priority: number;
}

export interface IncidentRealtimeEvent {
  action: string;
  incidentId: string;
  incident: Incident;
}
