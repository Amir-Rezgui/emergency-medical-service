import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { RealtimeService } from '../../core/services/realtime.service';
import { AnalyticsService } from '../../../services/analytics.service';
import { IncidentService } from '../../../services/incident.service';
import { HospitalService } from '../../../services/hospital.service';
import { AmbulanceService } from '../../../services/ambulance.service';
import { DashboardStats, DispatchVolume } from '../../models/analytics.model';
import { Facility } from '../../models/hospital.model';
import { Incident } from '../../models/incident.model';
import { Vehicle } from '../../models/vehicle.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css',
})
export class DashboardPageComponent implements OnInit {
  private readonly analyticsService = inject(AnalyticsService);
  private readonly incidentService = inject(IncidentService);
  private readonly hospitalService = inject(HospitalService);
  private readonly ambulanceService = inject(AmbulanceService);
  private readonly destroyRef = inject(DestroyRef);

  readonly realtime = inject(RealtimeService);

  loading = true;
  stats: DashboardStats | null = null;
  incidents: Incident[] = [];
  facilities: Facility[] = [];
  vehicles: Vehicle[] = [];
  dispatchData: DispatchVolume[] = [];
  maxDispatch = 0;

  private refreshTimer?: ReturnType<typeof setTimeout>;
  private refreshInFlight = false;

  constructor() {
    this.destroyRef.onDestroy(() => {
      if (this.refreshTimer) {
        clearTimeout(this.refreshTimer);
      }
    });
  }

  async ngOnInit() {
    await this.refreshDashboardData(true);

    this.realtime.watchHospitalManagerIncidentEvents()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.scheduleRefresh());

    this.realtime.watchHospitalManagerDispatches()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.scheduleRefresh());
  }

  statCards() {
    return [
      { label: 'Active Ambulances', value: `${this.stats?.activeAmbulances ?? 0} / ${this.stats?.totalAmbulances ?? 0}`, note: 'Current fleet utilization', positive: true },
      { label: 'Avg. Response Time', value: this.stats?.avgResponseTime || '--', note: 'Live average', positive: false },
      { label: 'Hospital Occupancy', value: `${this.stats?.hospitalOccupancy ?? 0}%`, note: 'Regional network load', positive: true },
      { label: 'Critical Incidents', value: String(this.stats?.criticalIncidents ?? 0).padStart(2, '0'), note: 'Priority 1 incidents', positive: false },
    ];
  }

  private scheduleRefresh() {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }

    this.refreshTimer = setTimeout(() => {
      void this.refreshDashboardData();
    }, 350);
  }

  private async refreshDashboardData(initial = false) {
    if (this.refreshInFlight) {
      return;
    }

    this.refreshInFlight = true;
    if (initial) {
      this.loading = true;
    }

    try {
      const [stats, incidents, facilities, dispatchData, vehicles] = await Promise.all([
        this.analyticsService.getDashboardStats(),
        this.incidentService.getIncidents(),
        this.hospitalService.getFacilities(),
        this.analyticsService.getDispatchVolume(),
        this.ambulanceService.getVehicles(),
      ]);

      this.stats = stats;
      this.incidents = incidents;
      this.facilities = facilities;
      this.dispatchData = dispatchData;
      this.vehicles = vehicles;
      this.maxDispatch = Math.max(...dispatchData.map((item) => item.volume), 1);
    } finally {
      this.refreshInFlight = false;
      this.loading = false;
    }
  }
}


