import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { DashboardStats, DispatchVolume, ResponseData } from '../app/models/analytics.model';
import { environment } from '../environments/environment';

type ApiResponsePoint = { day?: string; arrival?: number; dispatch?: number };
type ApiDispatchPoint = { time?: string; volume?: number };
type ApiDashboardStats = { activeAmbulances?: number; totalAmbulances?: number; avgResponseTime?: string; hospitalOccupancy?: number; criticalIncidents?: number };

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  private async apiFetch<T>(path: string, init?: { method?: string; body?: unknown }) {
    return firstValueFrom(this.http.request<T>(init?.method || 'GET', `${this.apiBaseUrl}${path}`, {
      body: init?.body,
    }));
  }

  async getResponseData(): Promise<ResponseData[]> {
    const data = await this.apiFetch<ApiResponsePoint[]>('/api/analytics/response');
    return data.map((item, index) => ({
      day: item.day || `Day ${index + 1}`,
      arrival: item.arrival ?? 0,
      dispatch: item.dispatch ?? 0,
    }));
  }

  async getDispatchVolume(): Promise<DispatchVolume[]> {
    const data = await this.apiFetch<ApiDispatchPoint[]>('/api/analytics/dispatch');
    return data.map((item, index) => ({
      time: item.time || `${String(index).padStart(2, '0')}:00`,
      volume: item.volume ?? 0,
    }));
  }

  async getDashboardStats(): Promise<DashboardStats> {
    const data = await this.apiFetch<ApiDashboardStats>('/api/stats');
    return {
      activeAmbulances: data.activeAmbulances ?? 0,
      totalAmbulances: data.totalAmbulances ?? 0,
      avgResponseTime: data.avgResponseTime || '--',
      hospitalOccupancy: data.hospitalOccupancy ?? 0,
      criticalIncidents: data.criticalIncidents ?? 0,
    };
  }
}
