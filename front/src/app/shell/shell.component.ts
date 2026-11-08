import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { IncidentService } from '../../services/incident.service';
import { AmbulanceService } from '../../services/ambulance.service';
import { PersonnelService } from '../../services/personnel.service';
import { HospitalService } from '../../services/hospital.service';
import { Hospital, Facility } from '../../app/models/hospital.model';
import { Incident } from '../../app/models/incident.model';
import { Personnel } from '../../app/models/personnel.model';
import { Vehicle } from '../../app/models/vehicle.model';
import { RealtimeService } from '../core/services/realtime.service';

interface SearchResult {
  id: string;
  title: string;
  subtitle: string;
  path: string;
  type: 'incident' | 'vehicle' | 'personnel' | 'facility';
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="min-h-screen bg-surface">
      <aside class="fixed left-0 top-0 z-40 flex h-screen w-64 flex-col border-r border-outline-variant/10 bg-surface-container-low p-4">
        <div class="mb-8 flex items-center justify-between px-2">
          <div class="flex items-center gap-3">
            <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-primary text-white shadow-lg shadow-primary/20">
              <svg viewBox="0 0 24 24" class="h-5 w-5 fill-current" aria-hidden="true">
                <path d="M19 8h-3V5a1 1 0 0 0-1-1H9a1 1 0 0 0-1 1v3H5a2 2 0 0 0-2 2v5a3 3 0 0 0 3 3h1.1a5 5 0 0 0 9.8 0H18a3 3 0 0 0 3-3v-5a2 2 0 0 0-2-2Zm-9-2h4v2h-4V6Zm2 13a3 3 0 1 1 0-6 3 3 0 0 1 0 6Zm7-4a1 1 0 0 1-1 1h-1.1a5 5 0 0 0-9.8 0H6a1 1 0 0 1-1-1v-5h14v5Z"/>
              </svg>
            </div>
            <div>
              <h1 class="font-headline text-xl font-black uppercase tracking-tight">EMS Command</h1>
              <p class="text-[10px] font-bold uppercase tracking-wider text-on-surface-variant">Angular Control Center</p>
            </div>
          </div>
          <div class="h-2 w-2 rounded-full" [class.bg-primary]="realtime.connected()" [class.animate-pulse]="realtime.connected()" [class.bg-tertiary]="!realtime.connected()"></div>
        </div>

        <nav class="flex-1 space-y-1">
          @for (item of navItems; track item.path) {
            <a
              [routerLink]="item.path"
              routerLinkActive="bg-surface-container-lowest text-primary border border-outline-variant/5 shadow-sm"
              [routerLinkActiveOptions]="{ exact: item.exact ?? false }"
              class="flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold text-on-surface-variant transition hover:bg-surface-container-high/60 hover:text-on-surface"
            >
              <span class="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-surface-container-high text-[10px] font-black uppercase text-primary">{{ item.short }}</span>
              <span>{{ item.label }}</span>
            </a>
          }
        </nav>

        <div class="border-t border-outline-variant/20 pt-4">
          <button (click)="signOut()" class="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold text-tertiary transition hover:bg-tertiary/5">
            <span class="inline-flex h-8 w-8 items-center justify-center rounded-lg bg-tertiary/10 text-tertiary">
              <svg viewBox="0 0 24 24" class="h-4 w-4 fill-current" aria-hidden="true">
                <path d="M10 17v-3H3v-4h7V7l5 5-5 5Zm7-12H9v2h8v10H9v2h8a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2Z"/>
              </svg>
            </span>
            Sign Out
          </button>
        </div>
      </aside>

      <header class="fixed right-0 top-0 z-30 flex h-16 w-[calc(100%-16rem)] items-center justify-between border-b border-outline-variant/10 bg-surface/80 px-8 backdrop-blur">
        <div class="relative w-full max-w-xl">
          <input
            [(ngModel)]="query"
            (ngModelChange)="handleSearch($event)"
            (focus)="searchOpen = true"
            placeholder="Search incidents, vehicles, hospitals, and personnel..."
            class="w-full rounded-xl border-none bg-surface-container-high py-3 pl-4 pr-4 text-sm outline-none ring-primary/20 transition focus:ring-2"
          />

