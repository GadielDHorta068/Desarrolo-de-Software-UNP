import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminEventReport, Report, ReportService, ReviewReportDTO, StatusReport } from '../../../services/report.service';
import { EventsTemp } from '../../../models/events.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InfoReport } from '../../../shared/components/info-report/info-report';
import { HandleReportStatusPipe } from '../../../pipes/handle-report-status.pipe';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { EventsService } from '../../../services/events.service';

@Component({
  selector: 'app-reports',
  imports: [CommonModule, FormsModule, InfoReport, HandleReportStatusPipe, HandleTypePipe, HandleDatePipe],
  templateUrl: './report-details.component.html',
  styleUrl: './report-details.component.css'
})
export class ReportDetails implements OnInit{

  eventIdParam!: Number;
  event!: EventsTemp|null;
  eventReport: AdminEventReport|null = null;
  titleEvent: string = "";
  emailCretor: string = "";
  nicknameCreator: string = "";
  countEarring: number = 0;
  countAproved: number = 0;
  countRejected: number = 0;
  emailCreator: string = "";
  adminNotes: string = "";
  errorMessage!: string;
  isloading: boolean = true;

  // Paginación
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalPages: number = 1;
  paginatedReports: ReviewReportDTO[] = [];

  // Modales
  showNotifyModal: boolean = false;
  showFinalDecisionModal: boolean = false;
  notifyMessage: string = "";
  finalDecisionStatus: string = ""; // Cambiar de StatusReport | null a string
  finalDecisionMessage: string = "";
  isSubmitting: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private reportService: ReportService,
    private eventService: EventsService,
    private cdr: ChangeDetectorRef
  ){}

  // Trae todos los reportes de un evento específico
  ngOnInit() {
    this.loadEventDetails();
  }

  loadEventDetails(){

    
    this.isloading = true;
    this.errorMessage = "";

    this.eventIdParam = Number(this.route.snapshot.paramMap.get('eventId'));

    if(this.eventIdParam){
      // traemos la info del evento
      this.reportService.getEventReportDetails(String(this.eventIdParam)).subscribe(
        data => {
          this.eventReport = data;
          console.log("[reportes] => reportes recuperados: ", this.eventReport);
          this.countByStatus(this.eventReport.reports!);
          this.eventService.getEventById(String(this.eventIdParam)).subscribe(
            eventData => {
              this.event = eventData;
              this.emailCreator = this.event.creator.email;
              this.nicknameCreator = this.event.creator.nickname;
              console.log("[reportes] => detalles del evento recuperados: ", this.event);
              this.isloading = false;
              this.cdr.detectChanges();
            },
            error => {
              console.error("[reportes] => Error al cargar detalles del evento: ", error);
              this.errorMessage = "Error al cargar los detalles del evento. Intenta más tarde.";
              this.isloading = false;
              this.cdr.detectChanges();
            }
          )
        },
        error => {
          console.error("[reportes] => Error al cargar detalles del evento reportado: ", error);
          this.isloading = false;
          this.errorMessage = "Error al cargar los detalles del evento reportado. Intenta más tarde.";
        }
      )
      // this.reports = await firstValueFrom(this.reportService.getReportsByEvent(""+this.eventIdParam));
    }
  }

  // Función privada para contar la cantidad de reportes por estado
  private countByStatus(reports: ReviewReportDTO[]){
    for (let i = 0; i < reports.length; i++) {
      const report = reports[i];
      if(report.statusReport == "EARRING"){
        this.countEarring++;
      }
      if(report.statusReport == "APPROVED"){
        this.countAproved++;
      }
      if(report.statusReport == "REJECTED"){
        this.countRejected++;
      }
    }
    this.updatePagination();
  }

  // Actualizar paginación cuando cargue los reportes
  private updatePagination(){
    if(this.eventReport?.reports){
      this.totalPages = Math.ceil(this.eventReport.reports.length / this.itemsPerPage);
      this.currentPage = 1;
      this.updatePaginatedReports();
    }
  }

  // Actualizar los reportes a mostrar según la página actual
  private updatePaginatedReports(){
    if(this.eventReport?.reports){
      const startIndex = (this.currentPage - 1) * this.itemsPerPage;
      const endIndex = startIndex + this.itemsPerPage;
      this.paginatedReports = this.eventReport.reports.slice(startIndex, endIndex);
    }
  }

  // Ir a página anterior
  previousPage(){
    if(this.currentPage > 1){
      this.currentPage--;
      this.updatePaginatedReports();
    }
  }

  // Ir a página siguiente
  nextPage(){
    if(this.currentPage < this.totalPages){
      this.currentPage++;
      this.updatePaginatedReports();
    }
  }

  // Abrir modal de notificación
  openNotifyModal(){
    this.showNotifyModal = true;
    this.notifyMessage = "";
  }

  // Cerrar modal de notificación
  closeNotifyModal(){
    this.showNotifyModal = false;
    this.notifyMessage = "";
  }

  // Abrir modal de decisión final
  openFinalDecisionModal(){
    this.showFinalDecisionModal = true;
    this.finalDecisionStatus = "";
    this.finalDecisionMessage = "";
  }

  // Cerrar modal de decisión final
  closeFinalDecisionModal(){
    this.showFinalDecisionModal = false;
    this.finalDecisionStatus = "";
    this.finalDecisionMessage = "";
  }


  // Notificar al creador del evento sobre el/los reporte/s
  notifyCreator(adminNotes: string) {
    this.isSubmitting = true;
    this.reportService.notifyCreatorAboutReports(String(this.eventIdParam), adminNotes).subscribe(
      response => {
        console.log("[reportes] => Notificación enviada al creador del evento: ", response);
        alert("Notificación enviada al creador del evento.");
        this.closeNotifyModal();
        this.isSubmitting = false;
      },
      error => {
        console.error("[reportes] => Error al enviar notificación al creador del evento: ", error);
        alert("Error al enviar la notificación. Intenta más tarde.");
        this.isSubmitting = false;
      }
    )
  }

  // Tomar decisión final sobre el evento (bloquear o no el evento)
  makeFinalDecision(finalStatus: string, adminMessage: string){
    this.isSubmitting = true;
    this.reportService.makeFinalDecisionOnEvent(String(this.eventIdParam), finalStatus as StatusReport, adminMessage).subscribe(
      response => {
        console.log("[reportes] => Decisión final aplicada al evento: ", response);
        alert("Decisión final aplicada al evento.");
        this.closeFinalDecisionModal();
        this.loadEventDetails();
        this.isSubmitting = false;
      },
      error => {
        console.error("[reportes] => Error al aplicar decisión final al evento: ", error);
        alert("Error al aplicar la decisión final. Intenta más tarde.");
        this.isSubmitting = false;
      }
    )
  }
  
  reloadDetails(){
    this.currentPage = 1;
    this.paginatedReports = [];
    this.countEarring = 0;
    this.countAproved = 0;
    this.countRejected = 0;
    this.loadEventDetails();
  }
}
