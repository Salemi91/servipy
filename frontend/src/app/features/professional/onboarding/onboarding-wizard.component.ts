import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
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

  onConfirmed(): void {
    if (!this.profileData) return;

    this.submitting.set(true);
    this.submitError.set(null);

    this.profileApi.createProfile(this.profileData).subscribe({
      next: () => {
        this.submitting.set(false);
        // Profile created successfully — redirect to professional dashboard
        this.router.navigate(['/professional']);
      },
      error: (err) => {
        this.submitting.set(false);
        const message = err?.error?.message || 'Error al crear el perfil. Intente nuevamente.';
        this.submitError.set(message);
      },
    });
  }

  private goToStep(step: number): void {
    this.currentStep = step;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
