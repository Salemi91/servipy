import { Component } from '@angular/core';
import { ProgressBarComponent } from './components/progress-bar.component';
import { ProfileStepComponent } from './steps/profile-step.component';
import { ServicesStepComponent } from './steps/services-step.component';
import { ConfirmationStepComponent } from './steps/confirmation-step.component';
import { OfferedServiceForm, ProfessionalProfileForm } from '../../../shared/models/professional.model';

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
  currentStep = 1;
  totalSteps = 3;

  profileData: ProfessionalProfileForm | null = null;
  servicesData: OfferedServiceForm[] | null = null;

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
    // Wizard complete — the confirmation component handles the success state internally
  }

  private goToStep(step: number): void {
    this.currentStep = step;
    // Scroll to top on step change
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
