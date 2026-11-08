import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Incident } from '../app/models/incident.model';
import { environment } from '../environments/environment';

type ApiCoordinates = { lat?: number; lng?: number };
type ApiIncident = { id?: string; title?: string; location?: string; coordinates?: ApiCoordinates; time?: string; type?: string; tags?: string[]; status?: string; priority?: number };

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  private async apiFetch<T>(path: string, init?: { method?: string; body?: unknown }) {
    return firstValueFrom(this.http.request<T>(init?.method || 'GET', `${this.apiBaseUrl}${path}`, {
      body: init?.body,
    }));
  }

  private mapIncident(item: ApiIncident): Incident {
    return {
      id: item.id || crypto.randomUUID(),
      title: item.title || 'Untitled incident',
      location: item.location || 'Unknown location',
      coordinates: {
        lat: item.coordinates?.lat ?? 36.8065,
        lng: item.coordinates?.lng ?? 10.1815,
      },
      time: item.time || 'Just now',
      type: (item.priority ?? 2) <= 1 || (item.type || '').toLowerCase() === 'urgent' ? 'urgent' : 'normal',
      tags: item.tags || [],
      status: item.status || 'Active',
      priority: item.priority ?? 2,
    };
  }

  async getIncidents(): Promise<Incident[]> {
    const data = await this.apiFetch<ApiIncident[]>('/api/incidents');
    return data.map((item) => this.mapIncident(item));
  }

  async reportIncident(incident: Omit<Incident, 'id' | 'time'>): Promise<Incident> {
    const created = await this.apiFetch<ApiIncident>('/api/incidents', { method: 'POST', body: incident });
    return this.mapIncident(created);
  }
}
