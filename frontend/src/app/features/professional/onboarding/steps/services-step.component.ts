import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass } from '@angular/common';
import { OfferedServiceForm } from '../../../../shared/models/professional.model';
import { Category } from '../../../../shared/models/category.model';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';
import { suggestionsForCategory } from '../service-name-suggestions';

@Component({
  selector: 'app-services-step',
  standalone: true,
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './services-step.component.html',
})
export class ServicesStepComponent implements OnInit {
  @Input() initialData: OfferedServiceForm[] | null = null;
  @Output() stepCompleted = new EventEmitter<OfferedServiceForm[]>();
  @Output() goBack = new EventEmitter<void>();

  private readonly referenceData = inject(ReferenceDataService);

  form!: FormGroup;
  categories = signal<Category[]>([]);
  categoriesError = signal(false);
  maxServices = 10;

  private fb = new FormBuilder();

  ngOnInit(): void {
    this.loadCategories();

    this.form = this.fb.group({
      services: this.fb.array([]),
    });

    if (this.initialData && this.initialData.length > 0) {
      this.initialData.forEach((service) => this.addService(service));
    } else {
      this.addService();
    }
  }

  get services(): FormArray {
    return this.form.get('services') as FormArray;
  }

  addService(data?: OfferedServiceForm): void {
    if (this.services.length >= this.maxServices) return;

    const group = this.fb.group({
      categoryId: [data?.categoryId ?? null, Validators.required],
      name: [data?.name ?? '', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: [data?.description ?? '', Validators.maxLength(300)],
      price: [data?.price ?? null, [Validators.required, Validators.min(1)]],
      currency: ['PYG'],
    });

    this.services.push(group);
  }

  removeService(index: number): void {
    if (this.services.length > 1) {
      this.services.removeAt(index);
    }
  }

  getNamePlaceholder(index: number): string {
    const suggestions = this.getNameSuggestions(index);
    return suggestions.length > 0 ? `Ej: ${suggestions[0]}` : 'Ej: Instalación de tomacorrientes';
  }

  getNameSuggestions(index: number): string[] {
    const categoryId = this.services.at(index).get('categoryId')?.value;
    const category = this.categories().find((c) => c.id === Number(categoryId));
    return suggestionsForCategory(category?.name);
  }

  private loadCategories(): void {
    this.referenceData.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.categoriesError.set(false);
      },
      error: () => this.categoriesError.set(true),
    });
  }

  onCategoryChange(index: number): void {
    // Clear name when category changes so placeholder updates
    const nameControl = this.services.at(index).get('name');
    if (!nameControl?.value) {
      nameControl?.markAsUntouched();
    }
  }

  formatPrice(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/\D/g, '');
    const numericValue = raw ? parseInt(raw, 10) : null;
    this.services.at(index).get('price')?.setValue(numericValue, { emitEvent: false });
    input.value = numericValue ? numericValue.toLocaleString('es-PY') : '';
  }

  getFormattedPrice(index: number): string {
    const price = this.services.at(index).get('price')?.value;
    return price ? price.toLocaleString('es-PY') : '';
  }

  onSubmit(): void {
    if (this.form.valid) {
      const data: OfferedServiceForm[] = this.services.controls.map((control) => ({
        categoryId: control.get('categoryId')?.value,
        name: control.get('name')?.value,
        description: control.get('description')?.value || '',
        price: control.get('price')?.value,
        currency: 'PYG',
      }));
      this.stepCompleted.emit(data);
    } else {
      this.services.controls.forEach((group) => {
        (group as FormGroup).markAllAsTouched();
      });
    }
  }

  onBack(): void {
    this.goBack.emit();
  }

  hasError(index: number, field: string): boolean {
    const control = this.services.at(index).get(field);
    return !!(control?.invalid && control?.touched);
  }
}
