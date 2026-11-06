import { Component, inject } from '@angular/core';
import { AuthService } from '../../services/authService';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.html',
  imports: [
    RouterLink
  ],
  styleUrl: './header.scss',
})
export class HeaderComponent {
  authService = inject(AuthService);
}
