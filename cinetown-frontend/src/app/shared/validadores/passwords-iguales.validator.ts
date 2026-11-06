import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Valida a nivel de grupo que dos campos tengan el mismo valor.
 * Reutilizable en cualquier formulario con contraseña y confirmación.
 */
export function passwordsIguales(
  campoPassword: string,
  campoConfirmacion: string,
): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const password = grupo.get(campoPassword)?.value;
    const confirmacion = grupo.get(campoConfirmacion)?.value;

    if (!password || !confirmacion) {
      return null;
    }

    return password === confirmacion ? null : { passwordsNoCoinciden: true };
  };
}
