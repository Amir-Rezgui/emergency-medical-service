import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AmbulanceService } from '../../../services/ambulance.service';
import { HospitalService } from '../../../services/hospital.service';
import { Hospital } from '../../models/hospital.model';
import { Vehicle } from '../../models/vehicle.model';

@Component({
  selector: 'app-resource-management-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './resource-management-page.component.html',
  styleUrl: './resource-management-page.component.css',
})
export class ResourceManagementPageComponent implements OnInit {
  private readonly ambulanceService = inject(AmbulanceService);
  private readonly hospitalService = inject(HospitalService);

  loading = true;
  hospitals: Hospital[] = [];
  fleetUnits: Vehicle[] = [];
  maintenance: Array<{ id: string; chassis: string; last: string; next: string; health: number; healthType: string }> = [];

  async ngOnInit() {
    const [hospitals, maintenance, fleetUnits] = await Promise.all([
      this.hospitalService.getHospitals(),
      this.ambulanceService.getMaintenanceLogs(),
      this.ambulanceService.getFleetUnits(),
    ]);
    this.hospitals = hospitals;
    this.maintenance = maintenance;
    this.fleetUnits = fleetUnits;
    this.loading = false;
  }

  averageOccupancy() {
    if (!this.hospitals.length) return 0;
    return Math.round(this.hospitals.reduce((sum, item) => sum + (item.occupancy ?? 0), 0) / this.hospitals.length);
  }
}


