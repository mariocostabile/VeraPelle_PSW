// src/app/features/admin/product-list/product-list.component.ts

import { Component, OnInit }    from '@angular/core';
import { CommonModule }          from '@angular/common';
import { RouterModule }          from '@angular/router';
import { ProductDTO }            from '@app/core/models/product-dto';
import { ProductService }        from '@app/core/services/product/product.service';

@Component({
  standalone: true,
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
  imports: [CommonModule, RouterModule]
})
export class ProductListComponent implements OnInit {
  products: ProductDTO[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.error = null;
    this.productService.getProducts().subscribe({
      next: data => {
        this.products = data;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Errore nel caricamento dei prodotti';
        this.isLoading = false;
      }
    });
  }

  deleteProduct(id: number): void {
    if (!confirm('Sei sicuro di voler eliminare questo prodotto?')) {
      return;
    }
    this.productService.deleteProduct(id).subscribe({
      next: () => this.loadProducts(),
      error: () => alert('Errore durante l\'eliminazione del prodotto')
    });
  }
}
