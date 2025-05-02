import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="home">
      <h1>Benvenuto in VeraPelle!</h1>
      <p>Questa è la pagina principale dell’app.</p>
    </section>
  `,
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {}