          @if (searchOpen && (results.length || query.length >= 2 || searching)) {
            <div class="absolute left-0 right-0 top-full mt-2 overflow-hidden rounded-2xl border border-outline-variant/10 bg-surface-container-lowest shadow-2xl">
              <div class="flex items-center justify-between border-b border-outline-variant/10 bg-surface-container-low px-4 py-2 text-[10px] font-bold uppercase tracking-widest text-outline">
                <span>Search Results</span>
                @if (searching) { <span>Loading</span> }
              </div>
              <div class="max-h-96 overflow-auto p-2">
                @if (!results.length && !searching && query.length >= 2) {
                  <div class="p-6 text-center text-sm text-on-surface-variant">No results found for "{{ query }}".</div>
                }
                @for (result of results; track result.type + result.id) {
                  <button (click)="go(result.path)" class="flex w-full items-center justify-between rounded-xl px-3 py-3 text-left transition hover:bg-surface-container-low">
                    <div>
                      <p class="text-sm font-semibold text-on-surface">{{ result.title }}</p>
                      <p class="text-[10px] font-bold uppercase tracking-wider text-outline">{{ result.subtitle }}</p>
                    </div>
                    <span class="badge bg-primary/10 text-primary">{{ result.type }}</span>
                  </button>
                }
              </div>
            </div>
          }
        </div>

        <div class="ml-6 flex items-center gap-4">
          <div class="relative">
            <button (click)="notificationsOpen = !notificationsOpen" class="relative flex items-center gap-2 rounded-xl bg-surface-container-high px-3 py-2 text-sm font-semibold text-on-surface">
              <svg viewBox="0 0 24 24" class="h-4 w-4 fill-current text-primary" aria-hidden="true">
                <path d="M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm6-6V11a6 6 0 1 0-12 0v5l-2 2v1h16v-1l-2-2Z"/>
              </svg>
              Alerts
              @if (unreadCount() > 0) {
                <span class="absolute -right-1 -top-1 inline-flex h-5 w-5 items-center justify-center rounded-full bg-tertiary text-[10px] font-black text-white">{{ unreadCount() }}</span>
              }
            </button>
            @if (notificationsOpen) {
              <div class="absolute right-0 top-full mt-2 w-96 overflow-hidden rounded-2xl border border-outline-variant/10 bg-surface-container-lowest shadow-2xl">
                <div class="flex items-center justify-between border-b border-outline-variant/10 bg-surface-container-low px-4 py-3">
                  <p class="text-sm font-black text-on-surface">Notifications</p>
                  <button (click)="realtime.clearAll()" class="text-xs font-bold text-primary">Clear</button>
                </div>
                <div class="max-h-96 overflow-auto">
                  @if (!realtime.notifications().length) {
                    <div class="p-6 text-center text-sm text-on-surface-variant">No notifications yet.</div>
                  }
                  @for (item of realtime.notifications(); track item.id) {
                    <button (click)="realtime.markAsRead(item.id)" class="block w-full border-b border-outline-variant/10 px-4 py-3 text-left" [ngClass]="{ 'bg-primary/5': !item.read }">
                      <div class="flex items-start justify-between gap-3">
                        <div>
                          <p class="text-sm font-semibold text-on-surface">{{ item.title }}</p>
                          <p class="mt-1 text-xs text-on-surface-variant">{{ item.message }}</p>
                        </div>
                        <span class="badge" [ngClass]="item.severity === 'info' ? 'bg-primary/10 text-primary' : 'bg-tertiary/10 text-tertiary'">{{ item.severity }}</span>
                      </div>
                    </button>
                  }
                </div>
              </div>
            }
          </div>

          <a routerLink="/profile" class="flex items-center gap-3 rounded-xl px-3 py-2 transition hover:bg-surface-container-high">
            <div class="text-right">
              <p class="text-sm font-bold text-on-surface">Command Profile</p>
              <p class="text-[10px] font-bold uppercase tracking-wider text-outline">Authenticated User</p>
            </div>
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-sm font-black text-white">CP</div>
          </a>
        </div>
      </header>

