import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-progress-bar',
  standalone: true,
  imports: [NgClass],
  templateUrl: './progress-bar.component.html',
})
export class ProgressBarComponent {
  @Input({ required: true }) currentStep!: number;
  @Input({ required: true }) totalSteps!: number;

  steps = [
    { number: 1, label: 'Perfil' },
    { number: 2, label: 'Servicios' },
    { number: 3, label: 'Listo' },
  ];

  isCompleted(stepNumber: number): boolean {
    return stepNumber < this.currentStep;
  }

  isCurrent(stepNumber: number): boolean {
    return stepNumber === this.currentStep;
  }
}
