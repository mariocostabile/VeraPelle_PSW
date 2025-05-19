// src/app/features/store/product-detail/product-detail.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { StoreService } from '@app/core/services/store/store.service';
import { ProductPublicDTO } from '@app/core/models/product-public-dto';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private store = inject(StoreService);

  product: ProductPublicDTO | null = null;
  selectedImage: string | null = null;
  currentIndex = 0;  // indice corrente per lo slider
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.store.getProductById(id).subscribe({
      next: p => {
        this.product = p;
        this.currentIndex = 0;
        this.selectedImage = p.imageUrls.length ? p.imageUrls[0] : null;
        this.loading = false;
      },
      error: () => {
        this.error = 'Prodotto non trovato';
        this.loading = false;
      }
    });
  }

  onSelectImage(url: string): void {
    this.selectedImage = url;
  }

  onPrev(): void {
    if (!this.product) return;
    const len = this.product.imageUrls.length;
    this.currentIndex = (this.currentIndex - 1 + len) % len;
    this.selectedImage = this.product.imageUrls[this.currentIndex];
  }

  onNext(): void {
    if (!this.product) return;
    const len = this.product.imageUrls.length;
    this.currentIndex = (this.currentIndex + 1) % len;
    this.selectedImage = this.product.imageUrls[this.currentIndex];
  }
}
