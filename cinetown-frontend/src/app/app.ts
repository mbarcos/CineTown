import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Landing} from './pages/landing/landing';

@Component({
  imports: [RouterOutlet, Landing],
  selector: 'app-root',
  template: '<app-landing />',
})
export class App {}
