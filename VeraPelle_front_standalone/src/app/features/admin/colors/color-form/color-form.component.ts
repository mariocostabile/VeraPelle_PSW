import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ColorService } from '@app/core/services/color/color.service';
import { ColorDTO } from '@app/core/models/color-dto';

@Component({
  selector: 'app-color-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './color-form.component.html',
  styleUrls: ['./color-form.component.scss']
})
export class ColorFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private colorSrv = inject(ColorService);

  form = this.fb.group({
    name: ['', Validators.required],
    hexCode: ['#000000', [
      Validators.required,
      Validators.pattern(/^#([0-9A-Fa-f]{6})$/)
    ]]
  });

  colorId?: number;
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.colorId = +id;
      this.colorSrv.getColor(this.colorId).subscribe({
        next: c => {
          this.form.patchValue(c);
          this.loading = false;
        },
        error: () => {
          this.error = 'Colore non trovato';
          this.loading = false;
        }
      });
    } else {
      this.loading = false;
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    // Estrai e pulisci valori per DTO
    const { name, hexCode } = this.form.value;
    const dto: Partial<ColorDTO> = {
      name: name ?? undefined,
      hexCode: hexCode ?? undefined
    };

    const op = this.colorId
      ? this.colorSrv.updateColor(this.colorId, dto)
      : this.colorSrv.createColor(dto);

    op.subscribe({
      next: () => {
        void this.router.navigate(['/admin/colors']);
      },
      error: err => {
        console.error('Errore salvataggio colore:', err);
        this.error = 'Errore durante il salvataggio';
      }
    });
  }

  onCancel(): void {
    void this.router.navigate(['/admin/colors']);
  }

  /** Quando l’utente mette a fuoco il campo hex, lascia solo il cancelletto */
  onHexFocus(): void {
    const ctrl = this.form.get('hexCode')!;
    const val: string = ctrl.value || '';
    ctrl.setValue(val.startsWith('#') ? '#' : '');
  }
}
