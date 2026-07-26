import { Component, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ServiceRequestService } from '../../../../../professional/requests/services/service-request.service';
import { CreateServiceRequestPayload } from '../../../../../../shared/models/service-request.model';
import { ErrorResponse } from '../../../../../../shared/models/api-response.model';

type FormState = 'form' | 'submitting' | 'success' | 'error';

@Component({
  selector: 'app-service-request-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './service-request-form.component.html',
})
export class ServiceRequestFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly serviceRequestService = inject(ServiceRequestService);

  professionalId = input.required<number>();

  state = signal<FormState>('form');
  createdRequestId = signal<number | null>(null);
  serverErrorMessage = signal<string | null>(null);

  requestForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    subject: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', [Validators.required, Validators.maxLength(2000)]],
    desiredDate: [''],
  });

  get isSubmitting(): boolean {
    return this.state() === 'submitting';
  }

  onSubmit(): void {
    if (this.requestForm.invalid || this.isSubmitting) {
      this.requestForm.markAllAsTouched();
      return;
    }

    this.state.set('submitting');
    this.serverErrorMessage.set(null);

    const formValue = this.requestForm.value;
    const payload: CreateServiceRequestPayload = {
      name: formValue.name,
      email: formValue.email,
      subject: formValue.subject,
      description: formValue.description,
    };

    if (formValue.phone?.trim()) {
      payload.phone = formValue.phone.trim();
    }

    if (formValue.desiredDate) {
      payload.desiredDate = formValue.desiredDate;
    }

    this.serviceRequestService
      .create(this.professionalId(), payload)
      .subscribe({
        next: (response) => {
          this.createdRequestId.set(response.id);
          this.state.set('success');
        },
        error: (err: HttpErrorResponse) => {
          this.handleError(err);
        },
      });
  }

  resetForm(): void {
    this.requestForm.reset();
    this.state.set('form');
    this.serverErrorMessage.set(null);
    this.createdRequestId.set(null);
  }

  hasError(field: string, error: string): boolean {
    const control = this.requestForm.get(field);
    return !!control && control.hasError(error) && control.touched;
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === 400 && err.error?.errors?.length) {
      const errorResponse = err.error as ErrorResponse;
      errorResponse.errors.forEach((fieldError) => {
        const control = this.requestForm.get(fieldError.field);
        if (control) {
          control.setErrors({ serverError: fieldError.message });
        }
      });
      this.state.set('form');
    } else if (err.status === 404) {
      this.serverErrorMessage.set('Profesional no disponible');
      this.state.set('error');
    } else {
      this.serverErrorMessage.set(
        'Ocurrió un error inesperado. Intente nuevamente.'
      );
      this.state.set('error');
    }
  }
}
