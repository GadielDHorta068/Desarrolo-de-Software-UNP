import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { QuestionaryService } from '../../services/questionary.service';
import { GuestUser } from './guestUser';


@Component({
  selector: 'app-questionary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './questionary.component.html',
  styleUrl: './questionary.component.css'
})

export class QuestionaryComponent {

    eventId!: number;
    guestUser!: GuestUser;
    isOpen = false;

    constructor(
        private questionaryService: QuestionaryService
    ) {}

    openModal(aEventId: number): void {
        this.eventId = aEventId;
        this.guestUser = {
            id: 0,
            name: '',
            surname: '',
            email: '',
            cellphone: ''
        }; // resetea el form por las dudas
        this.isOpen = true;
    }

    closeModal(): void {
        this.isOpen = false;
    }

    save(): void {
        this.questionaryService.save(
            this.guestUser,
            this.eventId //despues podria tener un objeto y usar un dto o algo asi
        ).subscribe({
            next: (responseMessage) => {
                console.log('Guardado en backend:', responseMessage);
                this.closeModal();
                alert('Usuario guardado con éxito');
            },
            error: (errorMessage) => {
                console.error('Error:', errorMessage);
                alert('Error al guardar, intentá de nuevo')
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