import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IncidentService } from '../../../services/incident.service';
import { RealtimeService } from '../../core/services/realtime.service';
import { Incident } from '../../models/incident.model';

@Component({
  selector: 'app-incident-details-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './incident-details-page.component.html',
  styleUrl: './incident-details-page.component.css',
})
export class IncidentDetailsPageComponent implements OnInit {
  private readonly incidentService = inject(IncidentService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly realtime = inject(RealtimeService);

  loading = true;
  incident: Incident | null = null;
  incidentId: string | null = null;

  async ngOnInit() {
    this.route.params
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        this.incidentId = params['id'];
        this.loadIncidentDetails();
      });
  }

  private async loadIncidentDetails() {
    if (!this.incidentId) {
      this.loading = false;
      return;
    }

    try {
      const incidents = await this.incidentService.getIncidents();
      this.incident = incidents.find(i => i.id === this.incidentId) || null;
    } catch (error) {
      console.error('Failed to load incident details:', error);
    } finally {
      this.loading = false;
    }
  }

  goBack() {
    this.router.navigate(['/incidents']);
  }

  getIconForType(): string {
    return this.incident?.type === 'urgent' ? '🔴' : '🟡';
  }

  getColorClass(): string {
    return this.incident?.type === 'urgent' ? 'urgent' : 'normal';
  }

  getStatusBadgeClass(): string {
    if (this.incident?.status === 'Active' || this.incident?.status === 'Ongoing') {
      return 'bg-primary/10 text-primary';
    } else if (this.incident?.status === 'Resolved') {
      return 'bg-green-100 text-green-700';
    }
    return 'bg-tertiary/10 text-tertiary';
  }
}