      <main class="min-h-screen pl-64 pt-16">
        <div class="mx-auto max-w-7xl p-8">
          <router-outlet />
        </div>
      </main>

      <div class="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
        <div class="absolute right-[-10%] top-[-10%] h-[45%] w-[45%] rounded-full bg-primary/5 blur-[120px]"></div>
        <div class="absolute bottom-[-10%] left-[-10%] h-[35%] w-[35%] rounded-full bg-tertiary/5 blur-[100px]"></div>
      </div>
    </div>
  `,
})
export class ShellComponent {
  private readonly router = inject(Router);
  private readonly incidentService = inject(IncidentService);
  private readonly ambulanceService = inject(AmbulanceService);
  private readonly personnelService = inject(PersonnelService);
  private readonly hospitalService = inject(HospitalService);

  readonly realtime = inject(RealtimeService);

  readonly navItems = [
    { path: '/', label: 'Dashboard', short: 'DB', exact: true },
    { path: '/incidents', label: 'Incident History', short: 'IH' },
    { path: '/users', label: 'Personnel', short: 'US' },
    { path: '/hospitals', label: 'Hospitals', short: 'HO' },
    { path: '/ambulances', label: 'Vehicles', short: 'VE' },
    { path: '/resources', label: 'Resources', short: 'RS' },
    { path: '/reports', label: 'Reports', short: 'RP' },
    { path: '/settings', label: 'Settings', short: 'ST' },
  ];

  query = '';
  results: SearchResult[] = [];
  searchOpen = false;
  searching = false;
  notificationsOpen = false;
  private searchTimer?: ReturnType<typeof setTimeout>;

  unreadCount() {
    return this.realtime.notifications().filter((item) => !item.read).length;
  }

  handleSearch(query: string) {
    this.query = query;
    this.searchOpen = true;
    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
    }

    if (query.trim().length < 2) {
      this.results = [];
      return;
    }

    this.searchTimer = setTimeout(async () => {
      this.searching = true;
      try {
        const [incidents, vehicles, personnel, facilities] = await Promise.all([
          this.incidentService.getIncidents(),
          this.ambulanceService.getVehicles(),
          this.personnelService.getPersonnel(),
          this.hospitalService.getFacilities(),
        ]);
        this.results = this.buildResults(query, incidents, vehicles, personnel, facilities).slice(0, 8);
      } finally {
        this.searching = false;
      }
    }, 250);
  }

  private buildResults(query: string, incidents: Incident[], vehicles: Vehicle[], personnel: Personnel[], facilities: Facility[]) {
    const q = query.toLowerCase();
    const results: SearchResult[] = [];

    incidents.forEach((item) => {
      if (item.title.toLowerCase().includes(q) || item.location.toLowerCase().includes(q)) {
        results.push({ id: item.id, type: 'incident', title: item.title, subtitle: item.location, path: '/' });
      }
    });
    vehicles.forEach((item) => {
      if (item.name.toLowerCase().includes(q) || item.id.toLowerCase().includes(q)) {
        results.push({ id: item.id, type: 'vehicle', title: item.name, subtitle: `${item.type} • ${item.status}`, path: '/ambulances' });
      }
    });
    personnel.forEach((item) => {
      if (item.name.toLowerCase().includes(q) || item.role.toLowerCase().includes(q)) {
        results.push({ id: item.id, type: 'personnel', title: item.name, subtitle: item.role, path: '/users' });
      }
    });
    facilities.forEach((item) => {
      if (item.name.toLowerCase().includes(q)) {
        results.push({ id: item.id, type: 'facility', title: item.name, subtitle: item.status, path: '/hospitals' });
      }
    });

    return results;
  }

  go(path: string) {
    this.searchOpen = false;
    this.notificationsOpen = false;
    void this.router.navigateByUrl(path);
  }

  signOut() {
    void this.router.navigateByUrl('/login');
  }
}
