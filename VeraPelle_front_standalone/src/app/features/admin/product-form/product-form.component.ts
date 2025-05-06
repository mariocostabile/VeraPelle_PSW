// src/app/features/admin/product-form/product-form.component.ts

import { Component, OnInit, inject }        from '@angular/core';
import { CommonModule }                     from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductService }                   from '@app/core/services/product/product.service';
import { CategoryService }                  from '@app/core/services/category/category.service';
import { ProductDTO }                       from '@app/core/models/product-dto';
import { CategoryDTO }                      from '@app/core/models/category-dto';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  private fb      = inject(FormBuilder);
  private route   = inject(ActivatedRoute);
  private router  = inject(Router);
  private prodSrv = inject(ProductService);
  private catSrv  = inject(CategoryService);

  form!: FormGroup;
  categories: CategoryDTO[] = [];
  productId?: number;

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      categoryIds: [[], Validators.required]
    });

    this.catSrv.getCategories().subscribe(cats => this.categories = cats);

    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.productId = +idParam;
      this.prodSrv.getProductById(this.productId)
        .subscribe(prod => this.form.patchValue(prod));
    }
  }

  onCategoryChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    const selected: number[] = this.form.get('categoryIds')!.value || [];
    const id = +checkbox.value;
    if (checkbox.checked) {
      this.form.get('categoryIds')!.setValue([...selected, id]);
    } else {
      this.form.get('categoryIds')!.setValue(selected.filter(x => x !== id));
    }
    this.form.get('categoryIds')!.markAsTouched();
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const dto: ProductDTO = this.form.value;
    const obs = this.productId
      ? this.prodSrv.updateProduct(this.productId, dto)
      : this.prodSrv.createProduct(dto);

    obs.subscribe({
      next: () => this.router.navigate(['/admin/products']),
      error: err => alert('Error: ' + err.message)
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/products']);
  }
}
