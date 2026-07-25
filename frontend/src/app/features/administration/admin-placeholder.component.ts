import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-placeholder',
  standalone: true,
  template: `
    <div class="flex flex-col items-center py-16">
      <h2 class="text-2xl font-semibold text-gray-800">Panel Administración</h2>
      <p class="mt-2 text-gray-500">Placeholder — requiere autenticación con rol ADMIN.</p>
    </div>
  `,
})
export class AdminPlaceholderComponent {}
