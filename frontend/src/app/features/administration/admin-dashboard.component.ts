import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="min-h-screen bg-gray-50">
      <div class="border-b border-gray-200 bg-white">
        <nav class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div class="flex h-14 items-center space-x-8">
            <h1 class="text-lg font-semibold text-gray-800">Admin</h1>
            <a routerLink="categories" routerLinkActive="text-indigo-600 border-b-2 border-indigo-600"
              class="px-1 py-4 text-sm font-medium text-gray-500 hover:text-gray-700">
              Categorías
            </a>
            <a routerLink="professionals" routerLinkActive="text-indigo-600 border-b-2 border-indigo-600"
              class="px-1 py-4 text-sm font-medium text-gray-500 hover:text-gray-700">
              Profesionales
            </a>
          </div>
        </nav>
      </div>
      <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-6">
        <router-outlet />
      </div>
    </div>
  `,
})
export class AdminDashboardComponent {}
