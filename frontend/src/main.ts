import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Polyfill para librerías que esperan 'global' en navegador
(window as any).global = window as any;

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
