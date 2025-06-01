// src/app/features/admin/product-form/product-form.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  ValidatorFn,
  AbstractControl
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
      name:           ['', Validators.required],
      description:    [''],
      price:          [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      categoryIds:    [[], Validators.required],
      colorIds:       [[], Validators.required],
      colorQuantities: this.fb.array([], Validators.required)
    }, {
      validators: this.matchTotalQuantity()
    });

    this.form.get('stockQuantity')!
      .valueChanges
      .subscribe(() => this.form.markAsTouched());

    // Aggiorna stockQuantity ogni volta che cambia colorQuantities
    this.colorQuantities.valueChanges.subscribe(items => {
      const sum = (items as any[])
        .map(i => i.quantity || 0)
        .reduce((a,b) => a + b, 0);
      // patchValue senza ciclare di nuovo valueChanges
      this.form.get('stockQuantity')!.patchValue(sum, { emitEvent: false });
    });

    // carica tutte le categorie e i colori
    this.catSrv.getCategories().subscribe(c => this.categories = c);
    this.colorSrv.getColors().subscribe(c => this.colors = c);

    // se siamo in modifica, recupera il prodotto e le sue immagini
    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.productId = +idParam;
      this.prodSrv.getProductById(this.productId).subscribe(p => {

        const vars = p.variants ?? [];
        // 1) patch dei campi base
        this.form.patchValue({
          name:          p.name,
          description:   p.description,
          price:         p.price,
          stockQuantity: p.stockQuantity,
          categoryIds:   p.categoryIds,
          // riempi colorIds da p.variants
          colorIds:     vars.map(v => v.colorId)
        });

        // 2) per ogni variante creo anche il group con quantità iniziale
        vars.forEach(v => {
          this.colorQuantities.push(this.createColorGroup(v.colorId, v.stockQuantity));
        });


        // 3) poi carichi le immagini come facevi
        this.imgSrv.getImages(this.productId!).subscribe(imgs => this.imageList = imgs);
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

  /** shortcut al FormArray colorQuantities */
  get colorQuantities(): FormArray {
    return this.form.get('colorQuantities') as FormArray;
  }

  private createColorGroup(colorId: number, initialQty = 0): FormGroup {
    return this.fb.group({
      colorId:  [colorId],
      quantity: [initialQty, [Validators.required, Validators.min(0)]]
    });
  }


  /** Estendi onColorChange per aggiungere/rimuovere i gruppi */
  onColorChange(e: Event): void {
    const cb = e.target as HTMLInputElement;
    const sel: number[] = this.form.value.colorIds || [];
    const id = +cb.value;
    let newSel: number[];
    if (cb.checked) {
      newSel = [...sel, id];
      this.colorQuantities.push(this.createColorGroup(id));
    } else {
      newSel = sel.filter(x => x !== id);
      const idx = this.colorQuantities.controls.findIndex(g => g.value.colorId === id);
      if (idx > -1) this.colorQuantities.removeAt(idx);
    }
    this.form.get('colorIds')!.setValue(newSel);
    this.form.get('colorIds')!.markAsTouched();
  }

  /** Validator di form-level: somma quantità = stockQuantity */
  private matchTotalQuantity(): ValidatorFn {
    return (fg: AbstractControl) => {
      const stock = fg.get('stockQuantity')!.value || 0;
      const sum = (fg.get('colorQuantities') as FormArray)
        .controls
        .map(c => c.get('quantity')!.value || 0)
        .reduce((a,b) => a + b, 0);
      return sum === stock ? null : { quantitiesSumMismatch: true };
    };
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

      // 1) destruttura come in onSubmit
      const { colorQuantities, ...base } = this.form.value as any;
      const variants = (colorQuantities as Array<{colorId:number,quantity:number}>)
        .map(cq => ({
          colorId: cq.colorId,
          stockQuantity: cq.quantity
        }));

      // 2) ricompongo il DTO con variants sempre presente
      const dto: any = {
        ...base,
        variants
      };

      // 3) creo il prodotto
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
    // 1. Scompongo il risultato del form:
    //    - prende tutti i campi base (name, price, ecc.),
    //    - estrae colorQuantities in una variabile
    const { colorQuantities, ...base } = this.form.value as any;

    // 2. Mappo colorQuantities in un array di "variant" minimale
    const variants = (colorQuantities as Array<{colorId:number,quantity:number}>)
      .map(cq => ({
        colorId: cq.colorId,
        stockQuantity: cq.quantity
      }));

    // 3. Ricompongo il DTO includendo sempre la proprietà "variants"
    const dto: any = {
      ...base,
      // Il campo stockQuantity è già in base
      // variants serve per aggiornare le righe product_variant
      variants
    };

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

  /** Restituisce il nome del colore dato il suo ID */
  getColorName(colorId: number): string {
    const col = this.colors.find(x => x.id === colorId);
    return col ? col.name : '';
  }

  /** Se l’admin mette a fuoco il prezzo e questo vale 0, lo azzero a null */
  onPriceFocus(): void {
    const ctrl = this.form.get('price');
    if (ctrl && ctrl.value === 0) {
      ctrl.setValue(null);
    }
  }

  onQuantityFocus(index: number): void {
    const ctrl = this.colorQuantities.at(index).get('quantity');
    if (ctrl?.value === 0) {
      ctrl.setValue(null);
    }
  }


}
