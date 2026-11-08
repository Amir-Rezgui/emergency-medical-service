import { Injectable } from '@angular/core';
import { Report } from '../app/models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly mockReports: Report[] = [
    { id: '1', name: 'Monthly_Response_Audit_Oct.pdf', category: 'Operational Analytics', date: 'Oct 31, 2023', status: 'Ready' },
    { id: '2', name: 'Critical_Incident_Review_Q3.xlsx', category: 'Clinical Review', date: 'Oct 28, 2023', status: 'Ready' },
    { id: '3', name: 'Fleet_Maintenance_Log_2023.pdf', category: 'Resources', date: 'Oct 25, 2023', status: 'Archived' },
  ];

  async getReports(): Promise<Report[]> {
    return this.mockReports;
  }
}
