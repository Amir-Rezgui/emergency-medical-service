import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { PersonnelService } from '../../../services/personnel.service';
import { Personnel } from '../../models/personnel.model';

@Component({
  selector: 'app-user-management-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-management-page.component.html',
  styleUrl: './user-management-page.component.css',
})
export class UserManagementPageComponent implements OnInit {
  private readonly personnelService = inject(PersonnelService);

  loading = true;
  personnel: Personnel[] = [];

  async ngOnInit() {
    this.personnel = await this.personnelService.getPersonnel();
    this.loading = false;
  }

  summaryCards() {
    const active = this.personnel.filter((person) => person.statusType === 'success').length;
    const urgent = this.personnel.filter((person) => person.statusType === 'urgent').length;
    return [
      { label: 'Total Personnel', value: this.personnel.length },
      { label: 'Active Now', value: active },
      { label: 'On Critical Calls', value: urgent },
      { label: 'Coverage', value: this.personnel.length ? `${Math.round((active / this.personnel.length) * 100)}%` : '0%' },
    ];
  }
}


