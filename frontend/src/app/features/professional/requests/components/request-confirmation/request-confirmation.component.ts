import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-request-confirmation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './request-confirmation.component.html',
})
export class RequestConfirmationComponent {
  requestId = input.required<number>();
}
