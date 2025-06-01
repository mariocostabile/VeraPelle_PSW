import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import { OrderService } from '../../../core/services/order/order.service';
import { OrderDTO } from '../../../core/models/order-dto';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './order-confirmation.component.html',
  styleUrls: ['./order-confirmation.component.scss']
})
export class OrderConfirmationComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private router = inject(Router);

  order?: OrderDTO;
  loading = true;
  error?: string;

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? +idParam : NaN;
    if (isNaN(id)) {
      this.error = 'ID ordine non valido';
      this.loading = false;
      return;
    }

    this.orderService.getOrder(id).subscribe({
      next: o => {
        this.order = o;
        this.loading = false;
      },
      error: err => {
        this.error = err?.message || 'Errore caricamento ordine';
        this.loading = false;
      }
    });
  }

}
