import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { CatalogService } from '../services/catalog.service';
import { ProfessionalDetail } from '../../../../shared/models/professional.model';
import { ErrorStateComponent } from '../components/error-state/error-state.component';
import { LoadingStateComponent } from '../components/loading-state/loading-state.component';

type DetailState = 'loading' | 'loaded' | 'not-found' | 'error';

@Component({
  selector: 'app-catalog-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, ErrorStateComponent, LoadingStateComponent],
  templateUrl: './catalog-detail.component.html',
})
export class CatalogDetailComponent implements OnInit {
  private catalogService: CatalogService;
  private route: ActivatedRoute;

  state = signal<DetailState>('loading');
  professional = signal<ProfessionalDetail | null>(null);
  private professionalId = 0;

  constructor(catalogService: CatalogService, route: ActivatedRoute) {
    this.catalogService = catalogService;
    this.route = route;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.professionalId = id;
    this.loadProfessional();
  }

  onRetry(): void {
    this.loadProfessional();
  }

  private loadProfessional(): void {
    this.state.set('loading');
    this.catalogService.getProfessionalById(this.professionalId).subscribe({
      next: (detail) => {
        this.professional.set(detail);
        this.state.set('loaded');
      },
      error: (err) => {
        if (err.status === 404) {
          this.state.set('not-found');
        } else {
          this.state.set('error');
        }
      },
    });
  }
}
