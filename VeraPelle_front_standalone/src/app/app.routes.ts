import { Routes }            from '@angular/router';
import { HomeComponent }     from './features/home/home.component';
import { ProfiloComponent }  from './features/profilo/profilo.component';
import { authGuard }         from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '',        component: HomeComponent },
  { path: 'profilo', component: ProfiloComponent, canActivate: [authGuard] },
  { path: '**',      redirectTo: '' }
];
