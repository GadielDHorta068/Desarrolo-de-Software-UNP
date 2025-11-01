import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuditEvent, AuditParticipant } from '../../../models/auditevent.model';
import { AuditService } from '../../../services/audit.service';
import { error } from 'console';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { EventTypes } from '../../../models/events.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [CommonModule, RouterModule, HandleTypePipe, FormsModule],
  templateUrl: './audit-list.html',
  styleUrls: ['./audit-list.css']
})
export class AuditList {

  auditEvents: AuditEvent[] = [];
  loading = true;
  creator: string = '';
  selectedType: EventTypes | null = null;
  dateFrom?: Date;
  dateTo?: Date;
  showModal = false;
  selectedEventParticipants: AuditParticipant[] = [];
  eventTypes = Object.values(EventTypes);
  seed: number | null = null;

  constructor(
    private auditService: AuditService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ){}

  laodAuditEvents() {
    this.loading = true;
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.creator = user.nickname;
        const fromDate = this.dateFrom ? new Date(this.dateFrom) : undefined;
        const toDate = this.dateTo ? new Date(this.dateTo) : undefined;
        if (toDate) {
          toDate.setHours(23, 59, 59, 999);
        }
        this.auditService.getFilterAuditEvent(
          this.creator,
          this.selectedType,
          fromDate,
          toDate
        ).subscribe({
          next: (data) => {
            this.auditEvents = data || [];
            this.loading = false;
           this.cdr.detectChanges();
           console.log('Logs de auditoría recuperados:', this.auditEvents);
          },
          error: (err) => {
            console.error('Error al recuperar logs de auditoría:', err);
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error al obtener usuario actual:', err);
        this.loading = false;
      }
    });
  }

  applyFilters() {
    if (this.dateFrom && this.dateTo && new Date(this.dateFrom) > new Date(this.dateTo)) {
      alert('La fecha "Desde" no puede ser posterior a la fecha "Hasta"');
      return;
    }
    this.laodAuditEvents();
  }

  clearFilters() {
    this.selectedType = null;
    this.dateFrom = undefined;
    this.dateTo = undefined;
    this.laodAuditEvents();
  }

  showParticipants(audit: AuditEvent) {
    this.selectedEventParticipants = audit.participants || [];
    this.seed = audit.seed || null;
    this.showModal = true;
    this.cdr.detectChanges();
  }

  closeModal() {
    this.showModal = false;
    this.selectedEventParticipants = [];
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.laodAuditEvents();
  }
}
