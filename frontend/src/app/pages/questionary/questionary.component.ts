import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { QuestionaryService } from '../../services/questionary.service';
import { GuestUser } from './guestUser';
import { NotificationService } from '../../services/notification.service';
import { EventsTemp, EventTypes, StatusEvent } from '../../models/events.model';
import { BuyRaffleNumberDTO } from '../../models/buyRaffleNumberDTO';
import { UserDTO } from '../../models/UserDTO';
import { EventsService } from '../../services/events.service';
import { ActivatedRoute } from '@angular/router';


@Component({
  selector: 'app-questionary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './questionary.component.html',
  styleUrl: './questionary.component.css'
})

export class QuestionaryComponent {

    @Input() eventId!: number;

    @Output() onInscript = new EventEmitter<UserDTO>();
    @Output() close = new EventEmitter<void>();

    
    // eventIdParam!: Number;
    event!: EventsTemp | null;
    allEventTypes = EventTypes;
    allEventStates = StatusEvent;

    guestUser!: UserDTO;

    constructor(
        private eventService: EventsService,
        private activatedRoute: ActivatedRoute,
        private questionaryService: QuestionaryService,
        private notificationService: NotificationService
    ) {}

    ngOnInit() {
        const eventIdParam = this.eventId ?? Number(this.activatedRoute.snapshot.paramMap.get('eventId'));
        if (eventIdParam != null) {
            this.eventService.getEventById("" + eventIdParam).subscribe(
                resp => {
                    this.event = resp;
                    if (this.event && this.event.statusEvent === this.allEventStates.OPEN) {
                        this.guestUser = <UserDTO> {};
                    }
                    // HACER ALGO SI NO ESTA ABIERTO EL EVENTO
                }
            );
        }
    }

    validatePhoneInput(event: KeyboardEvent) {
        const allowedChars = /[0-9+]/;
        const inputChar = event.key;

        if (!allowedChars.test(inputChar)) {
            event.preventDefault();
        }
    }

    onConfirmInscription(): void {
        this.onInscript.emit(this.guestUser);
        this.close.emit();
    }

    closeModal(): void {
        this.close.emit();
    }

}