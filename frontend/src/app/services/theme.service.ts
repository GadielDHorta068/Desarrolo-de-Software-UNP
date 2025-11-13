import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type Theme = 'light' | 'dark' | 'system';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'app-theme';
  private themeSubject = new BehaviorSubject<Theme>('system');
  private isDarkSubject = new BehaviorSubject<boolean>(false);

  constructor() {
    this.initializeTheme();
    this.setupSystemThemeListener();
  }

  get theme$(): Observable<Theme> {
    return this.themeSubject.asObservable();
  }

  get isDark$(): Observable<boolean> {
    return this.isDarkSubject.asObservable();
  }

  get currentTheme(): Theme {
    return this.themeSubject.value;
  }

  get isDark(): boolean {
    return this.isDarkSubject.value;
  }

  setTheme(theme: Theme): void {
    this.themeSubject.next(theme);
    localStorage.setItem(this.THEME_KEY, theme);
    this.applyTheme(theme);
  }

  toggleTheme(): void {
    const currentTheme = this.currentTheme;
    if (currentTheme === 'light') {
      this.setTheme('dark');
    } else if (currentTheme === 'dark') {
      this.setTheme('system');
    } else {
      this.setTheme('light');
    }
  }

  private initializeTheme(): void {
    const savedTheme = localStorage.getItem(this.THEME_KEY) as Theme;
    const theme = savedTheme || 'system';
    this.themeSubject.next(theme);
    this.applyTheme(theme);
  }

  private setupSystemThemeListener(): void {
    if (typeof window !== 'undefined' && window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      
      const handleChange = () => {
        if (this.currentTheme === 'system') {
          this.updateDarkMode(mediaQuery.matches);
        }
      };

      mediaQuery.addEventListener('change', handleChange);
      handleChange(); // Initial check
    }
  }

  private applyTheme(theme: Theme): void {
    if (typeof document === 'undefined') return;

    const root = document.documentElement;
    // Siempre controlar la clase 'dark' para Tailwind
    // Eliminamos cualquier clase previa que no sea necesaria
    root.classList.remove('light');

    if (theme === 'system') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      root.classList.toggle('dark', prefersDark);
      this.updateDarkMode(prefersDark);
    } else if (theme === 'dark') {
      root.classList.add('dark');
      this.updateDarkMode(true);
    } else {
      // light
      root.classList.remove('dark');
      this.updateDarkMode(false);
    }
  }

  private updateDarkMode(isDark: boolean): void {
    this.isDarkSubject.next(isDark);
  }
}