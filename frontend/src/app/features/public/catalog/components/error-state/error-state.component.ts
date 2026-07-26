import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  templateUrl: './error-state.component.html',
})
export class ErrorStateComponent {
  @Output() retry = new EventEmitter<void>();
}
