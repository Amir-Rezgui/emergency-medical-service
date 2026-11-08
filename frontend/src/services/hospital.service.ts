import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Equipment } from '../app/models/equipment.model';
import { Facility, Hospital } from '../app/models/hospital.model';
import { environment } from '../environments/environment';

type ApiCoordinates = { lat?: number; lng?: number };
type ApiEquipment = { id?: string; name?: string; status?: string; lastChecked?: string; quantity?: number };
type ApiHospital = {
  id?: string;
  name?: string;
  status?: string;
  beds?: string | number;
  distance?: string;
  type?: string;
  occupancy?: number;
  coordinates?: ApiCoordinates;
  equipment?: ApiEquipment[];
  facilityType?: string;
  waitTime?: string;
  icu?: number;
  icuTotal?: number;
  waitType?: string;
};

@Injectable({ providedIn: 'root' })
export class HospitalService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  private async apiFetch<T>(path: string, init?: { method?: string; body?: unknown }) {
    return firstValueFrom(this.http.request<T>(init?.method || 'GET', `${this.apiBaseUrl}${path}`, {
      body: init?.body,
    }));
  }

  private normalizeEquipmentStatus(status?: string): Equipment['status'] {
    switch ((status || '').toLowerCase()) {
      case 'maintenance':
      case 'needs-maintenance':
      case 'low':
        return 'needs-maintenance';
      case 'out':
      case 'offline':
      case 'out-of-service':
        return 'out-of-service';
      default:
        return 'functional';
    }
  }

  private mapEquipment(items?: ApiEquipment[]): Equipment[] {
    return (items || []).map((item, index) => ({
      id: item.id || `EQ-${index + 1}`,
      name: item.name || 'Unnamed equipment',
      status: this.normalizeEquipmentStatus(item.status),
      lastChecked: item.lastChecked || new Date().toISOString().split('T')[0],
      quantity: item.quantity ?? 0,
    }));
  }

  private mapHospital(item: ApiHospital): Hospital {
    const icu = item.icu ?? 0;
    const icuTotal = item.icuTotal ?? Math.max(icu, 1);
    const occupancy = item.occupancy ?? (icuTotal > 0 ? Math.round((icu / icuTotal) * 100) : 0);
    const beds = typeof item.beds === 'number' ? item.beds : Number.parseInt(item.beds || '0', 10) || 0;

    return {
      id: item.id || crypto.randomUUID(),
      name: item.name || 'Unknown hospital',
      type: item.type || item.facilityType || 'Hospital',
      waitTime: typeof item.waitTime === 'number' ? `${item.waitTime} min` : (item.waitTime || '0 min'),
      icu,
      icuTotal,
      beds,
      waitType: item.waitType === 'error' || occupancy >= 85 ? 'error' : 'primary',
      equipment: this.mapEquipment(item.equipment),
      coordinates: item.coordinates?.lat !== undefined && item.coordinates?.lng !== undefined ? {
        lat: item.coordinates.lat,
        lng: item.coordinates.lng,
      } : undefined,
      occupancy,
      facilityType: item.facilityType,
      status: item.status,
    };
  }

  private mapFacility(item: ApiHospital): Facility {
    const hospital = this.mapHospital(item);
    const occupancy = hospital.occupancy ?? 0;

    return {
      id: hospital.id,
      name: hospital.name,
      status: item.status || (occupancy >= 90 ? `CRITICAL (${occupancy}%)` : occupancy >= 75 ? `BUSY (${occupancy}%)` : `STABLE (${occupancy}%)`),
      beds: String(hospital.beds).padStart(2, '0'),
      distance: item.distance || 'N/A',
      type: occupancy >= 90 ? 'error' : occupancy >= 75 ? 'warning' : 'success',
      occupancy,
      coordinates: hospital.coordinates || { lat: 36.8065, lng: 10.1815 },
      equipment: hospital.equipment,
    };
  }

  async getFacilities(): Promise<Facility[]> {
    const data = await this.apiFetch<ApiHospital[]>('/api/facilities');
    return data.map((item) => this.mapFacility(item));
  }

  async getHospitals(): Promise<Hospital[]> {
    const data = await this.apiFetch<ApiHospital[]>('/api/hospitals');
    return data.map((item) => this.mapHospital(item));
  }

  async addHospital(hospital: Partial<Hospital>): Promise<Hospital> {
    const created = await this.apiFetch<ApiHospital>('/api/hospitals', { method: 'POST', body: hospital });
    return this.mapHospital(created);
  }

  async updateHospital(id: string, hospital: Partial<Hospital>): Promise<Hospital> {
    const updated = await this.apiFetch<ApiHospital>(`/api/hospitals/${id}`, { method: 'PUT', body: hospital });
    return this.mapHospital(updated);
  }

  async deleteHospital(id: string) {
    await this.apiFetch<void>(`/api/hospitals/${id}`, { method: 'DELETE' });
    return true;
  }
}
