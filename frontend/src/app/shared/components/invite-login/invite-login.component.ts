import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-invite-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invite-login.component.html',
  styleUrl: './invite-login.component.css'
})
export class InviteLoginComponent {

    @Input() eventId!: number;

    // @Output() onInscript = new EventEmitter<UserDTO>();
    @Output() close = new EventEmitter<any>();

    constructor(
    ) {}

    ngOnInit() {
        
    }


    closeModal(): void {
        this.close.emit();
    }

    onRedirectLogin(){
        this.close.emit({redirect: true});
    }
}
