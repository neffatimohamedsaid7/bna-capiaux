import { Routes } from '@angular/router';
import { RachatRechercheComponent } from './rachat-recherche.component';
import { RachatFormComponent } from './rachat-form.component';
import { RachatDetailComponent } from './rachat-detail.component';

export const RACHAT_ROUTES: Routes = [
  { path: '', component: RachatRechercheComponent },
  { path: 'nouveau', component: RachatFormComponent },
  { path: ':id/modifier', component: RachatFormComponent },
  { path: ':id', component: RachatDetailComponent },
];
