import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './empty-state.component.html',
})
export class EmptyStateComponent {
  @Input() hasFilters = false;
  @Output() clearFilters = new EventEmitter<void>();
}
