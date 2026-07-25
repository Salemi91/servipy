import { Component } from '@angular/core';

@Component({
  selector: 'app-login-placeholder',
  standalone: true,
  template: `
    <div class="flex flex-col items-center py-16">
      <h2 class="text-2xl font-semibold text-gray-800">Iniciar sesión</h2>
      <p class="mt-2 text-gray-500">Placeholder — se implementará con la spec de autenticación.</p>
    </div>
  `,
})
export class LoginPlaceholderComponent {}
