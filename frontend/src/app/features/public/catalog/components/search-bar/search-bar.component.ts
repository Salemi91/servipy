import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
})
export class SearchBarComponent implements OnInit, OnDestroy {
  @Input() initialValue = '';
  @Output() searchChanged = new EventEmitter<string>();

  searchTerm = '';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.searchTerm = this.initialValue;
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((term) => this.searchChanged.emit(term));
  }

  onInput(): void {
    this.searchSubject.next(this.searchTerm);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
