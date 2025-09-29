import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
    
    constructor(private snackBar: MatSnackBar) {}

    notifySuccess(message: string, action: string = 'X', duration: number = 2500) {
        this.snackBar.open(message, action, {
            duration: duration,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['snackbar-success']
        });
    }

    notifyError(message: string, action: string = 'X', duration: number = 2500) {
        this.snackBar.open(message, action, {
            duration: duration,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['snackbar-error']
        });
    }
}
