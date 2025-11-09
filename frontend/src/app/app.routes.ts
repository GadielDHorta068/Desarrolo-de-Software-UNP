import { Routes } from '@angular/router';
import { AuthGuard, GuestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'invite/:shortcode',
    loadComponent: () => import('./pages/invite/invite.component').then(m => m.InviteRedirectComponent),
    title: 'Invitación - Raffy'
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
    path: 'event/new',
    loadComponent: () => import('./pages/raffles-panel/raffles-panel').then(m => m.RafflesPanel),
    // canActivate: [GuestGuard],
    title: 'Creación de evento - Raffy',
    // data: { animation: 'register' }
  },
  {
    path: 'event/management/edit/:eventId',
    loadComponent: () => import('./pages/edit-event/edit-event').then(m => m.EditEvent),
    // canActivate: [GuestGuard],
    title: 'Edita tu evento - Raffy',
  },
  {
    path: 'event/management/:eventId',
    loadComponent: () => import('./pages/management-event/management-event').then(m => m.ManagementEvent),
    // canActivate: [GuestGuard],
    title: 'Gestiona tu evento - Raffy',
  },
  {
    path: 'event/payment',
    loadComponent: () => import('./pages/payments/payments').then(m => m.Payments),
    // canActivate: [GuestGuard],
    title: 'Pago de inscripción - Raffy',
  },
  {
    path: 'panel-list',
    loadComponent: () => import('./pages/panel-list/panel-list').then(m => m.PanelList),
    // canActivate: [GuestGuard],
    title: 'Lista de sorteos - Raffy',
    // data: { animation: 'register' }
  },
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile').then(m => m.Profile),
    canActivate: [AuthGuard],
    title: 'Perfil - Raffy'
  },
  {
    path: 'profile/:nickname',
    loadComponent: () => import('./pages/profile/profile').then(m => m.Profile),
    title: 'Perfil - Raffy'
  },
  {
    path: 'chat/:userId',
    loadComponent: () => import('./pages/chat/chat.component').then(m => m.ChatComponent),
    canActivate: [AuthGuard],
    title: 'Chat privado - Raffy'
  },
  {
    path: 'event',
    loadComponent: () => import('./pages/event/event').then(m => m.Event),
    canActivate: [GuestGuard],
    title: 'Eventos - Raffy',
    data: {animation: 'event'}
  },
  {
    path: 'public-events',
    loadComponent: () => import('./pages/public-events/public-events').then(m => m.PublicEvents),
    title: 'HUB de eventos - Raffy'
  },
  {
    path: 'events/reported',
    loadComponent: () => import('./pages/reported-events/reported-events').then(m => m.ReportedEvents),
    title: 'Eventos reportados - Raffy'
  },
  {
    path: 'event/:eventId/reports',
    loadComponent: () => import('./pages/reports/reports').then(m => m.Reports),
    title: 'Lista de reportes - Raffy'
  },
  {
    path: 'winners/:eventId',
    loadComponent: () => import('./pages/winners/winners-wheel').then(m => m.WinnersWheel),
    title: 'Ganadores - Raffy'
  },
  {
    path: 'audit',
    loadComponent: () => import('./pages/audit/audit-list/audit-list').then(m => m.AuditList),
    canActivate: [AuthGuard],
    title: 'Auditorías - Raffy'
  },
  {
    path: 'audit/:eventId',
    loadComponent: () => import('./pages/audit/audit-detail/audit-detail').then(m => m.AuditDetail),
    canActivate: [AuthGuard],
    title: 'Detalle de Auditoría - Raffy'
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
