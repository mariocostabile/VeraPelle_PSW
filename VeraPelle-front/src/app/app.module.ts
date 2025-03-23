import {APP_INITIALIZER, NgModule} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {HTTP_INTERCEPTORS, HttpClient} from '@angular/common/http';
import {HttpTokenInterceptor} from './core/services/interceptor/http-token.interceptor';
import {KeycloakService} from './core/services/keycloak/keycloak.service';
import { HomeComponent } from './pages/home/home.component';
import { HeaderComponent } from './core/component/header/header.component';
import { ProfiloComponent } from './pages/profilo/profilo.component';
import { FooterComponent } from './core/component/footer/footer.component';

export function kcFactory(kcService: KeycloakService){
  return () => kcService.init();
}

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    HeaderComponent,
    ProfiloComponent,
    FooterComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [
    HttpClient,{
      provide: HTTP_INTERCEPTORS,
      useClass: HttpTokenInterceptor,
      multi: true
    },
    {
      provide: APP_INITIALIZER,
      deps: [KeycloakService],
      useFactory: kcFactory,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
