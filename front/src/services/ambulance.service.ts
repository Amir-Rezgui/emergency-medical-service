import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { DispatchAssignment, DispatchRequest } from '../app/models/dispatch.model';
import { Equipment } from '../app/models/equipment.model';
import { Vehicle } from '../app/models/vehicle.model';
import { environment } from '../environments/environment';

type ApiCoordinates = { lat?: number; lng?: number };
type ApiEquipment = { id?: string; name?: string; status?: string; lastChecked?: string; quantity?: number };
type ApiVehicle = { id?: string; name?: string; status?: string; type?: string; location?: ApiCoordinates; crew?: string[]; lastUpdate?: string; equipment?: ApiEquipment[] };

@Injectable({ providedIn: 'root' })
export class AmbulanceService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  private readonly mockMaintenance = [
    { id: 'AMB-102', chassis: 'Ford Transit 350', last: 'Oct 12, 2023', next: 'Jan 12, 2024', health: 95, healthType: 'primary' },
    { id: 'AMB-205', chassis: 'Mercedes Sprinter', last: 'Sep 28, 2023', next: 'Oct 28, 2023', health: 45, healthType: 'error' },
    { id: 'AMB-109', chassis: 'Chevrolet G4500', last: 'Aug 15, 2023', next: 'Nov 15, 2023', health: 78, healthType: 'primary' },
  ];

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

  private normalizeVehicleStatus(status?: string): Vehicle['status'] {
    switch ((status || '').toLowerCase()) {
      case 'busy':
      case 'in-service':
        return 'busy';
      case 'maintenance':
        return 'maintenance';
      case 'out-of-service':
        return 'out-of-service';
      case 'off-duty':
      case 'offline':
        return 'offline';
      default:
        return 'available';
    }
  }

  private normalizeVehicleType(type?: string): Vehicle['type'] {
    switch ((type || '').toLowerCase()) {
      case 'ambulance':
      case 'supervisor':
      case 'fire-truck':
      case 'rescue':
        return type!.toLowerCase() as Vehicle['type'];
      default:
        return 'other';
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

  private mapVehicle(item: ApiVehicle): Vehicle {
    return {
      id: item.id || crypto.randomUUID(),
      name: item.name || 'Unnamed unit',
      status: this.normalizeVehicleStatus(item.status),
      type: this.normalizeVehicleType(item.type),
      location: {
        lat: item.location?.lat ?? 36.8065,
        lng: item.location?.lng ?? 10.1815,
      },
      crew: item.crew || [],
      lastUpdate: item.lastUpdate || 'Unknown',
      equipment: this.mapEquipment(item.equipment),
    };
  }

  async getMaintenanceLogs() {
    return this.mockMaintenance;
  }

  async getFleetUnits(): Promise<Vehicle[]> {
    const data = await this.apiFetch<ApiVehicle[]>('/api/vehicles');
    return data.map((item) => this.mapVehicle(item));
  }

  async getVehicles(): Promise<Vehicle[]> {
    return this.getFleetUnits();
  }

  async addAmbulance(vehicle: Partial<Vehicle>): Promise<Vehicle> {
    const created = await this.apiFetch<ApiVehicle>('/api/vehicles', { method: 'POST', body: vehicle });
    return this.mapVehicle(created);
  }

  async updateAmbulance(id: string, vehicle: Partial<Vehicle>): Promise<Vehicle> {
    const updated = await this.apiFetch<ApiVehicle>(`/api/vehicles/${id}`, { method: 'PUT', body: vehicle });
    return this.mapVehicle(updated);
  }

  async deleteAmbulance(id: string) {
    await this.apiFetch<void>(`/api/vehicles/${id}`, { method: 'DELETE' });
    return true;
  }

  async dispatchAmbulance(request: DispatchRequest): Promise<DispatchAssignment> {
    return this.apiFetch<DispatchAssignment>('/api/dispatch/assignments', { method: 'POST', body: request });
  }
}
