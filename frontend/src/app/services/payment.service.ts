import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { 
  Payment, 
  PaymentResponse, 
  PaymentSingleResponse, 
  PaymentFilter, 
  PaymentSortType,
  PaymentStatus 
} from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = '/payments'; // Será manejado por el proxy de nginx
  private paymentsSubject = new BehaviorSubject<Payment[]>([]);
  public payments$ = this.paymentsSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Obtener todos los pagos
  getAllPayments(): Observable<Payment[]> {
    return this.http.get<PaymentResponse>(`${this.apiUrl}`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data;
        }
        throw new Error(response.message);
      })
    );
  }

  // Obtener pagos por usuario (como pagador)
  getPaymentsByUser(userId: number): Observable<Payment[]> {
    return this.http.get<PaymentResponse>(`${this.apiUrl}/user/${userId}/all`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data;
        }
        throw new Error(response.message);
      }),
      catchError(error => {
        console.error('Error fetching payments by user:', error);
        throw error;
      })
    );
  }

  // Obtener pagos por receptor
  getPaymentsByReceiver(receiverId: number): Observable<Payment[]> {
    return this.http.get<PaymentResponse>(`${this.apiUrl}/receiver/${receiverId}/all`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data;
        }
        throw new Error(response.message);
      }),
      catchError(error => {
        console.error('Error fetching payments by receiver:', error);
        throw error;
      })
    );
  }

  // Obtener pagos por evento
  getPaymentsByEvent(eventId: number): Observable<Payment[]> {
    return this.http.get<PaymentResponse>(`${this.apiUrl}/event/${eventId}/all`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data;
        }
        throw new Error(response.message);
      })
    );
  }

  // Obtener pagos por estado
  getPaymentsByStatus(status: PaymentStatus): Observable<Payment[]> {
    return this.http.get<PaymentResponse>(`${this.apiUrl}/status/${status}/all`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data;
        }
        throw new Error(response.message);
      })
    );
  }

  // Obtener un pago específico
  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<PaymentSingleResponse>(`${this.apiUrl}/${id}`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data;
        }
        throw new Error(response.message);
      })
    );
  }

  // Verificar si un usuario ha pagado por un evento
  verifyUserPayment(userId: number, eventId: number): Observable<boolean> {
    return this.http.get<{status: number, data: {hasPaid: boolean}}>(`${this.apiUrl}/verify/user/${userId}/event/${eventId}`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data.hasPaid;
        }
        return false;
      })
    );
  }

  // Verificar si existe un pago
  checkPaymentExists(paymentId: number): Observable<boolean> {
    return this.http.get<{status: number, data: {exists: boolean}}>(`${this.apiUrl}/exists/${paymentId}`).pipe(
      map(response => {
        if (response.status === 200) {
          return response.data.exists;
        }
        return false;
      })
    );
  }

  // Métodos de utilidad para ordenamiento y filtrado
  sortPayments(payments: Payment[], filter: PaymentFilter): Payment[] {
    const sorted = [...payments].sort((a, b) => {
      let comparison = 0;
      
      switch (filter.sortBy) {
        case PaymentSortType.DATE:
          // Convertir arrays de fecha a timestamps para comparar
          const dateA = this.arrayToDate(a.createdAt).getTime();
          const dateB = this.arrayToDate(b.createdAt).getTime();
          comparison = dateA - dateB;
          break;
        case PaymentSortType.SENDER:
          comparison = a.user.name.localeCompare(b.user.name);
          break;
        case PaymentSortType.EVENT:
          comparison = a.event.title.localeCompare(b.event.title);
          break;
        default:
          comparison = 0;
      }
      
      return filter.sortOrder === 'desc' ? -comparison : comparison;
    });

    return sorted;
  }

  filterPayments(payments: Payment[], filter: PaymentFilter): Payment[] {
    let filtered = payments;

    if (filter.status) {
      filtered = filtered.filter(payment => payment.status === filter.status);
    }

    if (filter.userId) {
      filtered = filtered.filter(payment => payment.user.id === filter.userId);
    }

    if (filter.eventId) {
      filtered = filtered.filter(payment => payment.event.id === filter.eventId);
    }

    return this.sortPayments(filtered, filter);
  }

  // Formatear moneda
  formatCurrency(amount: number, currency: string): string {
    return new Intl.NumberFormat('es-PY', {
      style: 'currency',
      currency: currency || 'PYG'
    }).format(amount);
  }

  // Formatear fecha
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('es-PY', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // Convertir array de fecha del backend a Date
  private arrayToDate(dateArray: number[]): Date {
    if (!dateArray || dateArray.length < 3) {
      return new Date();
    }
    
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateArray;
    return new Date(year, month - 1, day, hour, minute, second);
  }

  // Obtener color del estado
  getStatusColor(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.APPROVED:
        return 'text-green-600 bg-green-100 dark:text-green-400 dark:bg-green-900/20';
      case PaymentStatus.PENDING:
        return 'text-yellow-600 bg-yellow-100 dark:text-yellow-400 dark:bg-yellow-900/20';
      case PaymentStatus.REJECTED:
        return 'text-red-600 bg-red-100 dark:text-red-400 dark:bg-red-900/20';
      case PaymentStatus.CANCELLED:
        return 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/20';
      case PaymentStatus.REFUNDED:
        return 'text-blue-600 bg-blue-100 dark:text-blue-400 dark:bg-blue-900/20';
      default:
        return 'text-gray-600 bg-gray-100 dark:text-gray-400 dark:bg-gray-900/20';
    }
  }

  // Obtener texto del estado en español
  getStatusText(status: PaymentStatus): string {
    switch (status) {
      case PaymentStatus.APPROVED:
        return 'Aprobado';
      case PaymentStatus.PENDING:
        return 'Pendiente';
      case PaymentStatus.REJECTED:
        return 'Rechazado';
      case PaymentStatus.CANCELLED:
        return 'Cancelado';
      case PaymentStatus.REFUNDED:
        return 'Reembolsado';
      default:
        return 'Desconocido';
    }
  }
}