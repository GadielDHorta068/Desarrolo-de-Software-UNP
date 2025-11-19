import { ChangeDetectorRef, Component } from '@angular/core';
import { AuditAction, AuditActionType } from '../../../models/auditevent.model';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuditService } from '../../../services/audit.service';
import { CommonModule } from '@angular/common';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { FormsModule } from '@angular/forms';
import { LoadingIndicator } from '../../../shared/components/loading-indicator/loading-indicator';


@Component({
  selector: 'app-audit-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, HandleTypePipe, FormsModule, LoadingIndicator],
  templateUrl: './audit-detail.html',
  styleUrls: ['./audit-detail.css']
})
export class AuditDetail {

  auditActions: AuditAction[] = [];
  loading = true;
  selectedAction: AuditActionType | null = null;
  dateFrom?: Date;
  dateTo?: Date;
  eventId: number | null = null;
  actionTypes = Object.values(AuditActionType);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auditService: AuditService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit() {
    this.route.params.subscribe(params => {
      const eventId = params['eventId'];
      if (eventId) {
        console.log('Related Event ID received:', eventId);
        this.eventId = Number(eventId);
        this.loadAuditDetails();
      } else {
        console.error('No se proporcionó ID del evento relacionado');
        this.router.navigate(['/audit']);
      }
    });
  }
  
  loadAuditDetails() {
    if (!this.eventId) {
      console.error('No se proporcionó ID del evento');
      return;
    }
      this.loading = true;    
      // Convertir las fechas a formato ISO si están definidas
      const fromDate = this.dateFrom ? new Date(this.dateFrom) : undefined;
      const toDate = this.dateTo ? new Date(this.dateTo) : undefined;
      if (toDate) {
        toDate.setHours(23, 59, 59, 999);
      }
      this.auditService.getActionByFilters(
        this.eventId,
        this.selectedAction,
        fromDate,
        toDate
      ).subscribe({
        next: (actions) => {
          this.auditActions = this.selectedAction 
          ? actions.filter(a => a.action === this.selectedAction)
          : actions;
          this.loading = false;
          this.cdr.detectChanges();
          console.log("Acción seleccionada: ", this.selectedAction);
          console.log('Acciones de auditoría recuperadas:', actions.filter(a => 
          !this.selectedAction || a.action === this.selectedAction
        ));
        },
        error: (err) => {
          console.error('Error al recuperar las acciones de auditoría:', err);
          this.loading = false;
        }
      });
  }

  clearFilters() {
    this.selectedAction = null;
    this.dateFrom = undefined;
    this.dateTo = undefined;
    this.loadAuditDetails();
  }

  applyFilters() {
    if (this.dateFrom && this.dateTo) {
        const fromDate = new Date(this.dateFrom);
        const toDate = new Date(this.dateTo);

        if (fromDate > toDate) {
          alert('La fecha "Desde" no puede ser posterior a la fecha "Hasta".');
        return;
      }
    }
    this.loadAuditDetails();
  }
 
  goBack(){
    this.router.navigate(['/audit']);
  }
}
