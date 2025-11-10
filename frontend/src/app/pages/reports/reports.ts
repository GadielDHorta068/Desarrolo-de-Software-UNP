import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Report, ReportService } from '../../services/report.service';
import { firstValueFrom } from 'rxjs';
import { EventsTemp } from '../../models/events.model';
import { AdminEventService } from '../../services/admin/adminEvent.service';
import { CommonModule } from '@angular/common';
import { InfoReport } from '../../shared/components/info-report/info-report';

@Component({
  selector: 'app-reports',
  imports: [CommonModule, InfoReport],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class Reports implements OnInit{

  eventIdParam!: Number;
  event!: EventsTemp|null;
  reports: Report[] = [];
  titleEvent: string = "";
  countEarring: number = 0;
  countAproved: number = 0;
  countRejected: number = 0;

  constructor(
    private route: ActivatedRoute,
    private reportService: ReportService,
    private adminEventService: AdminEventService,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit() {
    this.eventIdParam = Number(this.route.snapshot.paramMap.get('eventId'));

    this.event = this.adminEventService.getSelectedEvent();

    if(this.eventIdParam){
      // traemos la info del evento
      this.reportService.getReportsByEvent(""+this.eventIdParam).subscribe(
        data => {
          this.reports = data;
          console.log("[reportes] => reportes recuperados: ", this.reports);
          this.countByStatus(this.reports);
          this.cdr.detectChanges();
        }
      )
      // this.reports = await firstValueFrom(this.reportService.getReportsByEvent(""+this.eventIdParam));
    }
  }

  private countByStatus(reports: Report[]){
    for (let i = 0; i < reports.length; i++) {
      const report = reports[i];
      if(report.statusReport == "EARRING"){
        this.countEarring++;
      }
      if(report.statusReport == "APROVED"){
        this.countAproved++;
      }
      if(report.statusReport == "REJECTED"){
        this.countRejected++;
      }
    }
  }

}
