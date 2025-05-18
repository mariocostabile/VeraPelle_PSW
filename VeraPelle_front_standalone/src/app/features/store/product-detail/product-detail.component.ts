// src/app/features/store/product-detail/product-detail.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }               from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { StoreService }               from '@app/core/services/store/store.service';
import { ProductPublicDTO }           from '@app/core/models/product-public-dto';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule  // per *ngIf, *ngFor
  ],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private store = inject(StoreService);

  product: ProductPublicDTO | null = null;
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.store.getProductById(id).subscribe({
      next: p => {
        this.product = p;
        this.loading = false;
      },
      error: () => {
        this.error = 'Prodotto non trovato';
        this.loading = false;
      }
    });
  }
}
