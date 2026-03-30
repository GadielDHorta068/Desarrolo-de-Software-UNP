import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { Report, ReportService, ReviewReportDTO, StatusReport } from '../../../services/report.service';
import { HandleReportStatusPipe } from '../../../pipes/handle-report-status.pipe';
import { CommonModule } from '@angular/common';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-info-report',
  imports: [CommonModule, HandleReportStatusPipe, HandleDatePipe],
  templateUrl: './info-report.html',
  styleUrl: './info-report.css'
})
export class InfoReport implements OnInit{

  @Input() report!: ReviewReportDTO|null;
  isPending: boolean = true;
  
  StatusReport = StatusReport;

  constructor(
    private reportService: ReportService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
    console.log("[info-reporte] => datos: ", this.report);
    console.log("[info-reporte] => Id del reporte: ", this.report?.id);
    if(this.report?.statusReport != "EARRING"){
      this.isPending = false;
    }
    this.cdr.detectChanges();
  }


  onResolve(status: StatusReport){
    this.reportService.reviewReport(String(this.report?.id), status).subscribe(
      data => {
        this.report = data;
        let informResolve = {
          status: "OK",
          msg: "Reporte resuelto exitosamente"
        }
        console.log("[resolucion] => informe de resolucion de reporte: ", informResolve);
        this.cdr.detectChanges();
      },
      error => {
        let informResolve = {
          status: "ERROR",
          msg: "No fue posible realizar la operación "
        }
        console.log("[resolucion] => informe de resolucion de reporte: ", informResolve);
        this.cdr.detectChanges();
      }
    )
  }
}
