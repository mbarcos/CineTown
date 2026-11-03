import { Component } from '@angular/core';
import { Landing } from './pages/landing/landing';
import { HeaderComponent } from './components/header/header';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Landing, HeaderComponent],
  template: `
    <app-header />
    <app-landing />
  `,
})
export class App {}
