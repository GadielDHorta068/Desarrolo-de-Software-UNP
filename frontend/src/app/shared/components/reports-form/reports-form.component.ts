import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventsTemp } from '../../../models/events.model';
import { AuthService } from '../../../services/auth.service';
import { AdminEventService } from '../../../services/admin/adminEvent.service';
import { InformReport, ReportService } from '../../../services/report.service';

export interface ResumeService {
  status: string,
  msg: string
}

@Component({
  selector: 'app-reports-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reports-form.component.html',
  styleUrl: './reports-form.component.css'
})
export class ReportsFormComponent {

  @Input() eventId!: number;

  @Output() informReport = new EventEmitter<any>();
  @Output() close = new EventEmitter<void>();

  curentEvent!: EventsTemp|null;

  form!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private adminEventService: AdminEventService,
        private reportService: ReportService
    ) {}

    ngOnInit() {
        this.curentEvent = this.adminEventService.getSelectedEvent();
        
        this.form = this.fb.group({
            idEvent: [this.curentEvent?.id, Validators.required],
            titleEvent: [{value: this.curentEvent?.title, disabled: true}],
            description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
            email: [{value: this.authService.getCurrentUserValue()?.email, disabled: true}, [Validators.required, Validators.email]]
        });
    }
    
    onReport(){
        let dataReport = {
          eventId: this.form.get("idEvent")?.value,
          eventName: this.form.get("titleEvent")?.value,
          mailUserReport: this.form.get("email")?.value,
          reason: this.form.get("description")?.value
        } as InformReport;
        // console.log("[reportes] => datos del reporte recuperados: ", dataReport);
        this.reportService.informReport(dataReport).subscribe(
          (resp: any) => {
            if(resp.id){
              this.informReport.emit({status: "OK", msg: "El reporte fue creado exitosamente"} as ResumeService);
              this.close.emit();
            }
          },
          error => {
            let msgResponse = this.parseServerError(error?.error?.message) || "No fue posible crear el reporte";
            this.informReport.emit({status: "ERROR", msg: msgResponse} as ResumeService);
            this.close.emit();
          }
        )
    }

    private parseServerError(message: string | undefined): string | null {
        if (!message) return null;
        // Extrae el interpolatedMessage entre comillas si existe
        const match = message.match(/interpolatedMessage='([^']+)'/);
        if (match && match[1]) return match[1];
        // Fallback: intenta con messageTemplate
        const tmpl = message.match(/messageTemplate='([^']+)'/);
        if (tmpl && tmpl[1]) return tmpl[1];
        // Fallback genérico: si contiene "reason" ofrece un mensaje amigable
        if (message.includes('reason')) return 'El motivo debe tener entre 10 y 500 caracteres';
        return null;
    }

    closeModal(): void {
        this.close.emit();
    }
}
