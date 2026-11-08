import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Language, SettingsService, Theme } from '../../core/services/settings.service';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.css',
})
export class SettingsPageComponent {
  readonly settings = inject(SettingsService);
  readonly tabs = ['General', 'Notifications', 'Operations'];
  readonly themes: Theme[] = ['light', 'dark', 'system'];
  readonly languages: { label: string; value: Language }[] = [
    { label: 'English (United States)', value: 'en' },
    { label: 'Francais (Tunisie)', value: 'fr' },
    { label: 'Arabic (Tunisia)', value: 'ar' },
  ];
  activeTab = 'General';
}


