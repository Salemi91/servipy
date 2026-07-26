import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Category } from '../../../../../shared/models/category.model';

@Component({
  selector: 'app-category-filter',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './category-filter.component.html',
})
export class CategoryFilterComponent {
  @Input({ required: true }) categories: Category[] = [];
  @Input() selectedCategoryId: number | null = null;
  @Output() categorySelected = new EventEmitter<number | null>();

  select(categoryId: number | null): void {
    this.categorySelected.emit(categoryId);
  }
}
