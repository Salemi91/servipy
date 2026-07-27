import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="max-w-4xl mx-auto py-6 px-4">
      <!-- Navigation Tabs -->
      <nav class="flex border-b border-gray-200 mb-6">
        <a
          routerLink="profile"
          routerLinkActive="border-blue-600 text-blue-600"
          [routerLinkActiveOptions]="{ exact: true }"
          class="px-4 py-3 text-sm font-medium border-b-2 border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 transition-colors"
        >
          Mi Perfil
        </a>
        <a
          routerLink="requests"
          routerLinkActive="border-blue-600 text-blue-600"
          [routerLinkActiveOptions]="{ exact: false }"
          class="px-4 py-3 text-sm font-medium border-b-2 border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 transition-colors"
        >
          Mis Solicitudes
        </a>
      </nav>

      <!-- Page content -->
      <router-outlet />
    </div>
  `,
})
export class ClientLayoutComponent {}
