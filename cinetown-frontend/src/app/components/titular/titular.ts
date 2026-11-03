import { Component } from '@angular/core';

@Component({
  selector: 'app-titular',
  standalone: true,
  template: `
    <section class="hero">
      <div class="hero-backdrop" aria-hidden="true">
        <span class="poster poster-one">CINE</span>
        <span class="poster poster-two">NOCHE</span>
        <span class="poster poster-three">HORIZONTE</span>
        <span class="poster poster-four">ESTRENO</span>
        <span class="poster poster-five">FUTURO</span>
      </div>
      <div class="hero-shade"></div>

      <div class="hero-content">
        <h1>Vive el cine. Elige tu próxima historia.</h1>
        <p>Cartelera, sesiones y entradas en un solo lugar.</p>

        <div class="hero-actions">
          <a class="btn-primary" href="#cartelera">Explorar cartelera</a>
          <span class="badge age-badge">+16</span>
          <span class="badge premiere-badge">Estreno hoy</span>
        </div>

        <span class="hero-caption">HOY · 0 películas · 26 sesiones</span>
      </div>
    </section>
  `,
  styleUrl: './titular.scss',
})
export class TitularComponent {}
