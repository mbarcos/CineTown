import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  Validators,
  ReactiveFormsModule
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../services/authService';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './login.html'
})
export class Login {

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  enviando = signal(false);
  mensajeError = signal<string | null>(null);

  // Formulario
  formulario = this.fb.nonNullable.group({
    nombreUsuario: [
      '',
      [
        Validators.required,
        Validators.maxLength(50)
      ]
    ],
    password: [
      '',
      [
        Validators.required
      ]
    ]
  });

  iniciarSesion(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.mensajeError.set(null);
    this.authService.login(this.formulario.getRawValue()).subscribe({
      next: () => {

        this.enviando.set(false);

        // Volver a la página que intentaba visitar el usuario
        const returnUrl = this.route.snapshot
          .queryParamMap.get('returnUrl');

        const destino =
          returnUrl?.startsWith('/') &&
          !returnUrl.startsWith('//')
            ? returnUrl
            : '/';

        this.router.navigateByUrl(destino);

      },

      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);

        if (error.status === 401) {
          this.mensajeError.set(
            'Nombre de usuario o contraseña incorrectos'
          );

        } else {
          this.mensajeError.set(
            'Ha ocurrido un error al iniciar sesión'
          );
        }
      }
    });

  }

}
