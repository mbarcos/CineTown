import { Component } from '@angular/core';
import { Landing } from './pages/landing/landing';
import { HeaderComponent } from './components/header/header';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  template: `
    <app-header />
    <router-outlet />
  `,
})
export class App {}
