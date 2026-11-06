import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/authService';
import { passwordsIguales } from '../../shared/validadores/passwords-iguales.validator';

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: '../../shared/auth/auth-form.scss',
})
export class RegistroComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly enviando = signal(false);
  readonly mensajeError = signal<string | null>(null);

  // Mismas restricciones que RegistroRequest del backend
  readonly formulario = this.fb.nonNullable.group(
    {
      nombreUsuario: ['', [Validators.required, Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmarPassword: ['', Validators.required],
    },
    { validators: passwordsIguales('password', 'confirmarPassword') },
  );

  registrarse(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.mensajeError.set(null);

    // confirmarPassword es solo de interfaz: no se envía al backend
    const { nombreUsuario, password } = this.formulario.getRawValue();

    this.authService.registrar({ nombreUsuario, password }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err: HttpErrorResponse) => {
        this.mensajeError.set(this.extraerMensaje(err));
        this.enviando.set(false);
      },
    });
  }

  private extraerMensaje(err: HttpErrorResponse): string {
    return err.error?.error ?? 'No se pudo completar el registro. Inténtalo de nuevo.';
  }
}
