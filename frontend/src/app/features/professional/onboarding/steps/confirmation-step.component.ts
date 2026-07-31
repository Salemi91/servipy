import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OfferedServiceForm, ProfessionalProfileForm } from '../../../../shared/models/professional.model';
import { Category } from '../../../../shared/models/category.model';
import { City } from '../../../../shared/models/city.model';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';

@Component({
  selector: 'app-confirmation-step',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './confirmation-step.component.html',
})
export class ConfirmationStepComponent implements OnInit {
  @Input({ required: true }) profileData!: ProfessionalProfileForm;
  @Input({ required: true }) servicesData!: OfferedServiceForm[];
  @Output() goBack = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<void>();

  private readonly referenceData = inject(ReferenceDataService);

  isSubmitted = false;

  private readonly cities = signal<City[]>([]);
  private readonly categories = signal<Category[]>([]);

  ngOnInit(): void {
    this.referenceData.getCities().subscribe((cities) => this.cities.set(cities));
    this.referenceData.getCategories().subscribe((categories) => this.categories.set(categories));
  }

  getCityName(cityId: string): string {
    const city = this.cities().find((c) => c.id === Number(cityId));
    return city?.name ?? 'No especificada';
  }

  getCategoryName(categoryId: number | null): string {
    if (!categoryId) return 'Sin categoría';
    const category = this.categories().find((c) => c.id === categoryId);
    return category ? `${category.icon} ${category.name}` : 'Sin categoría';
  }

  formatPrice(price: number | null): string {
    if (!price) return 'Gs. 0';
    return `Gs. ${price.toLocaleString('es-PY')}`;
  }

  onConfirm(): void {
    this.isSubmitted = true;
    this.confirmed.emit();
  }

  onBack(): void {
    this.goBack.emit();
  }
}
