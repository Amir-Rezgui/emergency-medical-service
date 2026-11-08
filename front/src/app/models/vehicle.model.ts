import { Equipment } from './equipment.model';

export interface Vehicle {
  id: string;
  name: string;
  status: 'available' | 'busy' | 'maintenance' | 'out-of-service' | 'offline';
  type: 'ambulance' | 'supervisor' | 'fire-truck' | 'rescue' | 'other';
  location: { lat: number; lng: number };
  crew: string[];
  lastUpdate: string;
  equipment?: Equipment[];
}
