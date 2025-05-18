// src/app/features/admin/product-form/product-form.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { ProductService } from '@app/core/services/product/product.service';
import { CategoryService } from '@app/core/services/category/category.service';
import { ImageUploadService } from '@app/core/services/image-upload/image-upload.service';

import { ProductDTO } from '@app/core/models/product-dto';
import { CategoryDTO } from '@app/core/models/category-dto';
import { ProductImageDTO } from '@app/core/models/product-image-dto';

interface FilePreview {
  file: File;
  preview: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private prodSrv = inject(ProductService);
  private catSrv = inject(CategoryService);
  private imgSrv = inject(ImageUploadService);

  form!: FormGroup;
  categories: CategoryDTO[] = [];
  productId?: number;

  /** anteprime dei file selezionati in locale */
  selectedPreviews: FilePreview[] = [];
  /** immagini già caricate sul server */
  imageList: ProductImageDTO[] = [];

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      categoryIds: [[], Validators.required]
    });

    // carica tutte le categorie
    this.catSrv.getCategories().subscribe(c => this.categories = c);

    // se siamo in modifica, recupera il prodotto e le sue immagini
    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.productId = +idParam;
      this.prodSrv.getProductById(this.productId)
        .subscribe(p => {
          this.form.patchValue(p);
          this.imgSrv.getImages(this.productId!)
            .subscribe(imgs => this.imageList = imgs);
        });
    }
  }

  onCategoryChange(e: Event) {
    const cb = e.target as HTMLInputElement;
    const sel: number[] = this.form.get('categoryIds')!.value || [];
    const id = +cb.value;
    const updated = cb.checked
      ? [...sel, id]
      : sel.filter(x => x !== id);
    this.form.get('categoryIds')!.setValue(updated);
    this.form.get('categoryIds')!.markAsTouched();
  }

  onFilesSelected(e: Event) {
    const inp = e.target as HTMLInputElement;
    if (!inp.files) return;
    Array.from(inp.files).forEach(file => {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedPreviews.push({
          file,
          preview: reader.result as string
        });
      };
      reader.readAsDataURL(file);
    });
    // resetta l'input per poter ricaricare gli stessi file
    inp.value = '';
  }

  removeSelected(index: number) {
    this.selectedPreviews.splice(index, 1);
  }

  uploadSelectedImages() {
    if (!this.productId || this.selectedPreviews.length === 0) return;
    const filesToUpload = this.selectedPreviews.map(p => p.file);
    this.imgSrv.uploadImages(this.productId, filesToUpload).subscribe({
      next: newImgs => {
        this.imageList = [...this.imageList, ...newImgs];
        this.selectedPreviews = [];
      },
      error: err => alert('Upload error: ' + err.message)
    });
  }

  removeImage(img: ProductImageDTO) {
    if (!this.productId) return;
    this.imgSrv.deleteImage(this.productId, img.id).subscribe({
      next: () => {
        this.imageList = this.imageList.filter(i => i.id !== img.id);
      },
      error: err => alert('Delete error: ' + err.message)
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    const dto: ProductDTO = this.form.value;
    const op = this.productId
      ? this.prodSrv.updateProduct(this.productId, dto)
      : this.prodSrv.createProduct(dto);
    op.subscribe(() => this.router.navigate(['/admin/products']));
  }

  onCancel() {
    this.router.navigate(['/admin/products']);
  }
}
