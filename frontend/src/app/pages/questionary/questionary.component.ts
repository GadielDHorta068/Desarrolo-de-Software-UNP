import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { QuestionaryService } from '../../services/questionary.service';
import { GuestUser } from './guestUser';
import { NotificationService } from '../../services/notification.service';


@Component({
  selector: 'app-questionary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './questionary.component.html',
  styleUrl: './questionary.component.css'
})

export class QuestionaryComponent {

    @Input() eventId!: number;
    @Output() close = new EventEmitter<void>();

    guestUser!: GuestUser;

    constructor(
        private questionaryService: QuestionaryService,
        private notificationService: NotificationService
    ) {}

    ngOnInit() {
        this.guestUser = <GuestUser> {};
    }

    validatePhoneInput(event: KeyboardEvent) {
        const allowedChars = /[0-9+]/;
        const inputChar = event.key;

        if (!allowedChars.test(inputChar)) {
            event.preventDefault();
        }
    }

    closeModal(): void {
        this.close.emit();
    }

    onSubmit(): void {
        this.questionaryService.save(
            this.guestUser,
            this.eventId //despues podria tener un objeto y usar un dto o algo asi
        ).subscribe({
            next: (response) => {
                console.log('Guardado en backend:', response.message ); // borrar
                this.notificationService.notifySuccess('Registo al sorteo exitoso');
                this.closeModal();
            },
            error: (errorMessage) => {
                console.error('Error:', errorMessage);
                this.notificationService.notifyError('Error al guardar, intentá de nuevo');
            }
        });
    }
}

/*
    cambiar los Alert por alguno de los sig:
    Angular Material (MatSnackBar)

    SweetAlert2 (fácil de usar y se ve muy lindo)

    PrimeNG Toast
*/