import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { StoreService } from '@app/core/services/store/store.service';
import { CategoryService } from '@app/core/services/category/category.service';
import { ProductPublicDTO } from '@app/core/models/product-public-dto';
import { CategoryDTO } from '@app/core/models/category-dto';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  products: ProductPublicDTO[] = [];
  categories: CategoryDTO[] = [];
  selectedCategoryIds: number[] = [];

  // Paginazione
  page = 0;
  size = 12;
  totalPages = 0;

  constructor(
    private storeService: StoreService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories(): void {
    this.categoryService
      .getCategories()
      .subscribe(cats => this.categories = cats);
  }

  loadProducts(): void {
    this.storeService
      .getProducts(this.page, this.size, this.selectedCategoryIds)
      .subscribe(res => {
        this.products   = res.content;
        this.totalPages = res.totalPages;
      });
  }

  /**
   * Aggiunge o rimuove un filtro di categoria; se catId è null resetta tutti i filtri.
   */
  onCategoryToggle(catId: number | null): void {
    if (catId === null) {
      this.selectedCategoryIds = [];
    } else {
      const idx = this.selectedCategoryIds.indexOf(catId);
      if (idx > -1) {
        this.selectedCategoryIds.splice(idx, 1);
      } else {
        this.selectedCategoryIds.push(catId);
      }
    }
    this.page = 0;
    this.loadProducts();
  }

  goToPage(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.page = p;
    this.loadProducts();
  }
}
