import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass } from '@angular/common';
import { CityOption, ProfessionalProfileForm } from '../../../../shared/models/professional.model';
import { MOCK_CITIES } from '../mock-data';

@Component({
  selector: 'app-profile-step',
  standalone: true,
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './profile-step.component.html',
})
export class ProfileStepComponent implements OnInit {
  @Input() initialData: ProfessionalProfileForm | null = null;
  @Output() stepCompleted = new EventEmitter<ProfessionalProfileForm>();

  form!: FormGroup;
  cities: CityOption[] = MOCK_CITIES;
  sameWhatsapp = false;

  private fb = new FormBuilder();

  ngOnInit(): void {
    this.form = this.fb.group({
      phone: [
        this.initialData?.phone ?? '',
        [Validators.required, Validators.pattern(/^\d{4}\s?\d{3}\s?\d{3}$/)],
      ],
      whatsapp: [this.initialData?.whatsapp ?? ''],
      city: [this.initialData?.city ?? '', Validators.required],
      description: [
        this.initialData?.description ?? '',
        [Validators.required, Validators.minLength(10), Validators.maxLength(500)],
      ],
    });

    // If initial data had same phone/whatsapp, activate toggle
    if (this.initialData && this.initialData.phone && this.initialData.phone === this.initialData.whatsapp) {
      this.sameWhatsapp = true;
    }
  }

  toggleSameWhatsapp(): void {
    this.sameWhatsapp = !this.sameWhatsapp;
    if (this.sameWhatsapp) {
      this.form.get('whatsapp')?.setValue(this.form.get('phone')?.value);
    } else {
      this.form.get('whatsapp')?.setValue('');
    }
  }

  onPhoneChange(): void {
    if (this.sameWhatsapp) {
      this.form.get('whatsapp')?.setValue(this.form.get('phone')?.value);
    }
  }

  get descriptionLength(): number {
    return this.form.get('description')?.value?.length ?? 0;
  }

  onSubmit(): void {
    if (this.form.valid) {
      const data: ProfessionalProfileForm = {
        phone: this.form.value.phone,
        whatsapp: this.sameWhatsapp ? this.form.value.phone : this.form.value.whatsapp,
        city: this.form.value.city,
        description: this.form.value.description,
        photoUrl: null,
      };
      this.stepCompleted.emit(data);
    } else {
      this.form.markAllAsTouched();
    }
  }

  hasError(field: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched);
  }
}
