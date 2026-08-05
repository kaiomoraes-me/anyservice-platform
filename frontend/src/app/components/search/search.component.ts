import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { debounceTime, distinctUntilChanged, switchMap, catchError, filter, finalize } from 'rxjs/operators';
import { of, Observable, Subject, takeUntil } from 'rxjs';

export interface UserSearchDto {
  id: number;
  name: string;
  username: string;
  profilePictureUrl: string;
}

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit, OnDestroy {
  searchControl = new FormControl('');
  results$: Observable<UserSearchDto[]> = of([]);
  isSearching = false;
  private destroy$ = new Subject<void>();

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.results$ = this.searchControl.valueChanges.pipe(
      takeUntil(this.destroy$),
      filter((val): val is string => val !== null),
      debounceTime(350), 
      distinctUntilChanged(),
      switchMap(query => {
        const cleanQuery = query.trim();
        if (cleanQuery.length === 0 || cleanQuery.length > 50) {
          this.isSearching = false;
          return of([]);
        }
        
        this.isSearching = true;
        return this.http.get<UserSearchDto[]>(`/api/users/search?q=${encodeURIComponent(cleanQuery)}`).pipe(
          catchError(err => {
            console.error('Falha na API de Busca - Sistema Recuperado', err);
            return of([]); 
          }),
          finalize(() => this.isSearching = false)
        );
      })
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
