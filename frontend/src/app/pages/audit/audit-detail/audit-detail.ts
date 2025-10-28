import { ChangeDetectorRef, Component } from '@angular/core';
import { AuditAction, AuditActionType } from '../../../models/auditevent.model';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuditService } from '../../../services/audit.service';
import { CommonModule } from '@angular/common';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-audit-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, HandleTypePipe, FormsModule],
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
    
      this.auditService.getActionByFilters(
        this.eventId,
        this.selectedAction,
        this.dateFrom,
        this.dateTo
      ).subscribe({
        next: (actions) => {
          this.auditActions = actions;
          this.loading = false;
          this.cdr.detectChanges();
          console.log('Acciones de auditoría recuperadas:', this.auditActions);
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
    if (this.dateFrom && this.dateTo && new Date(this.dateFrom) > new Date(this.dateTo)) {
      alert('La fecha "Desde" no puede ser posterior a la fecha "Hasta"');
      return;
    }
    this.loadAuditDetails();
  }
 
  goBack(){
    this.router.navigate(['/audit']);
  }
}
