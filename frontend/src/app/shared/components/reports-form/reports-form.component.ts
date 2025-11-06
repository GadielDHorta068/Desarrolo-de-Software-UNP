import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { EventsTemp } from '../../../models/events.model';
import { AuthService } from '../../../services/auth.service';
import { AdminEventService } from '../../../services/admin/adminEvent.service';

@Component({
  selector: 'app-reports-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reports-form.component.html',
  styleUrl: './reports-form.component.css'
})
export class ReportsFormComponent {

  @Input() eventId!: number;

//   loggedUser?: UserDTO;
//   @Output() onInscript = new EventEmitter<UserDTO>();
  @Output() close = new EventEmitter<void>();

  curentEvent!: EventsTemp|null;

  form!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private adminEventService: AdminEventService,
        private activatedRoute: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.curentEvent = this.adminEventService.getSelectedEvent();
        
        // Inicializar form reactivo
        this.form = this.fb.group({
            idEvent: [this.curentEvent?.id, Validators.required],
            titleEvent: [{value: this.curentEvent?.title, disabled: true}],
            description: ['', [Validators.required]],
            email: [{value: this.authService.getCurrentUserValue()?.email, disabled: true}, [Validators.required, Validators.email]]
        });
    }
    
    onReport(){
        let dataReport: any = {};
        dataReport.eventId = this.form.get("idEvent")?.value;
        dataReport.email = this.form.get("email")?.value;
        dataReport.description = this.form.get("description")?.value;
        console.log("[reportes] => datos del reporte recuperados: ", dataReport);
        // TODO: usar el service para realizar el reporte y luego no mostrar el boton de reportar para este usuario en este evento
    }

    closeModal(): void {
        this.close.emit();
    }

    // getters convenientes para el template
    get nameControl() { return this.form.get('name'); }
    get surnameControl() { return this.form.get('surname'); }
    get emailControl() { return this.form.get('email'); }
    get cellphoneControl() { return this.form.get('cellphone'); }
}
