import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { RealtimeService } from '../../core/services/realtime.service';
import { IncidentService } from '../../../services/incident.service';
import { Incident } from '../../models/incident.model';

@Component({
  selector: 'app-incident-history-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './incident-history-page.component.html',
  styleUrl: './incident-history-page.component.css',
})
export class IncidentHistoryPageComponent implements OnInit {
  private readonly incidentService = inject(IncidentService);
  private readonly destroyRef = inject(DestroyRef);

  readonly realtime = inject(RealtimeService);

  loading = true;
  incidents: Incident[] = [];
  filteredIncidents: Incident[] = [];
  selectedFilter: 'all' | 'urgent' | 'normal' = 'all';
  searchQuery = '';

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
    await this.loadIncidents(true);

    this.realtime.watchHospitalManagerIncidentEvents()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.scheduleRefresh());
  }

  setFilter(filter: 'all' | 'urgent' | 'normal') {
    this.selectedFilter = filter;
    this.applyFilters();
  }

  applyFilters() {
    let filtered = this.incidents;

    if (this.selectedFilter !== 'all') {
      filtered = filtered.filter(i => i.type === this.selectedFilter);
    }

    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(i =>
        i.title.toLowerCase().includes(query) ||
        i.location.toLowerCase().includes(query) ||
        i.tags.some(t => t.toLowerCase().includes(query))
      );
    }

    this.filteredIncidents = filtered;
  }

  private scheduleRefresh() {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }

    this.refreshTimer = setTimeout(() => {
      void this.loadIncidents();
    }, 350);
  }

  private async loadIncidents(initial = false) {
    if (this.refreshInFlight) {
      return;
    }

    this.refreshInFlight = true;
    if (initial) {
      this.loading = true;
    }

    try {
      this.incidents = await this.incidentService.getIncidents();
      this.applyFilters();
    } finally {
      this.refreshInFlight = false;
      this.loading = false;
    }
  }
}
