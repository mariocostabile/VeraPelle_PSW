// src/app/features/admin/category-form/category-form.component.ts

import { Component, OnInit, inject }        from '@angular/core';
import { CommonModule }                     from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CategoryService }                  from '@app/core/services/category/category.service';
import { CategoryDTO }                      from '@app/core/models/category-dto';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  selector: 'app-category-form',
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.scss']
})
export class CategoryFormComponent implements OnInit {
  private fb     = inject(FormBuilder);
  private route  = inject(ActivatedRoute);
  private router = inject(Router);
  private srv    = inject(CategoryService);

  form!: FormGroup;
  categoryId?: number;

  ngOnInit(): void {
    // Inizializzo il form
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });

    // Se ho l'id nella route, sono in modifica
    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.categoryId = +idParam;
      this.srv.getCategoryById(this.categoryId)
        .subscribe(cat => this.form.patchValue(cat));
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const dto: CategoryDTO = this.form.value;
    const obs = this.categoryId
      ? this.srv.updateCategory(this.categoryId, dto)
      : this.srv.createCategory(dto);

    obs.subscribe({
      next: () => this.router.navigate(['/admin/categories']),
      error: err => alert('Errore: ' + err.message)
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/categories']);
  }
}
