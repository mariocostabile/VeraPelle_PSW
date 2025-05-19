import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ColorService } from '@app/core/services/color/color.service';
import { ColorDTO } from '@app/core/models/color-dto';

@Component({
  selector: 'app-color-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './color-list.component.html',
  styleUrls: ['./color-list.component.scss']
})
export class ColorListComponent implements OnInit {
  private colorSrv = inject(ColorService);
  private router = inject(Router);

  colors: ColorDTO[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.fetch();
  }

  fetch(): void {
    this.loading = true;
    this.colorSrv.getColors().subscribe({
      next: cs => {
        this.colors = cs;
        this.loading = false;
      },
      error: err => {
        this.error = 'Errore nel caricamento';
        this.loading = false;
      }
    });
  }

  onNew(): void {
    this.router.navigate(['/admin/colors/new']);
  }

  onEdit(c: ColorDTO): void {
    this.router.navigate(['/admin/colors', c.id, 'edit']);
  }

  onDelete(c: ColorDTO): void {
    if (!confirm(`Eliminare il colore "${c.name}"?`)) return;
    this.colorSrv.deleteColor(c.id).subscribe(() => this.fetch());
  }
}
