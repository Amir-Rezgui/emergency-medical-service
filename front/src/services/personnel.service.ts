import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Personnel } from '../app/models/personnel.model';
import { environment } from '../environments/environment';

type ApiPersonnel = { id?: string; name?: string; email?: string; role?: string; status?: string; statusType?: string; iconName?: string; color?: string };

@Injectable({ providedIn: 'root' })
export class PersonnelService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl.replace(/\/+$/, '');

  private async apiFetch<T>(path: string, init?: { method?: string; body?: unknown }) {
    return firstValueFrom(this.http.request<T>(init?.method || 'GET', `${this.apiBaseUrl}${path}`, {
      body: init?.body,
    }));
  }

  private mapPersonnel(item: ApiPersonnel): Personnel {
    const statusType = item.statusType === 'success' || item.statusType === 'urgent' ? item.statusType : 'normal';

    return {
      id: item.id || crypto.randomUUID(),
      name: item.name || 'Unknown staff member',
      email: item.email || 'N/A',
      role: item.role || 'Personnel',
      status: item.status || 'Unknown',
      statusType,
      iconName: item.iconName || 'FileText',
      color: item.color || 'bg-surface-container text-on-surface-variant',
    };
  }

  async getPersonnel(): Promise<Personnel[]> {
    const data = await this.apiFetch<ApiPersonnel[]>('/api/personnel');
    return data.map((item) => this.mapPersonnel(item));
  }
}
