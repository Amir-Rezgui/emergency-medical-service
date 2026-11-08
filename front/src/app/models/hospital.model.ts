import { Equipment } from './equipment.model';

export interface Facility {
  id: string;
  name: string;
  status: string;
  beds: string;
  distance: string;
  type: 'error' | 'warning' | 'success';
  occupancy: number;
  coordinates: { lat: number; lng: number };
  equipment?: Equipment[];
}

export interface Hospital {
  id: string;
  name: string;
  type: string;
  waitTime: string;
  icu: number;
  icuTotal: number;
  beds: number;
  waitType: 'primary' | 'error';
  equipment?: Equipment[];
  coordinates?: { lat: number; lng: number };
  occupancy?: number;
  facilityType?: string;
  status?: string;
}
