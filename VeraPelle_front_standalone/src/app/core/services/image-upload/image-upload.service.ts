import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ProductImageDTO } from '@app/core/models/product-image-dto';

@Injectable({
  providedIn: 'root',
})
export class ImageUploadService {
  private backend = 'http://localhost:8080';
  private baseUrl = `${this.backend}/api/admin/products`;

  constructor(private http: HttpClient) {}

  /**
   * Carica immagini per un prodotto. Accetta direttamente un FormData contenente i file.
   */
  uploadImages(productId: number, formData: FormData): Observable<ProductImageDTO[]> {
    return this.http.post<ProductImageDTO[]>(
      `${this.baseUrl}/${productId}/images`,
      formData
    ).pipe(
      map(imgs =>
        imgs.map(i => ({ id: i.id, urlPath: this.backend + i.urlPath }))
      )
    );
  }

  /**
   * Recupera le immagini già caricate di un prodotto.
   */
  getImages(productId: number): Observable<ProductImageDTO[]> {
    return this.http.get<ProductImageDTO[]>(
      `${this.baseUrl}/${productId}/images`
    ).pipe(
      map(imgs =>
        imgs.map(i => ({ id: i.id, urlPath: this.backend + i.urlPath }))
      )
    );
  }

  /**
   * Elimina un'immagine di un prodotto dal server.
   */
  deleteImage(productId: number, imageId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${productId}/images/${imageId}`
    );
  }
}
