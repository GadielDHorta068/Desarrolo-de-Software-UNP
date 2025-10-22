import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

import { QuestionaryService } from '../../services/questionary.service';
import { NotificationService } from '../../services/notification.service';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { UserDTO } from '../../models/UserDTO';
import { EventsService } from '../../services/events.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-questionary',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './questionary.component.html',
  styleUrl: './questionary.component.css'
})
export class QuestionaryComponent {

  @Input() eventId!: number;
  @Output() onInscript = new EventEmitter<UserDTO>();
  @Output() close = new EventEmitter<void>();

  event!: EventsTemp | null;
  allEventTypes = EventTypes;
  allEventStates = StatusEvent;

  form!: FormGroup;

    constructor(
        private fb: FormBuilder,
        private eventService: EventsService,
        private activatedRoute: ActivatedRoute,
    ) {}

    ngOnInit() {
        // Inicializar form reactivo
        this.form = this.fb.group({
            name: ['', Validators.required],
            surname: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            cellphone: ['']
        });

        // cargar evento si hace falta (opcional, mantiene lo que tenías)
        // const eventIdParam = this.eventId ?? Number(this.activatedRoute.snapshot.paramMap.get('eventId'));
        // if (eventIdParam != null) {
        //     this.eventService.getEventById('' + eventIdParam).subscribe({
        //         next: resp => {
        //             this.event = resp;
        //             // si necesitás precondiciones para habilitar el form, las podés controlar con this.event
        //         },
        //         error: err => {
        //             console.error('Error al cargar evento:', err);
        //         }
        //     });
        // }
    }

    validatePhoneInput(event: KeyboardEvent) {
        const allowedChars = /[0-9]/; // Solo dígitos
        const inputChar = event.key;
        if (!allowedChars.test(inputChar)) {
            event.preventDefault();
        }
    }

    // onCellphoneInput(event: Event): void {
    //     const input = event.target as HTMLInputElement;
    //     const digits = (input.value || '').replace(/\D/g, '').slice(0, 10);
    //     this.guestUser.cellphone = digits;
    // }

    // private formatArgPhone(input: string): string {
    //     const digits = (input || '').replace(/\D/g, '');
    //     if (!digits) return '';
    //     return digits.startsWith('54') ? digits : '54' + digits;
    // }
    onConfirmInscription(): void {
        // Forzamos validación visual si el usuario intenta enviar sin completar
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        const user: UserDTO = {
            name: this.form.value.name,
            surname: this.form.value.surname,
            email: this.form.value.email,
            cellphone: this.form.value.cellphone
        };

        this.onInscript.emit(user);
        this.close.emit();
    }

    closeModal(): void {
        this.close.emit();
    }

    // onSubmit(): void {
    //     // Formatear teléfono: solo dígitos y prefijo 54 si corresponde
    //     if (this.guestUser?.cellphone) {
    //         this.guestUser.cellphone = this.formatArgPhone(this.guestUser.cellphone);
    //     }

    //     if (this.eventType != EventTypes.RAFFLES) {
    //         this.questionaryService.save(
    //             this.guestUser,
    //             this.eventId //despues podria tener un objeto y usar un dto o algo asi
    //         ).subscribe({
    //             next: (response) => {
    //                 console.log('Guardado en backend:', JSON.stringify(response)); // borrar
    //                 this.notificationService.notifySuccess(response.message);
    //                 this.closeModal();
    //             },
    //             error: (errorResponse) => {
    //                 console.log('LOG ERROR:', JSON.stringify(errorResponse)); // borrar
    //                 console.error('Error:', errorResponse);
    //                 this.notificationService.notifyError(errorResponse.error.message);
    //             }
    //         });
    //     }
    //     else {
    //         const request: BuyRaffleNumberDTO = {
    //             aGuestUser: this.guestUser,
    //             someNumbersToBuy: this.selectedRaffleNumbers
    //         };
    //         this.questionaryService.saveRaffleNumber(
    //             this.eventId,
    //             request
    //         ).subscribe({
    //             next: (response) => {
    //                 this.notificationService.notifySuccess('Compra realizada con éxito');
    //                 this.closeModal();
    //             },
    //             error: (errorResponse) => {
    //                 console.error('Error en la compra:', errorResponse);
    //                 this.notificationService.notifyError(errorResponse.error.message);
    //             }
    //         });
    //     }
    // }

    // getters convenientes para el template
    get nameControl() { return this.form.get('name'); }
    get surnameControl() { return this.form.get('surname'); }
    get emailControl() { return this.form.get('email'); }
    get cellphoneControl() { return this.form.get('cellphone'); }
}
