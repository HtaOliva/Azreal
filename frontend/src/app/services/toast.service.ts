import { Injectable, signal } from '@angular/core';

export interface ToastItem {
  id: number;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  private readonly _toasts = signal<ToastItem[]>([]);

  readonly toasts = this._toasts.asReadonly();

  show(message: string, durationMs = 3400): void {
    const id = this.nextId++;
    this._toasts.update((list) => [...list, { id, message }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }

  dismiss(id: number): void {
    this._toasts.update((list) => list.filter((item) => item.id !== id));
  }
}
