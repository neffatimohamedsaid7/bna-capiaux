import { Routes } from '@angular/router';
import { SouscriptionRechercheComponent } from './souscription-recherche.component';
import { SouscriptionFormComponent } from './souscription-form.component';
import { SouscriptionDetailComponent } from './souscription-detail.component';

export const SOUSCRIPTION_ROUTES: Routes = [
  { path: '', component: SouscriptionRechercheComponent },
  { path: 'nouveau', component: SouscriptionFormComponent },
  { path: ':id/modifier', component: SouscriptionFormComponent },
  { path: ':id', component: SouscriptionDetailComponent },
];
