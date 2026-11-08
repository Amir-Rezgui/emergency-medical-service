import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RealtimeService } from '../../core/services/realtime.service';
import { HospitalService } from '../../../services/hospital.service';
import { Equipment } from '../../models/equipment.model';
import { Hospital } from '../../models/hospital.model';

@Component({
  selector: 'app-hospital-management-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hospital-management-page.component.html',
  styleUrl: './hospital-management-page.component.css',
})
export class HospitalManagementPageComponent implements OnInit {
  private readonly hospitalService = inject(HospitalService);

  readonly realtime = inject(RealtimeService);

  loading = true;
  hospitals: Hospital[] = [];
  search = '';
  formOpen = false;
  editingId: string | null = null;
  form: Partial<Hospital> = this.emptyForm();

  async ngOnInit() {
    await this.refresh();
  }

  emptyForm(): Partial<Hospital> {
    return { name: '', type: 'Hospital', waitTime: '0 min', beds: 0, icu: 0, icuTotal: 1, waitType: 'primary', equipment: [] as Equipment[] };
  }

  filteredHospitals() {
    const q = this.search.toLowerCase();
    return this.hospitals.filter((item) => item.name.toLowerCase().includes(q) || item.type.toLowerCase().includes(q));
  }

  async refresh() {
    this.hospitals = await this.hospitalService.getHospitals();
    this.loading = false;
  }

  openForm(hospital?: Hospital) {
    this.editingId = hospital?.id || null;
    this.form = hospital ? { ...hospital } : this.emptyForm();
    this.formOpen = true;
  }

  closeForm() {
    this.formOpen = false;
    this.editingId = null;
    this.form = this.emptyForm();
  }

  async save() {
    if (this.editingId) {
      await this.hospitalService.updateHospital(this.editingId, this.form);
    } else {
      await this.hospitalService.addHospital(this.form);
    }
    this.closeForm();
    await this.refresh();
  }

  async remove(id: string) {
    if (!window.confirm('Remove this hospital?')) return;
    await this.hospitalService.deleteHospital(id);
    await this.refresh();
  }
}



