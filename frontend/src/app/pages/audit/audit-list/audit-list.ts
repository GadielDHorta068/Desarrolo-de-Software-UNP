import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuditLog } from '../../../models/auditlog.model';
import { AuditService } from '../../../services/audit.service';
import { error } from 'console';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [CommonModule, RouterModule, HandleTypePipe],
  templateUrl: './audit-list.html',
  styleUrls: ['./audit-list.css']
})
export class AuditList {
  /* audits: AuditLog[] = [];
  loading = true;
  nickname: string = '';

  constructor(
    private auditService: AuditService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {console.log('AuditListComponent inicializado');}

  ngOnInit(): void {
    console.log('AuditList inicializado');
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.nickname = user.nickname;
        console.log('Usuario actual:', this.nickname);

        this.auditService.getAuditLogs(this.nickname).subscribe({
          next: (data) => {
            this.audits = data || [];
            this.loading = false;
            this.cdr.detectChanges();
            console.log('Datos de la auditoria: ', this.audits);
            console.log('Loading ahora es:', this.loading);
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

  formatDate(dateArray: any): string {
    if (!dateArray) return '-';
    const [year, month, day, hours = 0, minutes = 0, seconds = 0] = dateArray;
    const d = new Date(year, month - 1, day, hours, minutes, seconds);
    return d.toLocaleString();
  } */

}
