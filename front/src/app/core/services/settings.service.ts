import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, effect, inject, signal } from '@angular/core';

export type Theme = 'light' | 'dark' | 'system';
export type Language = 'en' | 'fr' | 'ar';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly document = inject(DOCUMENT);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  readonly theme = signal<Theme>(this.readStorage('theme', 'light') as Theme);
  readonly language = signal<Language>(this.readStorage('language', 'en') as Language);
  readonly audioEnabled = signal(this.readStorage('audioEnabled', 'true') !== 'false');
  readonly audioVolume = signal(Number(this.readStorage('audioVolume', '0.5')));
  readonly realtimeSync = signal(true);
  readonly predictiveAnalytics = signal(true);
  readonly auditLogging = signal(false);

  constructor() {
    effect(() => {
      const theme = this.theme();
      if (!this.isBrowser) {
        return;
      }
      localStorage.setItem('theme', theme);
      const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
      const dark = theme === 'dark' || (theme === 'system' && prefersDark);
      this.document.documentElement.classList.toggle('dark', dark);
    });

    effect(() => {
      if (!this.isBrowser) {
        return;
      }
      localStorage.setItem('language', this.language());
      localStorage.setItem('audioEnabled', String(this.audioEnabled()));
      localStorage.setItem('audioVolume', String(this.audioVolume()));
    });
  }

  private readStorage(key: string, fallback: string) {
    if (!this.isBrowser) {
      return fallback;
    }
    return localStorage.getItem(key) || fallback;
  }

  setTheme(theme: Theme) {
    this.theme.set(theme);
  }

  setLanguage(language: Language) {
    this.language.set(language);
  }

  setAudioEnabled(enabled: boolean) {
    this.audioEnabled.set(enabled);
  }

  setAudioVolume(volume: number) {
    this.audioVolume.set(volume);
  }
}
