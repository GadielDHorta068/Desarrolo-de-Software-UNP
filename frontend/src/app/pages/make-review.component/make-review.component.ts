import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { reviewFromFrontToBackDTO } from '../../models/review/reviewFromFrontToBackDTO';
import { DeliveryStatus } from '../../models/review/DeliveryStatus';
import { ReviewService } from '../../services/review.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-make-review',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './make-review.component.html',
  styleUrl: './make-review.component.css'
})
export class MakeReviewComponent {
    deliveryStatus = DeliveryStatus;
    
    review: reviewFromFrontToBackDTO = {
    email: '',
    score: 0,
    comment: '',
    delivery: this.deliveryStatus.A_TIEMPO,
  };
    
    eventId!: string;


    constructor(
        private route: ActivatedRoute,
        private reviewService: ReviewService,
        private notifyService: NotificationService
    ) {}

    ngOnInit() {
        this.eventId = this.route.snapshot.paramMap.get('eventId')!;
    }



    enviarReview() {
        this.reviewService.createReview(this.review, this.eventId).subscribe({
            next: (response) => {
                console.log(response.message);
                this.notifyService.notifySuccess(response.message);
            },
            error: (error) => {
                this.notifyService.notifyError(error.error.message);
                console.error(error.message);
            }
        });
    }
}
