import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * A custom validator that checks if two fields in a form group have the same value.
 * @param controlName The name of the first control.
 * @param matchingControlName The name of the control to match against.
 * @returns A ValidatorFn.
 */
export function matchPasswordValidator(controlName: string, matchingControlName: string): ValidatorFn {
  return (formGroup: AbstractControl): ValidationErrors | null => {
    const control = formGroup.get(controlName);
    const matchingControl = formGroup.get(matchingControlName);

    if (!control || !matchingControl) {
      return null;
    }

    if (matchingControl.errors && !matchingControl.errors['mustMatch']) {
      return null;
    }

    if (control.value !== matchingControl.value) {
      matchingControl.setErrors({ mustMatch: true });
      return { mustMatch: true };
    } else {
      matchingControl.setErrors(null);
      return null;
    }
  };
}
