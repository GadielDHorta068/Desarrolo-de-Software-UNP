import { Routes } from '@angular/router';
import { AuthGuard, GuestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent),
    title: 'Inicio - Raffy'
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then(m => m.Login),
    canActivate: [GuestGuard],
    title: 'Iniciar Sesión - Raffy',
    data: { animation: 'login' }
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then(m => m.Register),
    canActivate: [GuestGuard],
    title: 'Registrarse - Raffy',
    data: { animation: 'register' }
  },
  {
    path: 'settings',
    loadComponent: () => import('./pages/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [AuthGuard],
    title: 'Configuración - Raffy'
  },
  {
    path: 'panel',
    loadComponent: () => import('./pages/raffles-panel/raffles-panel').then(m => m.RafflesPanel),
    // canActivate: [GuestGuard],
    title: 'Panel - Raffy',
    // data: { animation: 'register' }
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
