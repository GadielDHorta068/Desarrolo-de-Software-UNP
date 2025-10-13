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
    path: 'panel',
    loadComponent: () => import('./pages/raffles-panel/raffles-panel').then(m => m.RafflesPanel),
    // canActivate: [GuestGuard],
    title: 'Creación de evento - Raffy',
    // data: { animation: 'register' }
  },
  {
    // path: 'event-edit',
    path: 'event/management/edit/:eventId',
    loadComponent: () => import('./pages/edit-event/edit-event').then(m => m.EditEvent),
    // canActivate: [GuestGuard],
    title: 'Edita tu evento - Raffy',
    // data: { animation: 'register' }
  },
  {
    // path: 'event-admin',
    path: 'event/management/:eventId',
    loadComponent: () => import('./pages/management-event/management-event').then(m => m.ManagementEvent),
    // canActivate: [GuestGuard],
    title: 'Gestiona tu evento - Raffy',
    // data: { animation: 'register' }
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
    path: 'draws',
    children: [
      {
        path: 'all',
        loadComponent: () => import('./pages/public-events/public-events').then(m => m.PublicEvents),
        title: 'Sorteos públicos - Raffy'
      },
      { path: 'edit',
        loadComponent: () => import('./pages/edit-event/edit-event').then(m => m.EditEvent),
        // canActivate: [GuestGuard],
        title: 'Editar sorteo - Raffy'
      },
      {
        path: '**',
        redirectTo: '/all'
      }
    ]
  },
  {
    path: 'raffle/id', // cambiar id por :id 
    loadComponent: () => import('./pages/rifa-front.component/rifa-front.component').then(m => m.RifaFrontComponent),
    title: 'raffle-detail'
  },
  {
    path: 'winners/:eventId',
    loadComponent: () => import('./pages/winners/winners-wheel').then(m => m.WinnersWheel),
    title: 'Ganadores - Raffy'
  },
  {
    path: '**',
    redirectTo: '/home'
  }
];
