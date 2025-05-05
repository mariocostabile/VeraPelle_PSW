// src/app/features/admin/category-list/category-list.component.ts

import { Component, OnInit }    from '@angular/core';
import { CommonModule }          from '@angular/common';
import { RouterModule }          from '@angular/router';
import { CategoryDTO }           from '@app/core/models/category-dto';
import { CategoryService }       from '@app/core/services/category/category.service';

@Component({
  standalone: true,
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.scss'],
  imports: [CommonModule, RouterModule]
})
export class CategoryListComponent implements OnInit {
  categories: CategoryDTO[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.error = null;
    this.categoryService.getCategories().subscribe({
      next: data => {
        this.categories = data;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Errore nel caricamento delle categorie';
        this.isLoading = false;
      }
    });
  }

  deleteCategory(id: number): void {
    if (!confirm('Sei sicuro di voler eliminare questa categoria?')) {
      return;
    }
    this.categoryService.deleteCategory(id).subscribe({
      next: () => this.loadCategories(),
      error: () => alert('Errore durante l\'eliminazione della categoria')
    });
  }
}
