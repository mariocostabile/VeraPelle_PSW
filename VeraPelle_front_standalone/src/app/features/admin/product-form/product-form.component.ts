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
import { ColorService } from '@app/core/services/color/color.service';
import { ImageUploadService } from '@app/core/services/image-upload/image-upload.service';

import { ProductDTO } from '@app/core/models/product-dto';
import { CategoryDTO } from '@app/core/models/category-dto';
import { ColorDTO } from '@app/core/models/color-dto';
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
  private colorSrv = inject(ColorService);
  private imgSrv = inject(ImageUploadService);

  form!: FormGroup;
  categories: CategoryDTO[] = [];
  colors: ColorDTO[] = [];
  productId?: number;

  /** anteprime dei file selezionati in locale */
  selectedPreviews: FilePreview[] = [];
  /** immagini già caricate sul server */
  imageList: ProductImageDTO[] = [];

  /** true se abbiamo creato automaticamente un prodotto */
  private isAutoCreated = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      categoryIds: [[], Validators.required],
      colorIds:    [[], Validators.required]
    });

    // carica tutte le categorie e i colori
    this.catSrv.getCategories().subscribe(c => this.categories = c);
    this.colorSrv.getColors().subscribe(c => this.colors = c);

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

  /** Gestione checkbox Categorie */
  onCategoryChange(e: Event): void {
    const cb = e.target as HTMLInputElement;
    const sel: number[] = this.form.get('categoryIds')!.value || [];
    const id = +cb.value;
    this.form.get('categoryIds')!.setValue(
      cb.checked ? [...sel, id] : sel.filter(x => x !== id)
    );
    this.form.get('categoryIds')!.markAsTouched();
  }

  /** Gestione checkbox Colori */
  onColorChange(e: Event): void {
    const cb = e.target as HTMLInputElement;
    const sel: number[] = this.form.get('colorIds')!.value || [];
    const id = +cb.value;
    this.form.get('colorIds')!.setValue(
      cb.checked ? [...sel, id] : sel.filter(x => x !== id)
    );
    this.form.get('colorIds')!.markAsTouched();
  }

  /** Selezione file locale */
  onFilesSelected(e: Event): void {
    const input = e.target as HTMLInputElement;
    if (!input.files?.length) return;
    Array.from(input.files).forEach(file => {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedPreviews.push({ file, preview: reader.result as string });
      };
      reader.readAsDataURL(file);
    });
    input.value = '';
  }

  removeSelected(index: number): void {
    this.selectedPreviews.splice(index, 1);
  }

  /** Metodo interno per l’upload vero e proprio */
  private _doUpload(id: number): void {
    const formData = new FormData();
    this.selectedPreviews.forEach(p => formData.append('files', p.file));
    this.imgSrv.uploadImages(id, formData).subscribe({
      next: newImgs => {
        this.imageList = [...this.imageList, ...newImgs];
        this.selectedPreviews = [];
      },
      error: err => alert('Upload error: ' + err.message)
    });
  }

  /** Upload immagini, con creazione automatica prodotto se serve */
  uploadSelectedImages(): void {
    if (!this.selectedPreviews.length) return;

    if (!this.productId) {
      // auto‐save prima di upload
      if (this.form.invalid) {
        alert('Compila i campi del prodotto prima di caricare le immagini.');
        return;
      }
      const dto: ProductDTO = this.form.value;
      this.prodSrv.createProduct(dto).subscribe({
        next: created => {
          this.productId = created.id;
          this.isAutoCreated = true;
          this._doUpload(created.id!);  // <-- assertion non-null
        },
        error: err => alert('Errore nella creazione del prodotto: ' + err.message)
      });
    } else {
      // prodotto già esistente
      this._doUpload(this.productId!);  // <-- assertion non-null
    }
  }

  removeImage(img: ProductImageDTO): void {
    if (!this.productId) return;
    this.imgSrv.deleteImage(this.productId, img.id).subscribe({
      next: () => this.imageList = this.imageList.filter(i => i.id !== img.id),
      error: err => alert('Delete error: ' + err.message)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const dto: ProductDTO = this.form.value;
    const op = this.productId
      ? this.prodSrv.updateProduct(this.productId, dto)
      : this.prodSrv.createProduct(dto);
    op.subscribe(() => this.router.navigate(['/admin/products']));
  }

  onCancel(): void {
    // se creato automaticamente, lo elimino
    if (this.isAutoCreated && this.productId) {
      this.prodSrv.deleteProduct(this.productId).subscribe({
        next: () => this.router.navigate(['/admin/products']),
        error: () => {
          alert('Non è stato possibile annullare la creazione.');
          this.router.navigate(['/admin/products']);
        }
      });
    } else {
      this.router.navigate(['/admin/products']);
    }
  }
}
