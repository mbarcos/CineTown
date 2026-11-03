import { Component } from '@angular/core';

@Component({
  selector: 'app-header',
  standalone: true,
  template: `
    <header class="site-header">
      <a class="logo" href="/" aria-label="CineTown, inicio">CINETOWN</a>

      <nav class="main-nav" aria-label="Navegación principal">
        <a class="active" href="#cartelera">Cartelera</a>
        <a href="#mis-compras">Mis compras</a>
        <a href="#ayuda">Ayuda</a>
      </nav>

      <a class="account-link" href="#mi-cuenta">Mi cuenta</a>
    </header>
  `,
  styleUrl: './header.scss',
})
export class HeaderComponent {}
