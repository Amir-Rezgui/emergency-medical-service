import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { UserProfile, UserStat } from '../app/models/profile.model';
import { environment } from '../environments/environment';

type ApiUserProfile = { id?: string; name?: string; role?: string; email?: string; phone?: string; location?: string; joined?: string; specialization?: string; stats?: UserStat[] };

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  private async apiFetch<T>(path: string, init?: { method?: string; body?: unknown }) {
    return firstValueFrom(this.http.request<T>(init?.method || 'GET', `${this.apiBaseUrl}${path}`, {
      body: init?.body,
    }));
  }

  async getUserProfile(): Promise<UserProfile> {
    const data = await this.apiFetch<ApiUserProfile>('/api/profile');
    return {
      id: data.id || 'unknown',
      name: data.name || 'Unknown user',
      role: data.role || 'Operator',
      email: data.email || 'N/A',
      phone: data.phone || 'N/A',
      location: data.location || 'N/A',
      joined: data.joined || 'N/A',
      specialization: data.specialization || 'N/A',
      stats: data.stats && data.stats.length > 0 ? data.stats : [
        { label: 'Role', value: data.role || 'N/A', iconName: 'Activity', color: 'text-primary' },
        { label: 'Location', value: data.location || 'N/A', iconName: 'Clock', color: 'text-tertiary' },
        { label: 'Joined', value: data.joined || 'N/A', iconName: 'FileText', color: 'text-secondary' },
      ],
    };
  }
}
