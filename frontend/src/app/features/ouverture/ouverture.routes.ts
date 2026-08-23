import { Routes } from '@angular/router';
import { OuvertureRechercheComponent } from './ouverture-recherche.component';
import { OuvertureFormComponent } from './ouverture-form.component';
import { OuvertureDetailComponent } from './ouverture-detail.component';

export const OUVERTURE_ROUTES: Routes = [
  { path: '', component: OuvertureRechercheComponent },
  { path: 'nouveau', component: OuvertureFormComponent },
  { path: ':id/modifier', component: OuvertureFormComponent },
  { path: ':id', component: OuvertureDetailComponent },
];
