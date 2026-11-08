import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { AnalyticsService } from '../../../services/analytics.service';
import { ReportService } from '../../../services/report.service';
import { ResponseData } from '../../models/analytics.model';
import { Report } from '../../models/report.model';

@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports-page.component.html',
  styleUrl: './reports-page.component.css',
})
export class ReportsPageComponent implements OnInit {
  private readonly reportService = inject(ReportService);
  private readonly analyticsService = inject(AnalyticsService);

  loading = true;
  reports: Report[] = [];
  responseData: ResponseData[] = [];

  async ngOnInit() {
    const [reports, responseData] = await Promise.all([
      this.reportService.getReports(),
      this.analyticsService.getResponseData(),
    ]);
    this.reports = reports;
    this.responseData = responseData;
    this.loading = false;
  }

  averageArrival() {
    if (!this.responseData.length) return '0';
    return Math.round(this.responseData.reduce((sum, item) => sum + item.arrival, 0) / this.responseData.length);
  }

  averageDispatch() {
    if (!this.responseData.length) return '0';
    return Math.round(this.responseData.reduce((sum, item) => sum + item.dispatch, 0) / this.responseData.length);
  }
}


