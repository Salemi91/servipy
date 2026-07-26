import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OfferedServiceForm, ProfessionalProfileForm } from '../../../../shared/models/professional.model';
import { MOCK_CATEGORIES, MOCK_CITIES } from '../mock-data';

@Component({
  selector: 'app-confirmation-step',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './confirmation-step.component.html',
})
export class ConfirmationStepComponent {
  @Input({ required: true }) profileData!: ProfessionalProfileForm;
  @Input({ required: true }) servicesData!: OfferedServiceForm[];
  @Output() goBack = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<void>();

  isSubmitted = false;

  getCityName(cityId: string): string {
    const city = MOCK_CITIES.find((c) => c.id === Number(cityId));
    return city?.name ?? 'No especificada';
  }

  getCategoryName(categoryId: number | null): string {
    if (!categoryId) return 'Sin categoría';
    const category = MOCK_CATEGORIES.find((c) => c.id === categoryId);
    return category ? `${category.icon} ${category.name}` : 'Sin categoría';
  }

  formatPrice(price: number | null): string {
    if (!price) return 'Gs. 0';
    return `Gs. ${price.toLocaleString('es-PY')}`;
  }

  onConfirm(): void {
    // Log the payload that would be sent to the backend
    const payload = {
      profile: this.profileData,
      services: this.servicesData,
    };
    console.log('[ServiPy] Onboarding payload:', payload);
    this.isSubmitted = true;
    this.confirmed.emit();
  }

  onBack(): void {
    this.goBack.emit();
  }
}
