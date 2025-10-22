import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { QuestionaryService } from '../../services/questionary.service';
import { GuestUser } from './guestUser';
import { NotificationService } from '../../services/notification.service';
import { EventTypes } from '../../models/events.model';
import { BuyRaffleNumberDTO } from '../../models/buyRaffleNumberDTO';
import { UserDTO } from '../../models/UserDTO';


@Component({
  selector: 'app-questionary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './questionary.component.html',
  styleUrl: './questionary.component.css'
})

export class QuestionaryComponent {

    @Input() eventId!: number;
    @Input() eventType!: EventTypes;
    @Input() selectedRaffleNumbers: number[] = [];
    @Output() close = new EventEmitter<void>();

    guestUser!: UserDTO;

    constructor(
        private questionaryService: QuestionaryService,
        private notificationService: NotificationService
    ) {}

    ngOnInit() {
        this.guestUser = <UserDTO> {};
    }

    validatePhoneInput(event: KeyboardEvent) {
        const allowedChars = /[0-9]/; // Solo dígitos
        const inputChar = event.key;

        if (!allowedChars.test(inputChar)) {
            event.preventDefault();
        }
    }

    onCellphoneInput(event: Event): void {
        const input = event.target as HTMLInputElement;
        const digits = (input.value || '').replace(/\D/g, '').slice(0, 10);
        this.guestUser.cellphone = digits;
    }

    private formatArgPhone(input: string): string {
        const digits = (input || '').replace(/\D/g, '');
        if (!digits) return '';
        return digits.startsWith('54') ? digits : '54' + digits;
    }

    closeModal(): void {
        this.close.emit();
    }

    onSubmit(): void {
        // Formatear teléfono: solo dígitos y prefijo 54 si corresponde
        if (this.guestUser?.cellphone) {
            this.guestUser.cellphone = this.formatArgPhone(this.guestUser.cellphone);
        }

        if (this.eventType != EventTypes.RAFFLES) {
            this.questionaryService.save(
                this.guestUser,
                this.eventId //despues podria tener un objeto y usar un dto o algo asi
            ).subscribe({
                next: (response) => {
                    console.log('Guardado en backend:', JSON.stringify(response)); // borrar
                    this.notificationService.notifySuccess(response.message);
                    this.closeModal();
                },
                error: (errorResponse) => {
                    console.log('LOG ERROR:', JSON.stringify(errorResponse)); // borrar
                    console.error('Error:', errorResponse);
                    this.notificationService.notifyError(errorResponse.error.message);
                }
            });
        }
        else {
            const request: BuyRaffleNumberDTO = {
                aGuestUser: this.guestUser,
                someNumbersToBuy: this.selectedRaffleNumbers
            };
            this.questionaryService.saveRaffleNumber(
                this.eventId,
                request
            ).subscribe({
                next: (response) => {
                    this.notificationService.notifySuccess('Compra realizada con éxito');
                    this.closeModal();
                },
                error: (errorResponse) => {
                    console.error('Error en la compra:', errorResponse);
                    this.notificationService.notifyError(errorResponse.error.message);
                }
            });
        }
    }
}
