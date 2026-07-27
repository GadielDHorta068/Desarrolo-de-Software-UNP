import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { reviewFromFrontToBackDTO } from '../../models/review/reviewFromFrontToBackDTO';
import { DeliveryStatus } from '../../models/review/DeliveryStatus';
import { ReviewService } from '../../services/review.service';
import { NotificationService } from '../../services/notification.service';
import { StarRatingComponent } from '../star-rating.component/star-rating.component';
import { AwardAlignment } from '../../models/review/AwardAlignment';
import { CommunicationRating } from '../../models/review/CommunicationRating';

@Component({
    selector: 'app-make-review',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        StarRatingComponent
    ],
    templateUrl: './make-review.component.html',
    styleUrl: './make-review.component.css'
})
export class MakeReviewComponent {
    deliveryStatus = DeliveryStatus;
    awardAlignmentOptions = AwardAlignment;
    communicationRatingOptions = CommunicationRating;
    
    review: reviewFromFrontToBackDTO = {
        email: '',
        score: 0,
        comment: '',
        delivery: null!,
        awardAlingment: null!,
        communicationRating: null!,
        urlShortcode: null!
    };
    
    eventId!: string;


    constructor(
        private route: ActivatedRoute,
        private reviewService: ReviewService,
        private notifyService: NotificationService
    ) {}

    ngOnInit() {
        this.eventId = this.route.snapshot.paramMap.get('eventId')!;
        const shortcode = this.route.snapshot.paramMap.get('shortcode');

        if (!shortcode) {
            this.notifyService.notifyError('El enlace de la reseña no es válido.');
            return;
        }

        this.review.urlShortcode = shortcode;
    }



    enviarReview() {
        this.reviewService.createReview(this.review, this.eventId).subscribe({
            next: (response) => {
                console.log(response.message);
                this.notifyService.notifySuccess(response.message);
            },
            error: (error) => {
                if (error.status === 409) {
                    this.notifyService.notifyWarning(error.error.message);    
                } else {
                    this.notifyService.notifyError(error.error.message);
                }

                console.error(error);
            }
        });
    }

    onDeliveryChange(): void {
    if (this.review.delivery === DeliveryStatus.NO_RECIBIDO) {
        this.review.awardAlingment = null;
    }
}
}
