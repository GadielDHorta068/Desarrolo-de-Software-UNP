import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { QuestionaryService } from '../../services/questionary.service';
import { GuestUser } from './guestUser';


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
        private questionaryService: QuestionaryService
    ) {}

    ngOnInit() {
        this.guestUser = <GuestUser> {};
    }

    closeModal(): void {
        this.close.emit();
    }

    onSubmit(): void {
        this.questionaryService.save(
            this.guestUser,
            this.eventId //despues podria tener un objeto y usar un dto o algo asi
        ).subscribe({
            next: (responseMessage) => {
                console.log('Guardado en backend:', responseMessage); // borrar
                this.closeModal();
                alert('Usuario participante registrado al sorteo exitosamente');
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