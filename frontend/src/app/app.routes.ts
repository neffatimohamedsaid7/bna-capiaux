import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'souscription',
    canActivate: [authGuard],
    loadChildren: () => import('./features/souscription/souscription.routes').then((m) => m.SOUSCRIPTION_ROUTES),
  },
  {
    path: 'rachat',
    canActivate: [authGuard],
    loadChildren: () => import('./features/rachat/rachat.routes').then((m) => m.RACHAT_ROUTES),
  },
  {
    path: 'ouverture',
    canActivate: [authGuard],
    loadChildren: () => import('./features/ouverture/ouverture.routes').then((m) => m.OUVERTURE_ROUTES),
  },
  {
    path: 'consultation',
    canActivate: [authGuard],
    loadComponent: () => import('./features/consultation/consultation.component').then((m) => m.ConsultationComponent),
  },
  {
    path: 'admin/utilisateurs',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/utilisateurs.component').then((m) => m.UtilisateursComponent),
  },
  { path: '**', redirectTo: '' },
];
