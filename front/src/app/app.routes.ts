import { Routes } from '@angular/router';
import { AmbulanceManagementPageComponent } from './pages/ambulance-management-page/ambulance-management-page.component';
import { DashboardPageComponent } from './pages/dashboard-page/dashboard-page.component';
import { HospitalManagementPageComponent } from './pages/hospital-management-page/hospital-management-page.component';
import { IncidentHistoryPageComponent } from './pages/incident-history-page/incident-history-page.component';
import { IncidentDetailsPageComponent } from './pages/incident-details-page/incident-details-page.component';
import { LoginPageComponent } from './pages/login-page/login-page.component';
import { ProfilePageComponent } from './pages/profile-page/profile-page.component';
import { ReportsPageComponent } from './pages/reports-page/reports-page.component';
import { ResourceManagementPageComponent } from './pages/resource-management-page/resource-management-page.component';
import { SettingsPageComponent } from './pages/settings-page/settings-page.component';
import { UserManagementPageComponent } from './pages/user-management-page/user-management-page.component';
import { ShellComponent } from './shell/shell.component';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', pathMatch: 'full', component: DashboardPageComponent },
      { path: 'incidents', component: IncidentHistoryPageComponent },
      { path: 'incidents/:id', component: IncidentDetailsPageComponent },
      { path: 'users', component: UserManagementPageComponent },
      { path: 'hospitals', component: HospitalManagementPageComponent },
      { path: 'ambulances', component: AmbulanceManagementPageComponent },
      { path: 'resources', component: ResourceManagementPageComponent },
      { path: 'reports', component: ReportsPageComponent },
      { path: 'settings', component: SettingsPageComponent },
      { path: 'profile', component: ProfilePageComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];

