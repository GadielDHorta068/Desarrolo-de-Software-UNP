import { Component, Input, OnInit } from '@angular/core';
import { Report, ReportService } from '../../../services/report.service';
import { HandleReportStatusPipe } from '../../../pipes/handle-report-status.pipe';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-info-report',
  imports: [CommonModule, HandleReportStatusPipe],
  templateUrl: './info-report.html',
  styleUrl: './info-report.css'
})
export class InfoReport implements OnInit{

  @Input() report!: Report|null;
  isPending: boolean = true;

  constructor(
    private reportService: ReportService
  ){}

  ngOnInit(): void {
    // console.log("[info-reporte] => datos: ", this.report);
    if(this.report?.statusReport != "EARRING"){
      this.isPending = false;
    }
  }


  onResolve(status: string){
    this.reportService.resolveReport(""+this.report?.id, ""+this.report?.eventId, status).subscribe(
      data => {
        this.report = data;
        let informResolve = {
          status: "OK",
          msg: "Reporte resuelto exitosamente"
        }
        console.log("[resolucion] => informe de resolucion de reporte: ", informResolve);
      },
      error => {
        let informResolve = {
          status: "OK",
          msg: "No fue posible realizar la operación "
        }
        console.log("[resolucion] => informe de resolucion de reporte: ", informResolve);
      }
    )
  }
}
