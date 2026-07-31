import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, concatMap, from, of, toArray } from 'rxjs';
import { ProgressBarComponent } from './components/progress-bar.component';
import { ProfileStepComponent } from './steps/profile-step.component';
import { ServicesStepComponent } from './steps/services-step.component';
import { ConfirmationStepComponent } from './steps/confirmation-step.component';
import { OfferedServiceForm, ProfessionalProfileForm } from '../../../shared/models/professional.model';
import { ProfessionalProfileApiService } from '../services/professional-profile.service';

@Component({
  selector: 'app-onboarding-wizard',
  standalone: true,
  imports: [
    ProgressBarComponent,
    ProfileStepComponent,
    ServicesStepComponent,
    ConfirmationStepComponent,
  ],
  templateUrl: './onboarding-wizard.component.html',
})
export class OnboardingWizardComponent {
  private readonly profileApi = inject(ProfessionalProfileApiService);
  private readonly router = inject(Router);

  currentStep = 1;
  totalSteps = 3;

  profileData: ProfessionalProfileForm | null = null;
  servicesData: OfferedServiceForm[] | null = null;

  submitting = signal(false);
  submitError = signal<string | null>(null);

  onProfileCompleted(data: ProfessionalProfileForm): void {
    this.profileData = data;
    this.goToStep(2);
  }

  onServicesCompleted(data: OfferedServiceForm[]): void {
    this.servicesData = data;
    this.goToStep(3);
  }

  onGoBackToProfile(): void {
    this.goToStep(1);
  }

  onGoBackToServices(): void {
    this.goToStep(2);
  }

  /**
   * Persiste el perfil y, a continuación, el tarifario recopilado en el paso 2.
   * Sin servicios activos el profesional no aparece en el catálogo, por lo que
   * ambos pasos forman una sola operación desde la perspectiva del usuario.
   */
  onConfirmed(): void {
    if (!this.profileData) return;

    this.submitting.set(true);
    this.submitError.set(null);

    this.profileApi
      .createProfile(this.profileData)
      .pipe(concatMap(() => this.persistServices()))
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.router.navigate(['/professional']);
        },
        error: (err) => {
          this.submitting.set(false);
          const message = err?.error?.message || 'Error al crear el perfil. Intente nuevamente.';
          this.submitError.set(message);
        },
      });
  }

  private persistServices(): Observable<unknown> {
    const services = this.servicesData ?? [];
    if (services.length === 0) {
      return of(null);
    }

    // Secuencial: el backend valida cada servicio y el primer error aborta el resto.
    return from(services).pipe(
      concatMap((service) => this.profileApi.createService(service)),
      toArray()
    );
  }

  private goToStep(step: number): void {
    this.currentStep = step;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
