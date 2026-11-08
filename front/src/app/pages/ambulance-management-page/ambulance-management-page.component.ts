import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { RealtimeService } from '../../core/services/realtime.service';
import { AmbulanceService } from '../../../services/ambulance.service';
import { DispatchAssignment } from '../../models/dispatch.model';
import { Equipment } from '../../models/equipment.model';
import { Vehicle } from '../../models/vehicle.model';

@Component({
  selector: 'app-ambulance-management-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ambulance-management-page.component.html',
  styleUrl: './ambulance-management-page.component.css',
})
export class AmbulanceManagementPageComponent implements OnInit {
  private readonly ambulanceService = inject(AmbulanceService);
  private readonly destroyRef = inject(DestroyRef);

  readonly realtime = inject(RealtimeService);

  loading = true;
  vehicles: Vehicle[] = [];
  search = '';
  formOpen = false;
  editingId: string | null = null;
  form: Partial<Vehicle> = this.emptyForm();
  crewText = '';
  latText = 36.8065;
  lngText = 10.1815;
  selectedDriverVehicleId = '';
  vehicleDispatchFeed: DispatchAssignment[] = [];
  private vehicleDispatchSubscription?: Subscription;

  constructor() {
    this.destroyRef.onDestroy(() => this.vehicleDispatchSubscription?.unsubscribe());
  }

  async ngOnInit() {
    await this.refresh();

    this.realtime.watchDriverDispatches()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((dispatch) => {
        this.vehicles = this.vehicles.map((vehicle) => vehicle.id === dispatch.vehicleId
          ? { ...vehicle, status: 'busy', lastUpdate: dispatch.dispatchedAt }
          : vehicle,
        );
      });
  }

  emptyForm(): Partial<Vehicle> {
    return {
      name: '',
      type: 'ambulance',
      status: 'available',
      crew: [],
      location: { lat: 36.8065, lng: 10.1815 },
      lastUpdate: 'Just now',
      equipment: [] as Equipment[],
    };
  }

  filteredVehicles() {
    const q = this.search.toLowerCase();
    return this.vehicles.filter((item) => item.name.toLowerCase().includes(q) || item.id.toLowerCase().includes(q));
  }

  async refresh() {
    this.vehicles = await this.ambulanceService.getVehicles();
    this.loading = false;

    if (!this.selectedDriverVehicleId) {
      const firstVehicle = this.vehicles.find((vehicle) => vehicle.type === 'ambulance') || this.vehicles[0];
      if (firstVehicle) {
        this.subscribeToVehicle(firstVehicle.id);
      }
    }
  }

  subscribeToVehicle(vehicleId: string) {
    this.selectedDriverVehicleId = vehicleId;
    this.vehicleDispatchSubscription?.unsubscribe();
    this.vehicleDispatchFeed = vehicleId ? this.realtime.driverDispatchesSignal(vehicleId)() : [];

    if (!vehicleId) {
      return;
    }

    this.vehicleDispatchSubscription = this.realtime.watchDriverDispatches(vehicleId).subscribe((dispatch) => {
      this.vehicleDispatchFeed = [dispatch, ...this.vehicleDispatchFeed.filter((item) => item.dispatchedAt !== dispatch.dispatchedAt || item.vehicleId !== dispatch.vehicleId)].slice(0, 8);
    });
  }

  openForm(vehicle?: Vehicle) {
    this.editingId = vehicle?.id || null;
    this.form = vehicle ? { ...vehicle, location: { ...vehicle.location }, crew: [...vehicle.crew] } : this.emptyForm();
    this.crewText = vehicle?.crew.join(', ') || '';
    this.latText = vehicle?.location.lat || 36.8065;
    this.lngText = vehicle?.location.lng || 10.1815;
    this.formOpen = true;
  }

  closeForm() {
    this.formOpen = false;
    this.editingId = null;
    this.form = this.emptyForm();
    this.crewText = '';
  }

  async save() {
    const payload: Partial<Vehicle> = {
      ...this.form,
      crew: this.crewText.split(',').map((item) => item.trim()).filter(Boolean),
      location: { lat: Number(this.latText), lng: Number(this.lngText) },
      lastUpdate: 'Just now',
    };
    if (this.editingId) {
      await this.ambulanceService.updateAmbulance(this.editingId, payload);
    } else {
      await this.ambulanceService.addAmbulance(payload);
    }
    this.closeForm();
    await this.refresh();
  }

  async remove(id: string) {
    if (!window.confirm('Remove this vehicle?')) return;
    await this.ambulanceService.deleteAmbulance(id);
    await this.refresh();
  }
}




