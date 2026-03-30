import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReportService, AdminEventReport } from '../../../services/report.service';
import { takeUntil, finalize, Subject } from 'rxjs';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { HandleReportStatusPipe } from '../../../pipes/handle-report-status.pipe';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-report-list',
  standalone: true,
  imports: [CommonModule, RouterModule, HandleTypePipe, HandleReportStatusPipe, HandleDatePipe],
  templateUrl: './report-list.component.html',
  styleUrl: './report-list.component.css',
})
export class ReportListComponent implements OnInit, OnDestroy {
  
  reportedEvents: AdminEventReport[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  reportCount: number = 0;
  private destroy$ = new Subject<void>();
  userLogged: boolean = false;

  constructor(
    private reportService: ReportService,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadReportedEvents();
  }

  /**
   * Carga la lista de eventos reportados desde el backend
   */
  loadReportedEvents(): void {
    this.userLogged = this.authService.isAuthenticated();

    this.isLoading = true;
    this.errorMessage = null;

    this.reportService.getAllReportedEvents()
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (data: AdminEventReport[]) => {
          this.reportedEvents = data;
          this.reportCount = data.length;
          console.log("[reportes] => Eventos reportados cargados: ", data);
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error("[reportes] => Error al cargar eventos reportados: ", error);
          this.errorMessage = "Error al cargar los eventos reportados. Intenta más tarde.";
          this.cdr.detectChanges();
        }
      });
  }

  /**
   * Navega a los detalles de un evento reportado
   * @param eventId ID del evento
   */
  viewReportDetails(eventId: Number): void {
    if (eventId) {
      console.log("[reportes] => Navegando a detalles del evento: ", eventId);
      this.router.navigate(['/admin/reports', eventId]);
    } else {
      console.error("[reportes] => ID del evento inválido: ", eventId);
    }
  }

  /**
   * Obtiene el color del badge según el estado
   * @param status Estado del reporte
   */
  getStatusBadgeColor(status: string): string {
    switch (status) {
      case 'EARRING':
        return 'bg-yellow-500'; // Pendiente
      case 'APPROVED':
        return 'bg-red-500'; // Bloqueado
      case 'REJECTED':
        return 'bg-green-500'; // Abierto
      default:
        return 'bg-gray-500';
    }
  }

/*   /**
   * Obtiene el texto del estado
   * @param status Estado del reporte
   
  getStatusText(status: string): string {
    switch (status) {
      case 'EARRING':
        return 'Pendiente';
      case 'APPROVED':
        return 'Aprobado (Evento Bloqueado)';
      case 'REJECTED':
        return 'Rechazado (Evento Abierto)';
      default:
        return status;
    }
  } */

  /**
   * Obtiene el color del score según su valor
   * @param score Score del evento
   */
  getScoreColor(score: Number): string {
    let scoreValue = score as number
    if (scoreValue < 5) return 'text-green-500';
    if (scoreValue >= 5 && scoreValue <= 9) return 'text-yellow-500';
    if (scoreValue >= 10 && scoreValue <= 14) return 'text-orange-500';
    return 'text-red-500';
  }

  /**
   * Recarga la lista de eventos reportados
   */
  reloadReports(): void {
    this.loadReportedEvents();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
