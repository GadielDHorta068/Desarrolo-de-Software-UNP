import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuditLog } from '../../../models/auditlog.model';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuditService } from '../../../services/audit.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';

@Component({
  selector: 'app-audit-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, HandleTypePipe],
  templateUrl: './audit-detail.html',
  styleUrls: ['./audit-detail.css']
})
export class AuditDetail {

 /*  auditlog?: AuditLog;
  loading = true;

  constructor (
    
    private route: ActivatedRoute,
    private router: Router,
    private auditService: AuditService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
    
  ) {console.log('AuditDetailComponent inicializado');}

  ngOnInit() :void {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        const nickname = user.nickname;
        this.fetchAuditLog(nickname);
        console.log("Estado del loading: ", this.loading)
      },
      error: (err) => {
        console.error('Error al obtener usuario actual: ', err);
        this.loading = false;
      }
    });
  }

  goBack(){
    this.router.navigate(['/audit']);
  }

  fetchAuditLog(nickname: string): void {
    this.loading = true;
    this.auditService.getAuditLogs(nickname).subscribe({
      next: (data) => {
        this.auditlog = data && data.length > 0 ? data[0] : undefined;
        this.loading = false;
        this.cdr.detectChanges();
        console.log("Detalles de las auditorías: ",this.auditlog);
      },
      error: (err) => {
        console.error('Error al recuperar el log de auditoría:', err);
        this.loading = false;
      }
      
    });
  }

  formatDate(dateArray: any): string {
    if (!dateArray) return '-';
    const [year, month, day, hours = 0, minutes = 0, seconds = 0] = dateArray;
    const d = new Date(year, month - 1, day, hours, minutes, seconds);
    console.log('Fecha formateada: ', d.toLocaleString());
    return d.toLocaleString();
  } */
}
