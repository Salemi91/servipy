import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProfessionalSummary } from '../../../../../shared/models/professional.model';

@Component({
  selector: 'app-professional-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './professional-card.component.html',
})
export class ProfessionalCardComponent {
  @Input({ required: true }) professional!: ProfessionalSummary;
}
