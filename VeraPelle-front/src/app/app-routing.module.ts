import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ProfiloComponent } from './pages/profilo/profilo.component';
import { authGuard } from './core/services/guard/auth.guard'; // percorso corretto al tuo guard

const routes: Routes = [
  { path: 'profilo', component: ProfiloComponent, canActivate: [authGuard] },
  //{ path: '', component: HomeComponent }, // rotta di default
  // Se in futuro creerai login o register, aggiungi qui i percorsi
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
